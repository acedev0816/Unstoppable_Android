package io.horizontalsystems.bankwallet.modules.market.discovery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.bankwallet.core.managers.ConnectivityManager
import io.horizontalsystems.bankwallet.modules.market.MarketField
import io.horizontalsystems.bankwallet.modules.market.MarketItem
import io.horizontalsystems.bankwallet.modules.market.MarketViewItem
import io.horizontalsystems.bankwallet.modules.market.SortingField
import io.horizontalsystems.core.SingleLiveEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class MarketDiscoveryViewModel(
        private val service: MarketDiscoveryService,
        private val connectivityManager: ConnectivityManager
) : ViewModel() {

    val sortingFields: Array<SortingField> = SortingField.values()
    val marketCategories: List<MarketCategory> by service::marketCategories

    var sortingField: SortingField = sortingFields.first()
        private set

    var marketField: MarketField = MarketField.PriceDiff
        private set

    val marketViewItemsLiveData = MutableLiveData<List<MarketViewItem>>()

    val loadingLiveData = MutableLiveData(false)
    val errorLiveData = MutableLiveData<String?>(null)

    val networkNotAvailable = SingleLiveEvent<Unit>()

    private val disposables = CompositeDisposable()

    init {
        service.stateObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    syncState(it)
                }
                .let {
                    disposables.add(it)
                }
    }

    private fun syncState(state: MarketDiscoveryService.State) {
        loadingLiveData.postValue(state is MarketDiscoveryService.State.Loading)

        if (state is MarketDiscoveryService.State.Error && !connectivityManager.isConnected) {
            networkNotAvailable.postValue(Unit)
        }

        errorLiveData.postValue((state as? MarketDiscoveryService.State.Error)?.error?.let { convertErrorMessage(it) })

        if (state is MarketDiscoveryService.State.Loaded) {
            syncViewItemsBySortingField()
        }
    }

    private fun convertErrorMessage(it: Throwable): String {
        return it.message ?: it.javaClass.simpleName
    }

    fun update(sortingField: SortingField? = null, marketField: MarketField? = null) {
        sortingField?.let {
            this.sortingField = it
        }
        marketField?.let {
            this.marketField = it
        }
        syncViewItemsBySortingField()
    }

    private fun syncViewItemsBySortingField() {
        val viewItems = sort(service.marketItems, sortingField).map {
            MarketViewItem.create(it, service.currency.symbol, marketField)
        }

        marketViewItemsLiveData.postValue(viewItems)
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

    fun refresh() {
        service.refresh()
    }

    fun onErrorClick() {
        service.refresh()
    }

    fun onSelectMarketCategory(marketCategory: MarketCategory?) {
        service.marketCategory = marketCategory
    }

}

inline fun <T, R : Comparable<R>> Iterable<T>.sortedByDescendingNullLast(crossinline selector: (T) -> R?): List<T> {
    return sortedWith(Comparator.nullsLast(compareByDescending(selector)))
}

inline fun <T, R : Comparable<R>> Iterable<T>.sortedByNullLast(crossinline selector: (T) -> R?): List<T> {
    return sortedWith(Comparator.nullsLast(compareBy(selector)))
}
