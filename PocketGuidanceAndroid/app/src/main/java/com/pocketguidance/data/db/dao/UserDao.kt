package com.pocketguidance.data.db.dao

import androidx.room.*
import com.pocketguidance.data.db.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET avatarPath = :path WHERE id = :userId")
    suspend fun updateAvatar(userId: Long, path: String)

    @Query("UPDATE users SET username = :username WHERE id = :userId")
    suspend fun updateUsername(userId: Long, username: String)
}
