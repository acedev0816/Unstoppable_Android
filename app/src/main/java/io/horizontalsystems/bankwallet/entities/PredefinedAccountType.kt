package io.horizontalsystems.bankwallet.entities

import io.horizontalsystems.bankwallet.R

sealed class PredefinedAccountType {
    object Standard : PredefinedAccountType()
    object Eos : PredefinedAccountType()
    object Binance : PredefinedAccountType()

    val title: Int
        get() = when (this) {
            is Standard -> R.string.AccountType_Unstoppable
            is Eos -> R.string.AccountType_Eos
            is Binance -> R.string.AccountType_Binance
        }

    val coinCodes: Int
        get() = when (this) {
            is Standard -> R.string.AccountType_Unstoppable_Text
            is Eos -> R.string.AccountType_Eos_Text
            is Binance -> R.string.AccountType_Binance_Text
        }

    fun supports(accountType: AccountType): Boolean {
        when (this) {
            is Standard -> {
                if (accountType is AccountType.Mnemonic) {
                    return accountType.words.size == 12
                }
            }
            is Eos -> {
                return accountType is AccountType.Eos
            }
            is Binance -> {
                if (accountType is AccountType.Mnemonic) {
                    return accountType.words.size == 24
                }
            }
        }

        return false
    }

    override fun toString(): String {
        return when (this) {
            is Standard -> STANDARD
            is Eos -> EOS
            is Binance -> BINANCE
        }
    }

    companion object{
        const val STANDARD = "standard"
        const val EOS = "eos"
        const val BINANCE = "binance"

        fun fromString(string: String?): PredefinedAccountType? {
            return when (string) {
                STANDARD -> Standard
                EOS -> Eos
                BINANCE -> Binance
                else -> null
            }
        }
    }
}
