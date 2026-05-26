package com.pocketguidance.data.db.dao

import androidx.room.*
import com.pocketguidance.data.db.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY category ASC")
    fun getAllForUser(userId: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category LIMIT 1")
    suspend fun findByCategory(userId: Long, category: String): BudgetEntity?

    @Query("UPDATE budgets SET spent = spent + :amount WHERE userId = :userId AND category = :category")
    suspend fun addSpent(userId: Long, category: String, amount: Double)

    @Query("UPDATE budgets SET spent = 0 WHERE userId = :userId")
    suspend fun resetAllSpent(userId: Long)
}
