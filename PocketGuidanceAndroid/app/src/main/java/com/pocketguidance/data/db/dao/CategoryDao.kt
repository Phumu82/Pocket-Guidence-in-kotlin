package com.pocketguidance.data.db.dao

import androidx.room.*
import com.pocketguidance.data.db.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY isCustom ASC, name ASC")
    fun getAllForUser(userId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY isCustom ASC, name ASC")
    suspend fun getAllForUserOnce(userId: Long): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE userId = :userId AND name = :name LIMIT 1")
    suspend fun findByName(userId: Long, name: String): CategoryEntity?

    @Query("DELETE FROM categories WHERE userId = :userId AND name = :name AND isCustom = 1")
    suspend fun deleteByName(userId: Long, name: String)
}
