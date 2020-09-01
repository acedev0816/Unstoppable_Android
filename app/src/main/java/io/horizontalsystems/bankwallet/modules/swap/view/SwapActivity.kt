package io.horizontalsystems.bankwallet.modules.swap.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.BaseActivity
import io.horizontalsystems.bankwallet.core.setOnSingleClickListener
import io.horizontalsystems.bankwallet.entities.Coin
import io.horizontalsystems.bankwallet.modules.swap.SwapModule
import io.horizontalsystems.bankwallet.modules.swap.coinselect.SelectSwapCoinModule
import io.horizontalsystems.bankwallet.modules.swap.confirmation.SwapConfirmationFragment
import io.horizontalsystems.bankwallet.modules.swap.view.item.TradeViewItem
import io.horizontalsystems.core.helpers.HudHelper
import kotlinx.android.synthetic.main.activity_swap.*
import java.math.BigDecimal

class SwapActivity : BaseActivity() {

    lateinit var viewModel: SwapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swap)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(getDrawable(R.drawable.ic_info))
        supportActionBar?.title = getString(R.string.Swap)

        val coinSending = intent.extras?.getParcelable<Coin>(SwapModule.tokenInKey)
        viewModel = ViewModelProvider(this, SwapModule.Factory(coinSending!!)).get(SwapViewModel::class.java)

        fromAmount.apply {
            onTokenButtonClick {
                SelectSwapCoinModule.start(this@SwapActivity, requestSelectFromCoin, true, viewModel.coinReceiving.value)
            }

            editText.addTextChangedListener(fromAmountListener)
        }

        toAmount.apply {
            onTokenButtonClick {
                SelectSwapCoinModule.start(this@SwapActivity, requestSelectToCoin, false, viewModel.coinSending.value)
            }

            editText.addTextChangedListener(toAmountListener)
        }

        proceedButton.setOnSingleClickListener {
            // open confirmation module
            viewModel.onProceedClick()
        }

        approveButton.setOnSingleClickListener {
            // open approve module
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.proceedButtonVisible.observe(this, Observer { proceedButtonVisible ->
            Log.e("AAA", "proceedButtonVisible: $proceedButtonVisible")
            proceedButton.isVisible = proceedButtonVisible
        })

        viewModel.proceedButtonEnabled.observe(this, Observer { proceedButtonEnabled ->
            Log.e("AAA", "proceedButtonEnabled: $proceedButtonEnabled")
            proceedButton.isEnabled = proceedButtonEnabled
        })

        viewModel.approveButtonVisible.observe(this, Observer { approveButtonVisible ->
            Log.e("AAA", "approveButtonVisible: $approveButtonVisible")
            approveButton.isVisible = approveButtonVisible
        })

        viewModel.openConfirmation.observe(this, Observer { requireConfirmation ->
            Log.e("AAA", "requireConfirmation: $requireConfirmation")
            if (requireConfirmation) {
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right,
                                R.anim.slide_in_from_right, R.anim.slide_out_to_right)
                        .add(R.id.rootView, SwapConfirmationFragment())
                        .addToBackStack("confirmFragment")
                        .commit()
            }
        })

        viewModel.coinSending.observe(this, Observer { coin ->
            Log.e("AAA", "coinSending: $coin")
            fromAmount.setSelectedCoin(coin?.code)
        })

        viewModel.coinReceiving.observe(this, Observer { coin ->
            Log.e("AAA", "coinReceiving: $coin")
            toAmount.setSelectedCoin(coin?.code)
        })

        viewModel.amountSending.observe(this, Observer { amount ->
            Log.e("AAA", "amountSending: $amount")
            setAmountSendingIfChanged(amount)
        })

        viewModel.amountReceiving.observe(this, Observer { amount ->
            Log.e("AAA", "amountReceiving: $amount")
            setAmountReceivingIfChanged(amount)
        })

        viewModel.balance.observe(this, Observer { balance ->
            Log.e("AAA", "balance: $balance")
            availableBalanceValue.text = balance
        })

        viewModel.amountSendingError.observe(this, Observer { amountSendingError ->
            Log.e("AAA", "amountSendingError: $amountSendingError")
            fromAmount.setError(amountSendingError)
        })

        viewModel.amountSendingLabelVisible.observe(this, Observer { isVisible ->
            Log.e("AAA", "amountSendingLabelVisible: $isVisible")
            fromAmountLabel.isVisible = isVisible
        })

        viewModel.amountReceivingLabelVisible.observe(this, Observer { isVisible ->
            Log.e("AAA", "amountReceivingLabelVisible: $isVisible")
            toAmountLabel.isVisible = isVisible
        })

        viewModel.tradeViewItem.observe(this, Observer { tradeViewItem ->
            Log.e("AAA", "tradeViewItem: $tradeViewItem")
            setTradeViewItem(tradeViewItem)
        })

        viewModel.tradeViewItemLoading.observe(this, Observer { isLoading ->
            Log.e("AAA", "tradeViewItemLoading: $isLoading")
            tradeViewItemProgressBar.isVisible = isLoading
        })

        viewModel.feeLoading.observe(this, Observer { isLoading ->
            Log.e("AAA", "feeLoading: $isLoading")
            feeProgressBar.isVisible = isLoading
        })

        viewModel.allowance.observe(this, Observer { allowance ->
            Log.e("AAA", "allowance: $allowance")
            setAllowance(allowance)
        })

        viewModel.allowanceLoading.observe(this, Observer { isLoading ->
            Log.e("AAA", "allowanceLoading: $isLoading")
            setAllowanceLoading(isLoading)
        })

        viewModel.allowanceColor.observe(this, Observer { color ->
            Log.e("AAA", "allowanceColor: $color")
            allowanceValue.setTextColor(color)
        })

        viewModel.priceImpactColor.observe(this, Observer { color ->
            Log.e("AAA", "priceImpactColor: $color")
            priceImpactValue.setTextColor(color)
        })

        viewModel.error.observe(this, Observer { error ->
            Log.e("AAA", "error: $error")
            commonError.text = error
            commonError.isVisible = error != null
        })

        viewModel.closeWithSuccess.observe(this, Observer {
            HudHelper.showSuccessMessage(findViewById(android.R.id.content), it)
            finish()
        })

        viewModel.closeWithError.observe(this, Observer {
            HudHelper.showErrorMessage(findViewById(android.R.id.content), it)
            finish()
        })
    }

    private fun setAllowance(allowance: String?) {
        allowanceValue.text = allowance
        val isVisible = allowance != null
        allowanceTitle.isVisible = isVisible
        allowanceValue.isVisible = isVisible
    }

    private fun setAllowanceLoading(isLoading: Boolean) {
        allowanceTitle.isVisible = allowanceTitle.isVisible || isLoading
        allowanceProgressBar.isVisible = isLoading
        allowanceValue.isVisible = !isLoading
    }

    private fun setTradeViewItem(tradeViewItem: TradeViewItem?) {
        priceValue.text = tradeViewItem?.price
        priceImpactValue.text = tradeViewItem?.priceImpact
        minMaxTitle.text = tradeViewItem?.minMaxTitle
        minMaxValue.text = tradeViewItem?.minMaxAmount

        setTradeViewItemVisibility(visible = tradeViewItem != null)
    }

    private fun setTradeViewItemVisibility(visible: Boolean) {
        priceTitle.isVisible = visible
        priceValue.isVisible = visible
        priceImpactTitle.isVisible = visible
        priceImpactValue.isVisible = visible
        minMaxTitle.isVisible = visible
        minMaxValue.isVisible = visible
    }

    private val fromAmountListener = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.setAmountSending(s?.toString())
        }

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    }

    private val toAmountListener = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.setAmountReceiving(s?.toString())
        }

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    }

    private fun setAmountSendingIfChanged(amount: String?) {
        fromAmount.editText.apply {
            if (amountsEqual(text?.toString()?.toBigDecimalOrNull(), amount?.toBigDecimalOrNull())) return

            removeTextChangedListener(fromAmountListener)
            setText(amount)
            addTextChangedListener(fromAmountListener)
        }
    }

    private fun setAmountReceivingIfChanged(amount: String?) {
        toAmount.editText.apply {
            if (amountsEqual(text?.toString()?.toBigDecimalOrNull(), amount?.toBigDecimalOrNull())) return

            removeTextChangedListener(toAmountListener)
            setText(amount)
            addTextChangedListener(toAmountListener)
        }
    }

    private fun amountsEqual(amount1: BigDecimal?, amount2: BigDecimal?): Boolean {
        return when {
            amount1 == null && amount2 == null -> true
            amount1 != null && amount2 != null && amount2.compareTo(amount1) == 0 -> true
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.swap_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuCancel -> {
                finish()
                return true
            }
            android.R.id.home -> {
                UniswapInfoActivity.start(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val selectedCoin = data?.getParcelableExtra<Coin>(SelectSwapCoinModule.selectedCoinKey)
                    ?: return
            when (requestCode) {
                requestSelectFromCoin -> viewModel.setCoinSending(selectedCoin)
                requestSelectToCoin -> viewModel.setCoinReceiving(selectedCoin)
            }
        }
    }

    companion object {
        const val requestSelectFromCoin = 0
        const val requestSelectToCoin = 1
    }

}
