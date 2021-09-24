package io.horizontalsystems.bankwallet.modules.coin.adapters

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.entities.CurrencyValue
import io.horizontalsystems.bankwallet.modules.coin.ChartInfoData
import io.horizontalsystems.bankwallet.modules.coin.ChartPointViewItem
import io.horizontalsystems.bankwallet.modules.coin.adapters.CoinChartAdapter.ChartViewType
import io.horizontalsystems.bankwallet.ui.compose.ComposeAppTheme
import io.horizontalsystems.bankwallet.ui.compose.components.TabButtonSecondary
import io.horizontalsystems.bankwallet.ui.compose.components.TabButtonSecondaryTransparent
import io.horizontalsystems.chartview.Chart
import io.horizontalsystems.chartview.ChartView
import io.horizontalsystems.chartview.models.ChartIndicator
import io.horizontalsystems.chartview.models.PointInfo
import io.horizontalsystems.core.entities.Currency
import io.horizontalsystems.core.helpers.DateHelper
import io.horizontalsystems.core.helpers.HudHelper
import io.horizontalsystems.views.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_holder_coin_chart.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

class CoinChartAdapter(
    viewItem: MutableLiveData<ViewItemWrapper>,
    private val currency: Currency,
    private val chartViewType: ChartViewType,
    private val listener: Listener,
    viewLifecycleOwner: LifecycleOwner
) : ListAdapter<CoinChartAdapter.ViewItemWrapper, ChartViewHolder>(diff) {

    init {
        viewItem.observe(viewLifecycleOwner) {
            submitList(listOf(it))
        }
    }

    interface Listener {
        fun onChartTouchDown()
        fun onChartTouchUp()
        fun onTabSelect(chartType: ChartView.ChartType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        return ChartViewHolder(
            inflate(parent, R.layout.view_holder_coin_chart, false),
            listener,
            currency,
            chartViewType
        )
    }

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {}

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = getItem(position)
        val prev = payloads.lastOrNull() as? ViewItemWrapper

        if (prev == null) {
            holder.bind(item)
        } else {
            holder.bindUpdate(item, prev)
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<ViewItemWrapper>() {
            override fun areItemsTheSame(oldItem: ViewItemWrapper, newItem: ViewItemWrapper): Boolean = true

            override fun areContentsTheSame(oldItem: ViewItemWrapper, newItem: ViewItemWrapper): Boolean {
                return oldItem.data == newItem.data
                        && oldItem.showError == newItem.showError
                        && oldItem.showSpinner == newItem.showSpinner
            }

            override fun getChangePayload(oldItem: ViewItemWrapper, newItem: ViewItemWrapper): Any? {
                return oldItem
            }
        }
    }

    data class ViewItemWrapper(
            val data: ChartInfoData?,
            val showSpinner: Boolean,
            val showError: Boolean
    )

    enum class ChartViewType{
        CoinChart, MarketMetricChart
    }

}

class ChartViewHolder(
    override val containerView: View,
    private val listener: CoinChartAdapter.Listener,
    private val currency: Currency,
    private val chartViewType: ChartViewType
)
    : RecyclerView.ViewHolder(containerView), LayoutContainer, Chart.Listener {

    init {
        chart.setListener(this)
        pointInfoVolumeTitle.isInvisible = true
        pointInfoVolume.isInvisible = true
    }

    private var macdIsEnabled = false
    private var enabledIndicator: ChartIndicator? = null

    fun bind(item: CoinChartAdapter.ViewItemWrapper) {
        if (item.showError) {
            chart.showError(containerView.context.getString(R.string.CoinPage_NoData))
            chart.hideSpinner()
        }

        if (item.showSpinner) {
            chart.showSpinner()
        } else {
            chart.hideSpinner()
        }

        item.data?.let { data ->
            containerView.post {
                chart.setData(data.chartData, data.chartType, data.maxValue, data.minValue)
            }

            bindTabs(getSelectedTabIndex(data.chartType), chartViewType, true)

            updateIndicatorsState(data.chartType)
        }

    }

    private fun getSelectedTabIndex(chartType: ChartView.ChartType): Int {
        var selectedIndex = getActions(chartViewType).indexOfFirst { it.first == chartType }
        if (selectedIndex < 0) {
            selectedIndex = 0
        }
        return selectedIndex
    }

    fun bindUpdate(current: CoinChartAdapter.ViewItemWrapper, prev: CoinChartAdapter.ViewItemWrapper) {
        current.apply {
            if (showSpinner != prev.showSpinner) {
                if (showSpinner) {
                    chart.showSpinner()
                } else {
                    chart.hideSpinner()
                }
            }
            if (showError != prev.showError && showError) {
                chart.showError(containerView.context.getString(R.string.CoinPage_NoData))
                chart.hideSpinner()
            }
            if (data != prev.data) {
                data?.let { data ->
                    chart.setData(data.chartData, data.chartType, data.maxValue, data.minValue)

                    val shouldScroll = prev.data == null
                    bindTabs(getSelectedTabIndex(data.chartType), chartViewType, shouldScroll)

                    updateIndicatorsState(data.chartType)
                }
            }
        }
    }

    override fun onTouchDown() {
        listener.onChartTouchDown()
        chartPointsInfo.isInvisible = false
        tabCompose.isInvisible = true
    }

    override fun onTouchUp() {
        listener.onChartTouchUp()
        chartPointsInfo.isInvisible = true
        tabCompose.isInvisible = false
    }

    override fun onTouchSelect(point: PointInfo) {
        val price = CurrencyValue(currency, point.value.toBigDecimal())

        if (macdIsEnabled) {
            setSelectedPoint(ChartPointViewItem(point.timestamp, price, null, point.macdInfo))
        } else {
            val volume = point.volume?.let { volume ->
                CurrencyValue(currency, volume.toBigDecimal())
            }
            setSelectedPoint(ChartPointViewItem(point.timestamp, price, volume, null))
        }

        HudHelper.vibrate(containerView.context)
    }

    private fun updateIndicatorsState(chartType: ChartView.ChartType) {
        val enabled = chartType != ChartView.ChartType.DAILY && chartType != ChartView.ChartType.TODAY

        enabledIndicator?.let {
            chart.setIndicator(it, enabled)
        }

        setIndicators(enabled)
    }

    private fun bindTabs(
        selectedIndex: Int = 0,
        chartViewType: ChartViewType,
        shouldScroll: Boolean
    ) {
        val tabs = getActions(chartViewType)

        tabCompose.setContent {
            val coroutineScope = rememberCoroutineScope()
            val listState = rememberLazyListState()

            ComposeAppTheme {
                CustomTab(
                    tabs.map { containerView.context.getString(it.second) },
                    selectedIndex,
                    listState
                )
            }

            if (shouldScroll) {
                coroutineScope.launch {
                    listState.scrollToItem(index = selectedIndex)
                }
            }
        }
    }

    @Composable
    private fun CustomTab(tabTitles: List<String>, selectedIndex: Int, listState: LazyListState) {
        var tabIndex by remember { mutableStateOf(selectedIndex) }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            itemsIndexed(tabTitles) { index, title ->
                val selected = tabIndex == index
                TabButtonSecondaryTransparent(
                    title = title,
                    onSelect = {
                        tabIndex = index
                        listener.onTabSelect(getActions(chartViewType)[index].first)
                    },
                    selected = selected
                )
            }
        }
    }

    private fun setIndicators(enabled: Boolean) {
        if (chartViewType == ChartViewType.MarketMetricChart){
            indicatorsCompose.isVisible = false
            return
        }

        indicatorsCompose.setContent {
            ComposeAppTheme {
                Column {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.padding(top = 7.dp)
                    ) {
                        items(getIndicators(enabled)) { item ->
                            TabButtonSecondary(
                                title = getIndicatorTitle(item.indicator),
                                onSelect = {
                                    onIndicatorChanged(item.indicator, !item.checked)
                                    setIndicators(enabled)
                                },
                                selected = item.checked,
                                enabled = item.enabled
                            )
                        }
                    }
                    Divider(
                        modifier = Modifier.padding(top = 7.dp),
                        thickness = 1.dp,
                        color = ComposeAppTheme.colors.steel10
                    )
                }
            }
        }
    }

    private fun getIndicators(enabled: Boolean) : List<IndicatorViewItem> {
        return listOf(
            IndicatorViewItem(ChartIndicator.Ema, enabledIndicator == ChartIndicator.Ema, enabled),
            IndicatorViewItem(ChartIndicator.Macd, enabledIndicator == ChartIndicator.Macd, enabled),
            IndicatorViewItem(ChartIndicator.Rsi, enabledIndicator == ChartIndicator.Rsi, enabled)
        )
    }

    private fun getIndicatorTitle(indicator: ChartIndicator): String {
        return when(indicator){
            ChartIndicator.Ema -> containerView.context.getString(R.string.CoinPage_IndicatorEMA)
            ChartIndicator.Macd -> containerView.context.getString(R.string.CoinPage_IndicatorMACD)
            ChartIndicator.Rsi -> containerView.context.getString(R.string.CoinPage_IndicatorRSI)
        }
    }

    private fun onIndicatorChanged(indicator: ChartIndicator, checked: Boolean) {
        enabledIndicator = if (checked) indicator else null

        chart.setIndicator(indicator, checked)

        macdIsEnabled = indicator == ChartIndicator.Macd && checked
    }

    private fun setSelectedPoint(item: ChartPointViewItem) {
        pointInfoVolumeTitle.isInvisible = true
        pointInfoVolume.isInvisible = true

        macdHistogram.isInvisible = true
        macdSignal.isInvisible = true
        macdValue.isInvisible = true

        pointInfoDate.text = DateHelper.getDayAndTime(Date(item.date * 1000))
        pointInfoPrice.text =
            getFormattedPointValue(item.price.value, item.price.currency.symbol, chartViewType)

        item.volume?.let {
            pointInfoVolumeTitle.isInvisible = false
            pointInfoVolume.isInvisible = false
            pointInfoVolume.text = formatFiatShortened(item.volume.value, item.volume.currency.symbol)
        }

        item.macdInfo?.let { macdInfo ->
            macdInfo.histogram?.let {
                macdHistogram.isVisible = true
                getHistogramColor(it)?.let { it1 -> macdHistogram.setTextColor(it1) }
                macdHistogram.text = App.numberFormatter.format(it, 0, 2)
            }
            macdInfo.signal?.let {
                macdSignal.isVisible = true
                macdSignal.text = App.numberFormatter.format(it, 0, 2)
            }
            macdInfo.macd?.let {
                macdValue.isVisible = true
                macdValue.text = App.numberFormatter.format(it, 0, 2)
            }
        }
    }

    private fun getFormattedPointValue(
        value: BigDecimal, symbol: String,
        chartViewType: ChartViewType
    ): String {
        return when (chartViewType) {
            ChartViewType.CoinChart -> {
                App.numberFormatter.formatFiat(value, symbol, 2, 4)
            }
            ChartViewType.MarketMetricChart -> {
                val (shortenValue, suffix) = App.numberFormatter.shortenValue(value)
                App.numberFormatter.formatFiat(shortenValue, symbol, 0, 2) + " $suffix"
            }
        }
    }

    private fun formatFiatShortened(value: BigDecimal, symbol: String): String {
        val shortCapValue = App.numberFormatter.shortenValue(value)
        return App.numberFormatter.formatFiat(shortCapValue.first, symbol, 0, 2) + " " + shortCapValue.second
    }

    private fun getHistogramColor(value: Float): Int? {
        val textColor = if (value > 0) R.color.green_d else R.color.red_d
        return containerView.context.getColor(textColor)
    }

    data class IndicatorViewItem(val indicator: ChartIndicator, val checked: Boolean, val enabled: Boolean)

    companion object {

        private fun getActions(chartViewType: ChartViewType): List<Pair<ChartView.ChartType, Int>> {
            return when (chartViewType) {
                ChartViewType.MarketMetricChart -> {
                    listOf(
                        Pair(ChartView.ChartType.DAILY, R.string.CoinPage_TimeDuration_Day),
                        Pair(ChartView.ChartType.WEEKLY, R.string.CoinPage_TimeDuration_Week),
                        Pair(ChartView.ChartType.MONTHLY, R.string.CoinPage_TimeDuration_Month)
                    )
                }
                ChartViewType.CoinChart -> {
                    listOf(
                        Pair(ChartView.ChartType.TODAY, R.string.CoinPage_TimeDuration_Today),
                        Pair(ChartView.ChartType.DAILY, R.string.CoinPage_TimeDuration_Day),
                        Pair(ChartView.ChartType.WEEKLY, R.string.CoinPage_TimeDuration_Week),
                        Pair(ChartView.ChartType.WEEKLY2, R.string.CoinPage_TimeDuration_TwoWeeks),
                        Pair(ChartView.ChartType.MONTHLY, R.string.CoinPage_TimeDuration_Month),
                        Pair(ChartView.ChartType.MONTHLY3, R.string.CoinPage_TimeDuration_Month3),
                        Pair(ChartView.ChartType.MONTHLY6, R.string.CoinPage_TimeDuration_HalfYear),
                        Pair(ChartView.ChartType.MONTHLY12, R.string.CoinPage_TimeDuration_Year),
                        Pair(ChartView.ChartType.MONTHLY24, R.string.CoinPage_TimeDuration_Year2)
                    )
                }
            }
        }
    }
}
