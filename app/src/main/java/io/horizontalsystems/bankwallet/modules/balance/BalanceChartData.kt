package io.horizontalsystems.bankwallet.modules.balance

import io.horizontalsystems.bankwallet.lib.chartview.models.ChartPoint
import java.math.BigDecimal

data class BalanceChartData(val points: List<ChartPoint>, val diff: BigDecimal)
