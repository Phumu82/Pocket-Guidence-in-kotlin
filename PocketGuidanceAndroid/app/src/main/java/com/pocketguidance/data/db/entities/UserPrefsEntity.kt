package com.pocketguidance.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_prefs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId", unique = true)]
)
data class UserPrefsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val currency: String = "R",
    val monthlyIncome: Double = 0.0,
    val monthlyBudgetGoal: Double = 0.0,
    val minMonthlySpendingGoal: Double = 0.0,
    val maxMonthlySpendingGoal: Double = 0.0,
    val onboarded: Boolean = false,
    val darkMode: Boolean = false
)
