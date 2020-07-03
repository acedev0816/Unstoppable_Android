package io.horizontalsystems.bankwallet.modules.notifications

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.*
import io.horizontalsystems.bankwallet.entities.Coin
import io.horizontalsystems.bankwallet.entities.PriceAlert
import io.horizontalsystems.bankwallet.modules.notifications.bottommenu.NotificationMenuMode
import io.horizontalsystems.core.SingleLiveEvent
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class NotificationsViewModel(
        private val priceAlertManager: IPriceAlertManager,
        walletManager: IWalletManager,
        private val coinManager: ICoinManager,
        private val notificationManager: INotificationManager,
        private val localStorage: ILocalStorage) : ViewModel() {

    private val viewItems = mutableListOf<NotificationViewItem>()
    private val portfolioCoins = walletManager.wallets.map { it.coin }
    private var disposable: Disposable? = null

    val itemsLiveData = MutableLiveData<List<NotificationViewItem>>()
    val openNotificationSettings = SingleLiveEvent<Void>()
    val setWarningVisible = MutableLiveData<Boolean>()
    val notificationIsOnLiveData = MutableLiveData<Boolean>()
    val openOptionsDialog = SingleLiveEvent<Triple<String, String, NotificationMenuMode>>()

    private var notificationIsOn: Boolean
        get() = localStorage.isAlertNotificationOn
        set(value) {
            localStorage.isAlertNotificationOn = value
        }

    init {
        loadAlerts()
        checkPriceAlertsEnabled()
        setNotificationIsOnSwitch()

        disposable = priceAlertManager.notificationChangedFlowable
                .subscribeOn(Schedulers.io())
                .subscribe {
                    loadAlerts()
                }
    }

    override fun onCleared() {
        disposable?.dispose()
        super.onCleared()
    }

    fun openSettings() {
        openNotificationSettings.call()
    }

    fun deactivateAll() {
        priceAlertManager.deactivateAllNotifications()
    }

    fun onResume() {
        checkPriceAlertsEnabled()
    }

    fun switchAlertNotification(checked: Boolean) {
        notificationIsOn = checked
        setNotificationIsOnSwitch()

        if (checked) {
            priceAlertManager.enablePriceAlerts()
        } else {
            priceAlertManager.disablePriceAlerts()
        }
    }

    fun onDropdownTap(item: NotificationViewItem) {
        val coinCode = item.coinCode ?: return
        val mode = when (item.type) {
            NotificationViewItemType.ChangeOption -> NotificationMenuMode.Change
            else -> NotificationMenuMode.Trend
        }
        openOptionsDialog.postValue(Triple(item.coinName, coinCode, mode))
    }

    private fun setNotificationIsOnSwitch() {
        notificationIsOnLiveData.postValue(notificationIsOn)
    }

    private fun loadAlerts() {
        viewItems.clear()

        val priceAlerts = priceAlertManager.getPriceAlerts()
        portfolioCoins.forEach { coin ->
            val priceAlert = priceAlerts.firstOrNull { it.coinCode == coin.code }
            viewItems.addAll(getPriceAlertViewItems(coin, priceAlert))
        }

        getOtherCoinsWithAlert(priceAlerts).forEach { coin ->
            priceAlerts.firstOrNull { it.coinCode == coin.code }?.let { priceAlert ->
                viewItems.addAll(getPriceAlertViewItems(coin, priceAlert))
            }
        }

        itemsLiveData.postValue(viewItems)
    }

    private fun getOtherCoinsWithAlert(priceAlerts: List<PriceAlert>): List<Coin> {
        val portfolioCoinCodes = portfolioCoins.map { it.code }
        val allCoins = coinManager.coins
        val nonPortfolioCoinAlerts = priceAlerts.filter { alert ->
            portfolioCoinCodes.indexOf(alert.coinCode) == -1
        }

        return nonPortfolioCoinAlerts.mapNotNull { priceAlert ->
            allCoins.firstOrNull { priceAlert.coinCode == it.code }
        }
    }

    private fun getPriceAlertViewItems(coin: Coin, priceAlert: PriceAlert?): List<NotificationViewItem> {
        val items = mutableListOf<NotificationViewItem>()
        items.add(NotificationViewItem(coin.title, NotificationViewItemType.CoinName))
        items.add(NotificationViewItem(coin.title, NotificationViewItemType.ChangeOption, coin.code, titleRes = R.string.NotificationBottomMenu_Change24h, dropdownValue = getChangeValue(priceAlert?.changeState)))
        items.add(NotificationViewItem(coin.title, NotificationViewItemType.TrendOption, coin.code, titleRes = R.string.NotificationBottomMenu_PriceTrendChange, dropdownValue = getTrendValue(priceAlert?.trendState)))
        return items
    }

    @StringRes
    private fun getChangeValue(changeState: PriceAlert.ChangeState?): Int {
        return when (changeState) {
            PriceAlert.ChangeState.PERCENT_2 -> R.string.NotificationBottomMenu_2
            PriceAlert.ChangeState.PERCENT_5 -> R.string.NotificationBottomMenu_5
            PriceAlert.ChangeState.PERCENT_10 -> R.string.NotificationBottomMenu_10
            else -> R.string.SettingsNotifications_Off
        }
    }

    @StringRes
    private fun getTrendValue(changeState: PriceAlert.TrendState?): Int {
        return when (changeState) {
            PriceAlert.TrendState.SHORT -> R.string.NotificationBottomMenu_Short
            PriceAlert.TrendState.LONG -> R.string.NotificationBottomMenu_Long
            else -> R.string.SettingsNotifications_Off
        }
    }

    private fun checkPriceAlertsEnabled() {
        setWarningVisible.postValue(!notificationManager.isEnabled)
    }
}

data class NotificationViewItem(
        val coinName: String,
        val type: NotificationViewItemType,
        val coinCode: String? = null,
        @StringRes val titleRes: Int? = null,
        @StringRes val dropdownValue: Int = R.string.SettingsNotifications_Off)

enum class NotificationViewItemType {
    CoinName,
    ChangeOption,
    TrendOption
}
