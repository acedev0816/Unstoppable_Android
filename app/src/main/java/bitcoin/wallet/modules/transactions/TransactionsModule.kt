package bitcoin.wallet.modules.transactions

import bitcoin.wallet.core.managers.DatabaseManager
import bitcoin.wallet.core.managers.Factory

object TransactionsModule {

    interface IView {
        fun showTransactionItems(items: List<TransactionRecordViewItem>)
    }

    interface IViewDelegate {
        fun viewDidLoad()
    }

    interface IInteractor {
        fun retrieveTransactionRecords()
    }

    interface IInteractorDelegate {
        fun didRetrieveTransactionRecords(items: List<TransactionRecordViewItem>)
    }

    interface IRouter

    private var databaseManager: DatabaseManager? = null

    fun initModule(view: TransactionsViewModel, router: IRouter) {
        val databaseManager = Factory.databaseManager

        val interactor = TransactionsInteractor(databaseManager, Factory.coinManager)
        val presenter = TransactionsPresenter(interactor, router)

        presenter.view = view
        interactor.delegate = presenter
        view.delegate = presenter

        this.databaseManager = databaseManager
    }

}
