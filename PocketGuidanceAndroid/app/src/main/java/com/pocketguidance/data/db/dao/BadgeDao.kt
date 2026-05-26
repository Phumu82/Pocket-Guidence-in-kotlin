package com.pocketguidance.data.db.dao

import androidx.room.*
import com.pocketguidance.data.db.entities.BadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(badge: BadgeEntity): Long

    @Query("SELECT * FROM badges WHERE userId = :userId")
    fun getForUser(userId: Long): Flow<List<BadgeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM badges WHERE userId = :userId AND badgeType = :type)")
    suspend fun hasBadge(userId: Long, type: String): Boolean
}
