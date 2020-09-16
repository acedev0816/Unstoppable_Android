package io.horizontalsystems.bankwallet.modules.lockscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.setFragmentResultListener
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.BaseActivity
import io.horizontalsystems.bankwallet.modules.ratelist.RatesListFragment
import io.horizontalsystems.bankwallet.modules.ratelist.RatesTopListFragment
import io.horizontalsystems.pin.PinFragment
import io.horizontalsystems.pin.PinInteractionType
import io.horizontalsystems.pin.PinModule
import kotlinx.android.synthetic.main.activity_lockscreen.*

class LockScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lockscreen)

        val fragments = listOf(
                PinFragment(),
                RatesListFragment(),
                RatesTopListFragment()
        )

        viewPager.offscreenPageLimit = 2
        viewPager.adapter = LockScreenViewPagerAdapter(fragments, supportFragmentManager)

        circleIndicator.setViewPager(viewPager)
        subscribeFragmentResult()
    }

    private fun subscribeFragmentResult() {
        supportFragmentManager.setFragmentResultListener(PinModule.requestKey, this) { requestKey, bundle ->
            val resultType = bundle.getParcelable<PinInteractionType>(PinModule.requestType)
            if (resultType == PinInteractionType.UNLOCK) {
                when (bundle.getInt(PinModule.requestResult)) {
                    PinModule.RESULT_OK -> setResult(PinModule.RESULT_OK)
                    PinModule.RESULT_CANCELLED -> setResult(PinModule.RESULT_CANCELLED)
                }

                finish()
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    companion object {
        fun startForResult(context: Activity, requestCode: Int = 0) {
            val intent = Intent(context, LockScreenActivity::class.java)
            context.startActivityForResult(intent, requestCode)
        }
    }
}
