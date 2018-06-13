package org.grouvi.wallet.modules.backupWords

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import org.grouvi.wallet.R
import org.grouvi.wallet.SingleLiveEvent

class BackupWordsViewModel : ViewModel(), BackupWordsModule.IView, BackupWordsModule.IRouter {
    override lateinit var presenter: BackupWordsModule.IPresenter

    val errorLiveData = MutableLiveData<Int>()
    val wordsLiveData = MutableLiveData<List<String>>()
    val wordIndexesToConfirmLiveData = MutableLiveData<List<Int>>()

    val navigationWordsLiveEvent = SingleLiveEvent<Void>()
    val navigationConfirmLiveEvent = SingleLiveEvent<Void>()
    val closeLiveEvent = SingleLiveEvent<Void>()
    val navigateBackLiveEvent = SingleLiveEvent<Void>()
    val navigateToMainLiveEvent = SingleLiveEvent<Void>()

    var dismissMode = BackupWordsModule.DismissMode.TO_MAIN

    fun init(dismissMode: BackupWordsModule.DismissMode) {
        BackupWordsModule.init(this, this)
        Log.e("AAA", "Yahoo, $presenter")

        this.dismissMode = dismissMode
    }

    // view

    override fun showWords(words: List<String>) {
        wordsLiveData.value = words
        navigationWordsLiveEvent.call()
    }

    override fun showConfirmationWithIndexes(indexes: List<Int>) {
        wordIndexesToConfirmLiveData.value = indexes
        navigationConfirmLiveEvent.call()
    }

    override fun hideWords() {
        navigateBackLiveEvent.call()
    }

    override fun hideConfirmation() {
        errorLiveData.value = null
        navigateBackLiveEvent.call()
    }

    override fun showConfirmationError() {
        errorLiveData.value = R.string.error
    }

    // router

    override fun close() {
        when (dismissMode) {
            BackupWordsModule.DismissMode.TO_MAIN -> navigateToMainLiveEvent.call()
            BackupWordsModule.DismissMode.DISMISS_SELF -> closeLiveEvent.call()
        }

    }
}