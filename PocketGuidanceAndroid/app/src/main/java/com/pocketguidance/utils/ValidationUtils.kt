package com.pocketguidance.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPassword(password: String): Boolean =
        password.length >= 6

    fun isValidUsername(username: String): Boolean =
        username.trim().length >= 2

    fun isValidAmount(input: String): Boolean {
        val d = input.toDoubleOrNull() ?: return false
        return d > 0
    }

    fun isValidDate(date: String): Boolean =
        date.matches(Regex("""\d{4}-\d{2}-\d{2}"""))

    fun parseAmount(input: String): Double? {
        val d = input.trim().toDoubleOrNull() ?: return null
        return if (d > 0) d else null
    }
}
