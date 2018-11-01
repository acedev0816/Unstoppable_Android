package bitcoin.wallet

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import bitcoin.wallet.core.App
import bitcoin.wallet.core.security.EncryptionManager
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    private var failureRunnable: Runnable? = null

    private val queuedRunnables: MutableList<Runnable?> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lightMode = App.localStorage.isLightModeOn
        setTheme(if (lightMode) R.style.LightModeAppTheme else R.style.DarkModeAppTheme)

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTHENTICATE_FOR_ENCRYPTION) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    queuedRunnables.forEach { it?.run() }
                } catch (e: Exception){
                    failureRunnable?.run()
                }
            } else {
                failureRunnable?.run()
            }
            failureRunnable = null
            queuedRunnables.clear()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun safeExecuteWithKeystore(action: Runnable, onSuccess: Runnable? = null, onFailure: Runnable? = null) {
        if (queuedRunnables.isNotEmpty()) {
            queuedRunnables.add(action)
            queuedRunnables.add(onSuccess)
        } else {
            try {
                action.run()
                onSuccess?.run()
            } catch (e: UserNotAuthenticatedException) {
                queuedRunnables.add(action)
                queuedRunnables.add(onSuccess)
                failureRunnable = onFailure
                EncryptionManager.showAuthenticationScreen(this, AUTHENTICATE_FOR_ENCRYPTION)
            } catch (e: KeyPermanentlyInvalidatedException) {
                EncryptionManager.showKeysInvalidatedAlert(this)
            } catch (e: Exception) {
                onFailure?.run()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            super.attachBaseContext(updateBaseContextLocale(it))
        } ?: super.attachBaseContext(newBase)
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val language = App.languageManager.currentLanguage
        Locale.setDefault(language)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResourcesLocale(context, language)
        } else updateResourcesLocaleLegacy(context, language)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResourcesLocale(context: Context , locale: Locale): Context {
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    @SuppressWarnings("deprecation")
    private fun updateResourcesLocaleLegacy(context: Context , locale: Locale): Context {
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    companion object {
        const val AUTHENTICATE_FOR_ENCRYPTION = 23
    }

}
