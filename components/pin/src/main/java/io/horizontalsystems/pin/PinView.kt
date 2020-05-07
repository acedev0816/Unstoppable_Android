package io.horizontalsystems.pin

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.MutableLiveData
import io.horizontalsystems.core.SingleLiveEvent
import java.util.*

class PinView : PinModule.IView {

    val toolbar = MutableLiveData<Pair<Int, Boolean>>()
    val addPages = MutableLiveData<List<PinPage>>()
    val showPageAtIndex = MutableLiveData<Int>()
    val showError = MutableLiveData<Int>()
    val updateTopTextForPage = MutableLiveData<Pair<TopText, Int>>()
    val fillPinCircles = MutableLiveData<Pair<Int, Int>>()
    val showCancelButton = MutableLiveData<Boolean>()
    val showFingerprintButton = SingleLiveEvent<Unit>()
    val showFingerprintInput = SingleLiveEvent<BiometricPrompt.CryptoObject>()
    val resetCirclesWithShakeAndDelayForPage = SingleLiveEvent<Int>()
    val showLockedView = SingleLiveEvent<Date>()
    val enablePinInput = SingleLiveEvent<Unit>()


    override fun setToolbar(title: Int, showBackButton: Boolean) {
        this.toolbar.value = Pair(title, showBackButton)
    }

    override fun addPages(pages: List<PinPage>) {
        addPages.value = pages
    }

    override fun showPage(index: Int) {
        showPageAtIndex.value = index
    }

    override fun updateTopTextForPage(text: TopText, pageIndex: Int) {
        updateTopTextForPage.value = Pair(text, pageIndex)
    }

    override fun showError(error: Int) {
        showError.value = error
    }

    override fun showPinWrong(pageIndex: Int) {
        resetCirclesWithShakeAndDelayForPage.value = pageIndex
    }

    override fun showFingerprintButton() {
        showFingerprintButton.call()
    }

    override fun showFingerprintDialog(cryptoObject: BiometricPrompt.CryptoObject) {
        showFingerprintInput.postValue(cryptoObject)
    }

    override fun showCancelButton() {
        showCancelButton.postValue(true)
    }

    override fun fillCircles(length: Int, pageIndex: Int) {
        fillPinCircles.value = Pair(length, pageIndex)
    }

    override fun showLockView(until: Date) {
        showLockedView.postValue(until)
    }

    override fun enablePinInput() {
        enablePinInput.call()
    }
}
