package com.pocketguidance.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "badges",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("badgeType", unique = true)]
)
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val badgeType: String,      // e.g., "SAVER_LEVEL_1", "CONSISTENT_LOGGING"
    val name: String,
    val description: String,
    val icon: String,           // Emoji or resource name
    val dateEarned: Long = System.currentTimeMillis()
)
