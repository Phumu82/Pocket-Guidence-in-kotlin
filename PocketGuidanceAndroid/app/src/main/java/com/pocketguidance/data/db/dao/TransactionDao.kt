package com.pocketguidance.data.db.dao

import androidx.room.*
import com.pocketguidance.data.db.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getAllForUser(userId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getByDateRange(userId: Long, startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId AND type = 'expense' 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getExpenses(userId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId AND type = 'expense' AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getExpensesByDateRange(
        userId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId AND type = 'expense' AND category = :category 
        ORDER BY date DESC
    """)
    fun getExpensesByCategory(userId: Long, category: String): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'expense'")
    suspend fun getTotalExpenses(userId: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'expense' AND date BETWEEN :start AND :end")
    suspend fun getTotalExpensesInRange(userId: Long, start: String, end: String): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'income'")
    suspend fun getTotalIncome(userId: Long): Double?

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE userId = :userId AND type = 'expense' 
        GROUP BY category 
        ORDER BY total DESC
    """)
    suspend fun getExpensesByCategory(userId: Long): List<CategoryTotal>

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE userId = :userId AND type = 'expense' AND date BETWEEN :startDate AND :endDate
        GROUP BY category 
        ORDER BY total DESC
    """)
    suspend fun getExpensesByCategoryInRange(
        userId: Long, startDate: String, endDate: String
    ): List<CategoryTotal>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, createdAt DESC LIMIT :limit")
    suspend fun getRecent(userId: Long, limit: Int = 5): List<TransactionEntity>

    @Query("UPDATE transactions SET receiptPhotoPath = :path WHERE id = :txId")
    suspend fun updateReceiptPhoto(txId: Long, path: String)
}

data class CategoryTotal(val category: String, val total: Double)
