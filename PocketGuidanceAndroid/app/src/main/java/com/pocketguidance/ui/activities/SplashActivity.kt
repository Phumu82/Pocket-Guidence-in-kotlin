package com.pocketguidance.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.pocketguidance.utils.SessionManager

/**
 * SplashActivity — entry point of the app.
 *
 * Uses the AndroidX SplashScreen API (core-splashscreen 1.0.1) which:
 *  • On API 31+  → delegates entirely to the system SplashScreen (zero extra layout)
 *  • On API <31  → emulates the same look via the compat library
 *
 * Pattern:
 *  1. Call installSplashScreen() BEFORE super.onCreate
 *  2. setKeepOnScreenCondition keeps splash visible during async work
 *  3. Navigate to the correct destination — splash exits automatically
 *
 * NO layout file is used — the SplashScreen API handles all visuals.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {

        // ── MUST be called before super.onCreate ─────────────────────────────
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        // No setContentView — splash screen IS the UI at this point

        Log.d(TAG, "SplashActivity created — checking session")

        // Session check is synchronous (SharedPreferences), so we don't need
        // to hold the splash on screen — let it dismiss immediately.
        splashScreen.setKeepOnScreenCondition { false }

        // ── Decide destination ────────────────────────────────────────────────
        if (SessionManager.isLoggedIn(this)) {
            Log.i(TAG, "Session found → Dashboard")
            navigateTo(DashboardActivity::class.java, finish = true)
        } else {
            Log.i(TAG, "No session → Login")
            navigateTo(LoginActivity::class.java, finish = true)
        }
    }
}
