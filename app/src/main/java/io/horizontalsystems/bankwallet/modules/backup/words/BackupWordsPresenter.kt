package io.horizontalsystems.bankwallet.modules.backup.words

import java.util.*

class BackupWordsPresenter(private val interactor: BackupWordsModule.IInteractor, private val router: BackupWordsModule.IRouter, private val state: BackupWordsModule.State)
    : BackupWordsModule.IPresenter, BackupWordsModule.IViewDelegate, BackupWordsModule.IInteractorDelegate {

    //  IView

    override var view: BackupWordsModule.IView? = null

    //  IView delegate

    override fun viewDidLoad() {
        view?.showWords(state.words)
        loadCurrentPage()
    }

    override fun onNextClick() {
        if (state.canLoadNextPage()) {
            if (state.currentPage == 2) {
                if (state.backedUp) {
                    router.notifyClosed()
                    return
                }

                view?.showConfirmationWords(interactor.getConfirmationIndices())
            }

            loadCurrentPage()
        } else {
            view?.validateWords()
        }
    }

    override fun onBackClick() {
        if (state.canLoadPrevPage()) {
            loadCurrentPage()
        } else {
            router.close()
        }
    }

    override fun validateDidClick(confirmationWords: HashMap<Int, String>) {
        interactor.validate(confirmationWords)
    }

    // IInteractor Delegate

    override fun onValidateSuccess() {
        router.notifyBackedUp()
    }

    override fun onValidateFailure() {
        view?.showConfirmationError()
    }

    // Private

    private fun loadCurrentPage() {
        view?.loadPage(state.currentPage)
    }
}

