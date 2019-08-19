package io.horizontalsystems.bankwallet.modules.send.sendviews.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.modules.send.SendViewModel
import io.horizontalsystems.bankwallet.modules.send.sendviews.confirmation.subviews.ConfirmationMemoView
import io.horizontalsystems.bankwallet.modules.send.sendviews.confirmation.subviews.ConfirmationPrimaryView
import io.horizontalsystems.bankwallet.modules.send.sendviews.confirmation.subviews.ConfirmationSecondaryDataView
import io.horizontalsystems.bankwallet.modules.send.sendviews.confirmation.subviews.ConfirmationSendButtonView
import io.horizontalsystems.bankwallet.ui.extensions.TopMenuItem
import io.horizontalsystems.bankwallet.viewHelpers.HudHelper
import kotlinx.android.synthetic.main.fragment_confirmation.*

class ConfirmationFragment : Fragment() {

    private var sendViewModel: SendViewModel? = null
    private var confirmationViewModel: SendConfirmationViewModel? = null
    private var memoViewItem: ConfirmationMemoView? = null
    private var sendButtonView: ConfirmationSendButtonView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shadowlessToolbar.bind(
                title = getString(R.string.Send_Confirmation_Title),
                leftBtnItem = TopMenuItem(R.drawable.back) {
                    activity?.onBackPressed()
                }
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            sendViewModel = ViewModelProviders.of(it).get(SendViewModel::class.java)
        }

        confirmationViewModel = ViewModelProviders.of(this).get(SendConfirmationViewModel::class.java)

        sendViewModel?.sendConfirmationLiveData?.observe(viewLifecycleOwner, Observer {
            confirmationViewModel?.init(it)
        })

        confirmationViewModel?.addPrimaryDataViewItem?.observe(viewLifecycleOwner, Observer { primaryViewItem ->
            context?.let {
                val primaryItemView = ConfirmationPrimaryView(it)
                primaryItemView.bind(primaryViewItem) { confirmationViewModel?.delegate?.onReceiverClick() }
                confirmationLinearLayout.addView(primaryItemView)
            }
        })

        confirmationViewModel?.addMemoViewItem?.observe(viewLifecycleOwner, Observer {
            context?.let {
                memoViewItem = ConfirmationMemoView(it)
                confirmationLinearLayout.addView(memoViewItem)
            }
        })

        confirmationViewModel?.showCopied?.observe(viewLifecycleOwner, Observer {
            HudHelper.showSuccessMessage(R.string.Hud_Text_Copied, 500)
        })

        confirmationViewModel?.addSecondaryDataViewItem?.observe(viewLifecycleOwner, Observer { secondaryData ->
            context?.let {
                val secondaryDataView = ConfirmationSecondaryDataView(it)
                secondaryDataView.bind(secondaryData)
                confirmationLinearLayout.addView(secondaryDataView)
            }
        })

        confirmationViewModel?.addSendButton?.observe(viewLifecycleOwner, Observer {
            context?.let {
                sendButtonView = ConfirmationSendButtonView(it)
                sendButtonView?.setOnClickListener {
                    confirmationViewModel?.delegate?.onSendClick(memoViewItem?.getMemo())
                }

                confirmationLinearLayout.addView(sendButtonView)
            }
        })

        confirmationViewModel?.sendWithMemo?.observe(viewLifecycleOwner, Observer { memo ->
            sendViewModel?.delegate?.onSendConfirmed(memo)
        })

        sendViewModel?.errorLiveData?.observe(viewLifecycleOwner, Observer { errorMsgTextRes ->
            errorMsgTextRes?.let { HudHelper.showErrorMessage(it) }
            confirmationViewModel?.delegate?.onSendError()
        })

        confirmationViewModel?.sendButtonState?.observe(viewLifecycleOwner, Observer { state ->
            sendButtonView?.bind(state)
        })

    }

}
