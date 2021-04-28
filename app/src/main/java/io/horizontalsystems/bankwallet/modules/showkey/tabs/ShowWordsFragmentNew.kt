package io.horizontalsystems.bankwallet.modules.showkey.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.BaseFragment
import kotlinx.android.synthetic.main.fragment_show_words_new.*

open class ShowWordsFragmentNew : BaseFragment() {
    private val words: List<String>
        get() = requireArguments().getStringArrayList(WORDS) ?: listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_words_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mnemonicPhraseView.populateWords(words)
    }

    companion object {
        private const val WORDS = "words"
        private const val PASSPHRASE = "passphrase"

        fun getInstance(words: List<String>, passphrase: String): ShowWordsFragmentNew {
            val fragment = ShowWordsFragmentNew()
            val arguments = bundleOf(WORDS to ArrayList(words), PASSPHRASE to passphrase)
            fragment.arguments = arguments
            return fragment
        }
    }

}
