package com.pocketguidance.data.repository

import android.util.Log
import com.pocketguidance.data.db.AppDatabase
import com.pocketguidance.data.db.entities.UserEntity
import java.security.MessageDigest

class AuthRepository(private val db: AppDatabase) {

    private val TAG = "AuthRepository"

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** Returns the UserId on success, null on failure. */
    suspend fun login(email: String, password: String): Long? {
        val user = db.userDao().findByEmail(email.lowercase().trim())
        if (user == null) {
            Log.w(TAG, "Login failed: email not found")
            return null
        }
        if (user.passwordHash != sha256(password)) {
            Log.w(TAG, "Login failed: wrong password for ${email}")
            return null
        }
        Log.i(TAG, "Login success for userId=${user.id}")
        return user.id
    }

    /** Returns userId on success, null if email already taken. */
    suspend fun signup(email: String, password: String, username: String): Long? {
        val existing = db.userDao().findByEmail(email.lowercase().trim())
        if (existing != null) {
            Log.w(TAG, "Signup failed: email already registered")
            return null
        }
        val user = UserEntity(
            email = email.lowercase().trim(),
            username = username.trim(),
            passwordHash = sha256(password)
        )
        val id = db.userDao().insert(user)
        Log.i(TAG, "Signup success: userId=$id")
        return id
    }

    suspend fun getUserById(id: Long): UserEntity? = db.userDao().findById(id)

    suspend fun updateUsername(userId: Long, username: String) {
        db.userDao().updateUsername(userId, username)
        Log.d(TAG, "Updated username for userId=$userId")
    }

    suspend fun updateAvatar(userId: Long, path: String) {
        db.userDao().updateAvatar(userId, path)
        Log.d(TAG, "Updated avatar path for userId=$userId")
    }
}
