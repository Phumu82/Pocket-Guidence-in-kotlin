package com.pocketguidance.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.pocketguidance.PocketGuidanceApp
import com.pocketguidance.utils.SessionManager

abstract class BaseActivity : AppCompatActivity() {

    protected val app get() = application as PocketGuidanceApp
    protected val financeRepo get() = app.financeRepository
    protected val authRepo get() = app.authRepository

    protected val userId: Long get() = SessionManager.getUserId(this)

    protected fun navigateTo(cls: Class<*>, finish: Boolean = false) {
        startActivity(Intent(this, cls))
        if (finish) finish()
    }

    protected fun requireLogin() {
        if (!SessionManager.isLoggedIn(this)) {
            navigateTo(LoginActivity::class.java, finish = true)
        }
    }
}
