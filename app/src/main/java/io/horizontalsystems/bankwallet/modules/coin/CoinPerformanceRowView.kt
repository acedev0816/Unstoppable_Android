package io.horizontalsystems.bankwallet.modules.coin

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.views.helpers.LayoutHelper
import io.horizontalsystems.xrateskit.entities.TimePeriod
import java.math.BigDecimal

class CoinPerformanceRowView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = HORIZONTAL
    }

    fun bind(item: RoiViewItem) {
        removeAllViews()

        when (item) {
            is RoiViewItem.RowViewItem -> bind(item.title, item.values, item.last)
            is RoiViewItem.HeaderRowViewItem -> bindHeader(item.title, item.periods)
        }
    }

    private fun bindHeader(title: String, periods: List<TimePeriod>) {
        val titleTextView = getHeaderTextView(title).apply {
            setTextAppearance(R.style.Subhead1)
            setTextColor(context.getColor(R.color.oz))
        }

        addView(titleTextView)

        periods.forEach { timePeriod ->
            addView(getSeparatorLine())
            val periodName = getPeriodName(timePeriod)
            addView(getHeaderTextView(periodName))
        }

        setBackgroundResource(R.drawable.rounded_lawrence_background_top)
    }

    private fun bind(title: String, values: List<BigDecimal?>, last: Boolean) {
        addView(getRowTitleTextView(title))

        values.forEach { value ->
            addView(getSeparatorLine())
            addView(getDiffTextView(value))
        }

        setBackgroundResource(getLineBackground(last))
    }

    private fun getSeparatorLine(): View {
        return View(context).apply {
            layoutParams = LayoutParams(LayoutHelper.dp(1f, context), LayoutParams.MATCH_PARENT)
            setBackgroundColor(ContextCompat.getColor(context, R.color.steel_10))
        }
    }

    private fun getHeaderTextView(title: String): TextView {
        return getTextView(title).apply {
            val params = LayoutParams(0, LayoutHelper.dp(44f, context))
            params.weight = 1f
            layoutParams = params
        }
    }

    private fun getRowTitleTextView(title: String): TextView {
        return getTextView(title).apply {
            val params = LayoutParams(0, LayoutHelper.dp(38f, context))
            params.weight = 1f
            layoutParams = params
        }
    }

    private fun getTextView(title: String): TextView {
        return TextView(context).apply {
            gravity = Gravity.CENTER
            setTextColor(context.getColor(R.color.bran))
            setTextAppearance(R.style.Caption)
            text = title
        }
    }

    private fun getDiffTextView(percentage: BigDecimal?): TextView {
        percentage ?: return TextView(context)

        val color = if (percentage >= BigDecimal.ZERO) R.color.remus else R.color.lucian
        val sign = if (percentage >= BigDecimal.ZERO) "+" else "-"

        return TextView(context).apply {
            val params = LayoutParams(0, LayoutParams.WRAP_CONTENT)
            params.weight = 1f
            layoutParams = params
            gravity = Gravity.CENTER
            setTextAppearance(R.style.Caption)
            setTextColor(context.getColor(color))
            text = App.numberFormatter.format(percentage.abs(), 0, 2, prefix = sign, suffix = "%")
        }
    }

    private fun getPeriodName(period: TimePeriod): String {
        return when (period) {
            TimePeriod.DAY_7 -> context.getString(R.string.CoinPage_Performance_Week)
            TimePeriod.DAY_30 -> context.getString(R.string.CoinPage_Performance_Month)
            else -> ""
        }
    }

    private fun getLineBackground(last: Boolean): Int {
        return if (last)
            R.drawable.rounded_steel10_background_bottom
        else
            R.drawable.rounded_steel10_background_middle
    }

}
