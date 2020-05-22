package io.horizontalsystems.bankwallet.core.managers

import io.horizontalsystems.bankwallet.core.*
import io.horizontalsystems.bankwallet.entities.Account
import io.horizontalsystems.bankwallet.entities.AccountOrigin
import io.horizontalsystems.bankwallet.entities.AccountType
import io.horizontalsystems.bankwallet.entities.PredefinedAccountType
import java.util.*

class AccountCreator(
        private val wordsManager: IWordsManager
) : IAccountCreator {

    override fun newAccount(predefinedAccountType: PredefinedAccountType): Account {
        val accountType = accountType(predefinedAccountType)
        return createAccount(accountType, AccountOrigin.Created, false)
    }

    override fun restoredAccount(accountType: AccountType): Account {
        return createAccount(accountType, AccountOrigin.Restored, true)
    }

    private fun accountType(predefinedAccountType: PredefinedAccountType): AccountType {
        return when (predefinedAccountType) {
            is PredefinedAccountType.Standard -> createMnemonicAccountType(12)
            is PredefinedAccountType.Binance -> createMnemonicAccountType(24)
            is PredefinedAccountType.Eos -> throw EosUnsupportedException()
        }
    }

    private fun createMnemonicAccountType(wordsCount: Int): AccountType {
        val words = wordsManager.generateWords(wordsCount)
        return AccountType.Mnemonic(words)
    }

    private fun createAccount(accountType: AccountType, origin: AccountOrigin, backedUp: Boolean): Account {
        val id = UUID.randomUUID().toString()
        if (accountType is AccountType.Mnemonic) {
            accountType.seed = wordsManager.generateSeed(accountType.words).toHexString()
        }
        return Account(
                id = id,
                name = id,
                type = accountType,
                origin = origin,
                isBackedUp = backedUp
        )
    }

}
