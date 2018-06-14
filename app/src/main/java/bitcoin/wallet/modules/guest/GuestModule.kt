package bitcoin.wallet.modules.guest

import android.content.Context
import android.content.Intent
import bitcoin.wallet.core.managers.Factory

object GuestModule {

    interface IViewDelegate {
        fun createWalletDidClick()
        fun restoreWalletDidClick()
    }

    interface IInteractor {
        fun createWallet()
    }

    interface IInteractorDelegate {
        fun didCreateWallet()
    }

    interface IRouter {
        fun navigateToBackupRoutingToMain()
        fun navigateToRestore()
    }

    fun start(context: Context) {
        val intent = Intent(context, GuestActivity::class.java)
        context.startActivity(intent)
    }

    fun init(view: GuestViewModel, router: IRouter) {
        val interactor = GuestInteractor(Factory.mnemonicManager, Factory.preferencesManager)
        val presenter = GuestPresenter()

        view.delegate = presenter

        interactor.delegate = presenter

        presenter.interactor = interactor
        presenter.router = router
    }

}

