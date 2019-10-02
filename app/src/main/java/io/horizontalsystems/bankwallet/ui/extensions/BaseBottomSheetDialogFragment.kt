package io.horizontalsystems.bankwallet.ui.extensions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.horizontalsystems.bankwallet.R

open class BaseBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private var content: ViewStub? = null
    private var txtTitle: TextView? = null
    private var txtSubtitle: TextView? = null
    private var headerIcon: ImageView? = null

    override fun getTheme(): Int {
        return R.style.BottomDialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.constraint_layout_with_header, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        content = view.findViewById(R.id.content)
        txtTitle = view.findViewById(R.id.txtTitle)
        txtSubtitle = view.findViewById(R.id.txtSubtitle)
        headerIcon = view.findViewById(R.id.headerIcon)

        view.findViewById<ImageView>(R.id.closeButton)?.setOnClickListener{ dismiss() }
    }

    fun setContentView(@LayoutRes resource: Int) {
        content?.layoutResource = resource
        content?.inflate()
    }

    fun setTitle(title: String?) {
        txtTitle?.text = title
    }

    fun setSubtitle(subtitle: String?) {
        txtSubtitle?.text = subtitle
    }

    fun setHeaderIcon(@DrawableRes resource: Int) {
        headerIcon?.setImageResource(resource)
    }

}
