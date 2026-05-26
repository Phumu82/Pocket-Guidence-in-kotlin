package com.pocketguidance.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mirrors the TypeScript Transaction interface.
 * type values: "income" | "expense" | "goal_contribution" | "settlement" | "received"
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("date"), Index("category")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val type: String,           // "income" | "expense" | "goal_contribution" | "settlement" | "received"
    val amount: Double,
    val category: String,
    val description: String,
    val date: String,           // ISO yyyy-MM-dd
    val receiptPhotoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
