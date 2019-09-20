package io.horizontalsystems.bankwallet.modules.pin.edit

import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.modules.pin.ManagePinPresenter
import io.horizontalsystems.bankwallet.modules.pin.PinModule
import io.horizontalsystems.bankwallet.modules.pin.PinPage
import io.horizontalsystems.bankwallet.modules.pin.TopText

class EditPinPresenter(
        override val view: PinModule.IView,
        val router: EditPinModule.IRouter,
        interactor: PinModule.IInteractor)
    : ManagePinPresenter(view, interactor, pages = listOf(Page.UNLOCK, Page.ENTER, Page.CONFIRM)) {

    override fun viewDidLoad() {
        view.setTitle(R.string.EditPin_Title)
        val pinPages = mutableListOf<PinPage>()

        pages.forEach { page ->
            when (page) {
                Page.UNLOCK -> pinPages.add(PinPage(TopText.Description(R.string.EditPin_UnlockInfo)))
                Page.ENTER -> pinPages.add(PinPage(TopText.Description(R.string.EditPin_NewPinInfo)))
                Page.CONFIRM -> pinPages.add(PinPage(TopText.Description(R.string.SetPin_ConfirmInfo)))
            }
        }
        view.addPages(pinPages)
        view.showBackButton()
    }

    override fun didSavePin() {
        router.dismissModuleWithSuccess()
    }

}
