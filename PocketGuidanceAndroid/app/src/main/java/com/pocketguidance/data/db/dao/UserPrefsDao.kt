package com.pocketguidance.data.db.dao

import androidx.room.*
import com.pocketguidance.data.db.entities.UserPrefsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPrefsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prefs: UserPrefsEntity)

    @Update
    suspend fun update(prefs: UserPrefsEntity)

    @Query("SELECT * FROM user_prefs WHERE userId = :userId LIMIT 1")
    fun getForUser(userId: Long): Flow<UserPrefsEntity?>

    @Query("SELECT * FROM user_prefs WHERE userId = :userId LIMIT 1")
    suspend fun getForUserOnce(userId: Long): UserPrefsEntity?

    @Query("UPDATE user_prefs SET monthlyIncome = :income WHERE userId = :userId")
    suspend fun updateMonthlyIncome(userId: Long, income: Double)

    @Query("UPDATE user_prefs SET monthlyBudgetGoal = :goal WHERE userId = :userId")
    suspend fun updateBudgetGoal(userId: Long, goal: Double)

    @Query("UPDATE user_prefs SET minMonthlySpendingGoal = :min, maxMonthlySpendingGoal = :max WHERE userId = :userId")
    suspend fun updateSpendingGoals(userId: Long, min: Double, max: Double)

    @Query("UPDATE user_prefs SET currency = :currency WHERE userId = :userId")
    suspend fun updateCurrency(userId: Long, currency: String)

    @Query("UPDATE user_prefs SET onboarded = 1 WHERE userId = :userId")
    suspend fun markOnboarded(userId: Long)

    @Query("UPDATE user_prefs SET darkMode = :dark WHERE userId = :userId")
    suspend fun updateDarkMode(userId: Long, dark: Boolean)
}
