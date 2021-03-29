package io.horizontalsystems.bankwallet.modules.swap.tradeoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.core.providers.Translator
import io.horizontalsystems.bankwallet.modules.swap.SwapTradeService
import io.horizontalsystems.coinkit.models.CoinType

object SwapTradeOptionsModule {

    class Factory(private val tradeService: SwapTradeService) : ViewModelProvider.Factory {

        private val service by lazy { SwapTradeOptionsService(tradeService.tradeOptions) }
        private val translator by lazy { Translator }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val ethereumCoin = App.coinManager.getCoin(CoinType.Ethereum) ?: throw IllegalArgumentException()

            return when (modelClass) {
                SwapTradeOptionsViewModel::class.java -> SwapTradeOptionsViewModel(service, tradeService, translator) as T
                SwapDeadlineViewModel::class.java -> SwapDeadlineViewModel(service, translator) as T
                SwapSlippageViewModel::class.java -> SwapSlippageViewModel(service) as T
                RecipientAddressViewModel::class.java -> {
                    val addressParser = App.addressParserFactory.parser(ethereumCoin)
                    val resolutionService = AddressResolutionService(ethereumCoin.code, true)
                    val placeholder = translator.string(R.string.SwapSettings_RecipientPlaceholder)
                    RecipientAddressViewModel(service, resolutionService, addressParser, placeholder, listOf(service, resolutionService)) as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }
}
