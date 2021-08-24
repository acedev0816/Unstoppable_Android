package io.horizontalsystems.bankwallet.modules.transactions.q

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.coinkit.models.Coin

object Transactions2Module {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return Transactions2ViewModel(
                Transactions2Service(
                    TransactionRecordRepository(App.transactionAdapterManager),
                    TransactionsXRateRepository(App.currencyManager, App.xRateManager),
                    TransactionSyncStateRepository(App.transactionAdapterManager),
                    App.transactionAdapterManager,
                    App.walletManager
                ),
                TransactionViewItem2Factory()
            ) as T
        }
    }

    data class Filter(val coin: Coin?)
}