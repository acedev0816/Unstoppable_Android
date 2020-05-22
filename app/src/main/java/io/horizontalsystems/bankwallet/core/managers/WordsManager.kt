package io.horizontalsystems.bankwallet.core.managers

import io.horizontalsystems.bankwallet.core.IWordsManager
import io.horizontalsystems.hdwalletkit.Mnemonic

class WordsManager : IWordsManager {

    private val mnemonic = Mnemonic()

    @Throws(Mnemonic.MnemonicException::class)
    override fun validate(words: List<String>) {
        mnemonic.validate(words)
    }

    override fun generateWords(count: Int): List<String> {
        val strength = when (count) {
            24 -> Mnemonic.Strength.VeryHigh
            else -> Mnemonic.Strength.Default
        }

        return mnemonic.generate(strength)
    }

    override fun generateSeed(words: List<String>): ByteArray {
        return mnemonic.toSeed(words)
    }

}
