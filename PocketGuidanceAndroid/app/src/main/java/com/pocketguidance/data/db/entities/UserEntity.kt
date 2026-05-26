package com.pocketguidance.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,          // stored lowercase; unique
    val username: String,
    val passwordHash: String,   // SHA-256 hex of password
    val avatarPath: String? = null
)
