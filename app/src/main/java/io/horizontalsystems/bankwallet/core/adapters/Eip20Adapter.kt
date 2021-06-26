package io.horizontalsystems.bankwallet.core.adapters

import android.content.Context
import io.horizontalsystems.bankwallet.core.*
import io.horizontalsystems.bankwallet.entities.TransactionRecord
import io.horizontalsystems.bankwallet.entities.TransactionType
import io.horizontalsystems.erc20kit.core.Erc20Kit
import io.horizontalsystems.ethereumkit.core.EthereumKit
import io.horizontalsystems.ethereumkit.core.EthereumKit.SyncState
import io.horizontalsystems.ethereumkit.core.hexStringToByteArray
import io.horizontalsystems.ethereumkit.models.*
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigDecimal
import java.math.BigInteger

class Eip20Adapter(
        context: Context,
        evmKit: EthereumKit,
        decimal: Int,
        contractAddress: String,
        private val fee: BigDecimal = BigDecimal.ZERO,
        override val minimumRequiredBalance: BigDecimal = BigDecimal.ZERO,
        override val minimumSendAmount: BigDecimal = BigDecimal.ZERO
) : BaseEvmAdapter(evmKit, decimal) {

    private val contractAddress: Address = Address(contractAddress)
    val eip20Kit: Erc20Kit = Erc20Kit.getInstance(context, this.evmKit, this.contractAddress)

    val pendingTransactions: List<TransactionRecord>
        get() = eip20Kit.getPendingTransactions().map { transactionRecord(it) }

    // IAdapter

    override fun start() {
        eip20Kit.start()
    }

    override fun stop() {
        // stopped via EthereumKitManager
    }

    override fun refresh() {
        eip20Kit.refresh()
    }

    // IBalanceAdapter

    override val balanceState: AdapterState
        get() = convertToAdapterState(eip20Kit.syncState)

    override val balanceStateUpdatedFlowable: Flowable<Unit>
        get() = eip20Kit.syncStateFlowable.map { }

    override val balanceData: BalanceData
        get() = BalanceData(balanceInBigDecimal(eip20Kit.balance, decimal))

    override val balanceUpdatedFlowable: Flowable<Unit>
        get() = eip20Kit.balanceFlowable.map { Unit }

    // ITransactionsAdapter

    override val transactionsState: AdapterState
        get() = convertToAdapterState(eip20Kit.transactionsSyncState)

    override val transactionsStateUpdatedFlowable: Flowable<Unit>
        get() = eip20Kit.transactionsSyncStateFlowable.map { }

    override fun getTransactions(from: TransactionRecord?, limit: Int): Single<List<TransactionRecord>> {
        val fromHash = from?.transactionHash?.hexStringToByteArray()
        return eip20Kit.getTransactionsAsync(fromHash, limit)
                .flatMap { fullTransactionList ->
                    return@flatMap Single.just(fullTransactionList.map { transactionRecord(it) })
                }
    }

    override val transactionRecordsFlowable: Flowable<List<TransactionRecord>>
        get() = eip20Kit.transactionsFlowable.map { it.map { tx -> transactionRecord(tx) } }

    // ISendEthereumAdapter

    override fun sendInternal(address: Address, amount: BigInteger, gasPrice: Long, gasLimit: Long, logger: AppLogger): Single<Unit> {
        logger.info("call erc20Kit.buildTransferTransactionData")
        val transactionData = eip20Kit.buildTransferTransactionData(address, amount)

        return evmKit.send(transactionData, gasPrice, gasLimit)
                .doOnSubscribe {
                    logger.info("call ethereumKit.send")
                }
                .map {}
    }

    override fun estimateGasLimitInternal(toAddress: Address?, value: BigInteger, gasPrice: Long?): Single<Long> {
        if (toAddress == null) {
            return Single.just(evmKit.defaultGasLimit)
        }
        val transactionData = eip20Kit.buildTransferTransactionData(toAddress, value)

        return evmKit.estimateGas(transactionData, gasPrice)
    }

    override fun availableBalance(gasPrice: Long, gasLimit: Long): BigDecimal {
        return BigDecimal.ZERO.max(balanceData.available - fee)
    }

    override val ethereumBalance: BigDecimal
        get() = balanceInBigDecimal(evmKit.accountState?.balance, EvmAdapter.decimal)

    override fun getTransactionData(amount: BigInteger, address: Address): TransactionData {
        return eip20Kit.buildTransferTransactionData(address, amount)
    }

    private fun convertAmount(amount: BigInteger, fromAddress: Address): BigDecimal {
        var significandAmount = scaleDown(amount.toBigDecimal())

        if (significandAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
        }

        val fromMine = fromAddress == evmKit.receiveAddress

        if (fromMine) {
            significandAmount = significandAmount.negate()
        }

        return significandAmount
    }

    private fun convertToAdapterState(syncState: SyncState): AdapterState = when (syncState) {
        is SyncState.Synced -> AdapterState.Synced
        is SyncState.NotSynced -> AdapterState.NotSynced(syncState.error)
        is SyncState.Syncing -> AdapterState.Syncing(50, null)
    }

    private fun transactionRecord(fullTransaction: FullTransaction): TransactionRecord {
        val transaction = fullTransaction.transaction
        val receipt = fullTransaction.receiptWithLogs?.receipt

        var from = transaction.from
        var to = transaction.to

        var amount = convertAmount(transaction.value, transaction.from)

        amount += fullTransaction.internalTransactions
                .map {
                    from = it.from
                    to = it.to
                    convertAmount(it.value, it.from)
                }.fold(BigDecimal.ZERO) { acc, bigDecimal -> acc + bigDecimal }

        val type = when {
            transaction.from == transaction.to -> TransactionType.SentToSelf
            amount < BigDecimal.ZERO -> TransactionType.Outgoing
            else -> TransactionType.Incoming
        }

        val txHash = transaction.hash.toHexString()
        return TransactionRecord(
                uid = txHash,
                transactionHash = txHash,
                transactionIndex = receipt?.transactionIndex ?: 0,
                interTransactionIndex = 0,
                blockHeight = receipt?.blockNumber,
                amount = amount.abs(),
                confirmationsThreshold = confirmationsThreshold,
                timestamp = transaction.timestamp,
                from = from.eip55,
                memo = null,
                to = to?.eip55,
                type = type,
                failed = fullTransaction.isFailed()
        )
    }

    fun allowance(spenderAddress: Address, defaultBlockParameter: DefaultBlockParameter): Single<BigDecimal> {
        return eip20Kit.getAllowanceAsync(spenderAddress, defaultBlockParameter)
                .map {
                    scaleDown(it.toBigDecimal())
                }
    }

    companion object {
        private const val approveConfirmationsThreshold = 1

        fun clear(walletId: String, testMode: Boolean) {
            val networkTypes = when {
                testMode -> listOf(EthereumKit.NetworkType.EthRopsten)
                else -> listOf(EthereumKit.NetworkType.EthMainNet, EthereumKit.NetworkType.BscMainNet)
            }

            networkTypes.forEach {
                Erc20Kit.clear(App.instance, it, walletId)
            }
        }
    }

}
