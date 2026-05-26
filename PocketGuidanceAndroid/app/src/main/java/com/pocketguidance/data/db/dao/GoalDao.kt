package com.pocketguidance.data.db.dao

import androidx.room.*
import com.pocketguidance.data.db.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY completed ASC, deadline ASC")
    fun getAllForUser(userId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): GoalEntity?

    @Query("""
        UPDATE goals 
        SET currentAmount = currentAmount + :amount,
            completed = CASE WHEN currentAmount + :amount >= targetAmount THEN 1 ELSE 0 END
        WHERE id = :goalId
    """)
    suspend fun addContribution(goalId: Long, amount: Double)
}
