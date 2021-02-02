package io.horizontalsystems.bankwallet.modules.market.overview

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.core.Clearable
import io.horizontalsystems.bankwallet.modules.market.MarketField
import io.horizontalsystems.bankwallet.modules.market.MarketItem
import io.horizontalsystems.bankwallet.modules.market.SortingField
import io.horizontalsystems.bankwallet.modules.market.MarketViewItem
import io.horizontalsystems.bankwallet.modules.market.sortedByDescendingNullLast
import io.horizontalsystems.bankwallet.modules.market.sortedByNullLast
import io.reactivex.disposables.CompositeDisposable

class MarketOverviewViewModel(
        private val service: MarketOverviewService,
        private val clearables: List<Clearable>
) : ViewModel() {

    val topGainersViewItemsLiveData = MutableLiveData<List<MarketViewItem>>()
    val topLosersViewItemsLiveData = MutableLiveData<List<MarketViewItem>>()
    val topByVolumeViewItemsLiveData = MutableLiveData<List<MarketViewItem>>()
    val showPoweredByLiveData = MutableLiveData(false)

    val loadingLiveData = MutableLiveData(false)
    val errorLiveData = MutableLiveData<String?>(null)

    private val disposable = CompositeDisposable()

    init {
        service.stateObservable
                .subscribe {
                    syncState(it)
                }
                .let {
                    disposable.add(it)
                }
    }

    private fun syncState(state: MarketOverviewService.State) {
        loadingLiveData.postValue(state is MarketOverviewService.State.Loading)
        errorLiveData.postValue((state as? MarketOverviewService.State.Error)?.error?.let { convertErrorMessage(it) })

        if (state is MarketOverviewService.State.Loaded) {
            syncViewItemsBySortingField()
        }

        showPoweredByLiveData.postValue(service.marketItems.isNotEmpty())
    }

    private fun syncViewItemsBySortingField() {
        topGainersViewItemsLiveData.postValue(sort(service.marketItems, SortingField.TopGainers).subList(0, 3).map { this.convertItemToViewItem(it, MarketField.PriceDiff) })
        topLosersViewItemsLiveData.postValue(sort(service.marketItems, SortingField.TopLosers).subList(0, 3).map { this.convertItemToViewItem(it, MarketField.PriceDiff) })
        topByVolumeViewItemsLiveData.postValue(sort(service.marketItems, SortingField.HighestVolume).subList(0, 3).map { this.convertItemToViewItem(it, MarketField.Volume) })
    }

    private fun convertItemToViewItem(it: MarketItem, marketField: MarketField): MarketViewItem {
        val formattedRate = App.numberFormatter.formatFiat(it.rate, service.currency.symbol, 2, 2)
        val marketDataValue = when (marketField) {
            MarketField.MarketCap -> {
                val marketCapFormatted = it.marketCap?.let { marketCap ->
                    val (shortenValue, suffix) = App.numberFormatter.shortenValue(marketCap)
                    App.numberFormatter.formatFiat(shortenValue, service.currency.symbol, 0, 2) + suffix
                }

                MarketViewItem.MarketDataValue.MarketCap(marketCapFormatted ?: App.instance.getString(R.string.NotAvailable))
            }
            MarketField.Volume -> {
                val (shortenValue, suffix) = App.numberFormatter.shortenValue(it.volume)
                val volumeFormatted = App.numberFormatter.formatFiat(shortenValue, service.currency.symbol, 0, 2) + suffix

                MarketViewItem.MarketDataValue.Volume(volumeFormatted)
            }
            MarketField.PriceDiff -> MarketViewItem.MarketDataValue.Diff(it.diff)
        }


        return MarketViewItem(it.score, it.coinCode, it.coinName, formattedRate, it.diff, marketDataValue)
    }

    private fun convertErrorMessage(it: Throwable): String {
        return it.message ?: it.javaClass.simpleName
    }


    override fun onCleared() {
        clearables.forEach(Clearable::clear)
        disposable.clear()
        super.onCleared()
    }

    fun refresh() {
        service.refresh()
    }

    private fun sort(items: List<MarketItem>, sortingField: SortingField) = when (sortingField) {
        SortingField.HighestCap -> items.sortedByDescendingNullLast { it.marketCap }
        SortingField.LowestCap -> items.sortedByNullLast { it.marketCap }
        SortingField.HighestVolume -> items.sortedByDescendingNullLast { it.volume }
        SortingField.LowestVolume -> items.sortedByNullLast { it.volume }
        SortingField.HighestPrice -> items.sortedByDescendingNullLast { it.rate }
        SortingField.LowestPrice -> items.sortedByNullLast { it.rate }
        SortingField.TopGainers -> items.sortedByDescendingNullLast { it.diff }
        SortingField.TopLosers -> items.sortedByNullLast { it.diff }
    }

}
