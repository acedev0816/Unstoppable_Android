package io.horizontalsystems.bankwallet.modules.send.submodules.fee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.entities.Coin
import io.horizontalsystems.bankwallet.entities.FeeRateInfo
import io.horizontalsystems.bankwallet.modules.send.SendPresenter
import io.horizontalsystems.bankwallet.viewHelpers.DateHelper
import io.horizontalsystems.bankwallet.viewHelpers.TextHelper
import kotlinx.android.synthetic.main.view_send_fee.*

class SendFeeFragment(private val feeIsAdjustable: Boolean,
                      private val coin: Coin,
                      private val moduleDelegate: SendPresenter)
    : Fragment(), FeeRatePrioritySelector.Listener {

    private var presenter: SendFeePresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_send_fee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        presenter = ViewModelProviders.of(this, SendFeeModule.Factory(coin, moduleDelegate))
                .get(SendFeePresenter::class.java)
        val presenterView = presenter?.view as SendFeeView

        txSpeedLayout.visibility = if (feeIsAdjustable) View.VISIBLE else View.GONE
        txSpeedLayout.setOnClickListener {
            presenter?.onClickFeeRatePriority()
        }

        presenterView.primaryFee.observe(viewLifecycleOwner, Observer { txFeePrimary.text = " $it" })

        presenterView.secondaryFee.observe(viewLifecycleOwner, Observer { fiatFee ->
            fiatFee?.let { txFeeSecondary.text = " | $it" }
        })

        presenterView.duration.observe(viewLifecycleOwner, Observer { duration ->
            context?.let {
                val txDurationString = DateHelper.getTxDurationString(it, duration)
                txDuration.text = it.getString(R.string.Duration_Within, txDurationString)
            }
        })

        presenterView.feePriority.observe(viewLifecycleOwner, Observer { feePriority ->
            context?.let {
                txSpeedMenu.text = TextHelper.getFeeRatePriorityString(it, feePriority)
            }
        })

        presenterView.showFeePriorityOptions.observe(viewLifecycleOwner, Observer { feeRates ->
            FeeRatePrioritySelector
                    .newInstance(this, feeRates)
                    .show(this.requireFragmentManager(), "fee_rate_priority_selector")
        })


        presenterView.insufficientFeeBalanceError.observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                feeError.visibility = View.VISIBLE
                txSpeedLayout.visibility = View.GONE
                feeLayout.visibility = View.GONE

                val coinCode = error.coin.code
                val tokenProtocol = error.coinProtocol
                val feeCoinTitle = error.feeCoin.title
                val formattedFee = App.numberFormatter.format(error.fee)

                feeError.text = context?.getString(R.string.Send_Token_InsufficientFeeAlert, coinCode, tokenProtocol,
                                                   feeCoinTitle, formattedFee)
            } else {
                feeError.visibility = View.GONE
                txSpeedLayout.visibility = if (feeIsAdjustable) View.VISIBLE else View.GONE
                feeLayout.visibility = View.VISIBLE
            }
        })

        presenter?.onViewDidLoad()
    }

    override fun onSelectFeeRate(feeRate: FeeRateInfo) {
        presenter?.onChangeFeeRate(feeRate)
    }
}
