package io.horizontalsystems.bankwallet.modules.balance

import io.horizontalsystems.bankwallet.core.AdapterState
import io.horizontalsystems.bankwallet.entities.Coin
import io.horizontalsystems.bankwallet.entities.CoinValue
import io.horizontalsystems.bankwallet.entities.Currency
import io.horizontalsystems.bankwallet.entities.CurrencyValue
import io.horizontalsystems.bankwallet.lib.chartview.models.ChartData
import java.math.BigDecimal

data class BalanceViewItem(
        val coin: Coin,
        val coinValue: CoinValue,
        val exchangeValue: CurrencyValue?,
        val currencyValue: CurrencyValue?,
        val state: AdapterState,
        val chartStatsFetching: Boolean,
        val chartStats: Pair<BigDecimal, ChartData>?,
        val rateExpired: Boolean
)

data class BalanceHeaderViewItem(
        val currencyValue: CurrencyValue?,
        val upToDate: Boolean,
        val chartEnabled: Boolean
)

class BalanceViewItemFactory {

    fun createViewItem(item: BalanceModule.BalanceItem, currency: Currency?): BalanceViewItem {
        var exchangeValue: CurrencyValue? = null
        var currencyValue: CurrencyValue? = null

        item.rate?.let { rate ->
            currency?.let {
                exchangeValue = CurrencyValue(it, rate.value)
                currencyValue = CurrencyValue(it, rate.value * item.balance)
            }
        }

        return BalanceViewItem(
                item.wallet.coin,
                CoinValue(item.wallet.coin.code, item.balance),
                exchangeValue,
                currencyValue,
                item.state,
                item.chartStatsFetching,
                item.chartStats,
                item.rate?.expired ?: false
        )
    }

    fun createHeaderViewItem(items: List<BalanceModule.BalanceItem>, chartEnabled: Boolean, currency: Currency?): BalanceHeaderViewItem {
        var sum = BigDecimal.ZERO
        var expired = false
        val nonZeroItems = items.filter { it.balance > BigDecimal.ZERO }

        nonZeroItems.forEach { balanceItem ->
            val rate = balanceItem.rate

            rate?.value?.times(balanceItem.balance)?.let {
                sum += it
            }

            expired = expired || balanceItem.state != AdapterState.Synced || rate?.expired == true
        }

        return BalanceHeaderViewItem(currency?.let { CurrencyValue(it, sum) }, !expired, chartEnabled)
    }

}
