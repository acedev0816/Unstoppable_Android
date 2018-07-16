package bitcoin.wallet.lib

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import bitcoin.wallet.R
import kotlinx.android.synthetic.main.dialog_error.*

class ErrorDialog(context: Context, val text: Int) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_error)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        textView.setText(text)
    }

}
