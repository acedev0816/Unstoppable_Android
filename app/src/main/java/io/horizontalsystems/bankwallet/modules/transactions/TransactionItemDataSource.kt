package io.horizontalsystems.bankwallet.modules.transactions

import android.support.v7.util.DiffUtil
import io.horizontalsystems.bankwallet.entities.TransactionItem
import io.horizontalsystems.bankwallet.entities.TransactionRecord
import java.util.concurrent.CopyOnWriteArrayList

class TransactionItemDataSource {
    val count
        get() = items.size

    private val items = CopyOnWriteArrayList<TransactionItem>()

    fun clear() {
        items.clear()
    }

    fun add(items: List<TransactionItem>) {
        this.items.addAll(items)
    }

    fun itemForIndex(index: Int): TransactionItem = items[index]

    fun itemIndexesForTimestamp(coinCode: CoinCode, timestamp: Long): List<Int> {
        val indexes = mutableListOf<Int>()

        items.forEachIndexed { index, transactionItem ->
            if (transactionItem.coinCode == coinCode && transactionItem.record.timestamp == timestamp) {
                indexes.add(index)
            }
        }

        return indexes
    }

    fun itemIndexesForPending(coinCode: CoinCode, thresholdBlockHeight: Int): List<Int> {
        val indexes = mutableListOf<Int>()

        items.forEachIndexed { index, item ->
            if (item.coinCode == coinCode && (item.record.blockHeight == 0L || item.record.blockHeight >= thresholdBlockHeight)) {
                indexes.add(index)
            }
        }

        return indexes
    }

    fun handleModifiedItems(updatedItems: List<TransactionItem>, insertedItems: List<TransactionItem>): DiffUtil.DiffResult {
        val tmpList = items.toMutableList()
        tmpList.removeAll(updatedItems)
        tmpList.addAll(updatedItems)
        tmpList.addAll(insertedItems)
        tmpList.sortByDescending { it.record.timestamp }

        val diffCallback = TransactionDiffCallback(items.toList(), tmpList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items.clear()
        items.addAll(tmpList)

        return diffResult
    }

    fun shouldInsertRecord(record: TransactionRecord): Boolean {
        return items.lastOrNull()?.record?.let { it.timestamp < record.timestamp } ?: true
    }
}
