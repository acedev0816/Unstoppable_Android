package io.horizontalsystems.pin

import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import kotlinx.android.parcel.Parcelize
import java.util.*

object PinModule {

    const val RESULT_OK = 1
    const val RESULT_CANCELLED = 2
    const val PIN_COUNT = 6

    const val keyInteractionType = "interaction_type"
    const val keyShowCancel = "show_cancel"
    const val requestKey = "pin_request_key"
    const val requestType = "pin_request_type"
    const val requestResult = "pin_request_result"

    interface IView {
        fun setToolbar(title: Int, showBackButton: Boolean)
        fun addPages(pages: List<PinPage>)
        fun showPage(index: Int)
        fun updateTopTextForPage(text: TopText, pageIndex: Int)
        fun showError(error: Int)
        fun showPinWrong(pageIndex: Int)
        fun showCancelButton()
        fun fillCircles(length: Int, pageIndex: Int)
        fun showBiometricAuthDialog(cryptoObject: BiometricPrompt.CryptoObject)
        fun showLockView(until: Date)
        fun enablePinInput()
        fun showBiometricAuthButton()
    }

    interface IViewDelegate {
        fun viewDidLoad()
        fun onEnter(pin: String, pageIndex: Int)
        fun onDelete(pageIndex: Int)
        fun onBiometricsUnlock() {}
        fun showBiometricAuthButton() {}
        fun showBiometricAuthInput() {}
        fun resetPin()
    }

    interface IInteractor {
        fun set(pin: String?)
        fun validate(pin: String): Boolean
        fun save(pin: String)
        fun unlock(pin: String): Boolean
    }

    interface IInteractorDelegate {
        fun didSavePin()
        fun didFailToSavePin()
    }

    fun startForSetPin(): PinFragment {
        return PinFragment.start(PinInteractionType.SET_PIN, true)
    }

    fun startForEditPin(): PinFragment {
        return PinFragment.start(PinInteractionType.EDIT_PIN, false)
    }

    fun startForUnlock(): PinFragment {
        return PinFragment.start(PinInteractionType.UNLOCK, true)
    }

}

sealed class TopText(open val text: Int) {
    class Title(override val text: Int) : TopText(text)
    class BigError(override val text: Int) : TopText(text)
    class Description(override val text: Int) : TopText(text)
    class SmallError(override val text: Int) : TopText(text)
}

@Parcelize
enum class PinInteractionType : Parcelable {
    SET_PIN,
    UNLOCK,
    EDIT_PIN
}
