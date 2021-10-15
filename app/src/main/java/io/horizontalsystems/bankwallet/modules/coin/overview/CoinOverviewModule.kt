package io.horizontalsystems.bankwallet.modules.coin.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.modules.coin.CoinViewFactory
import io.horizontalsystems.marketkit.models.FullCoin
import java.math.BigDecimal

object CoinOverviewModule {

    class Factory(private val fullCoin: FullCoin) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val currency = App.currencyManager.baseCurrency

            val service = CoinOverviewService(
                fullCoin,
                currency,
                App.marketKit,
                App.chartTypeStorage,
                App.appConfigProvider.guidesUrl,
                App.languageManager
            )

            return CoinOverviewViewModel(
                service,
                fullCoin.coin.code,
                CoinViewFactory(currency, App.numberFormatter),
                listOf(service)) as T
        }

    }
}

data class TitleViewItem(
    val rate: String?,
    val rateDiff: BigDecimal?
)
