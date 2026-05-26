package com.pocketguidance.data.repository

import android.util.Log
import com.pocketguidance.data.db.AppDatabase
import com.pocketguidance.data.db.dao.CategoryTotal
import com.pocketguidance.data.db.entities.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FinanceRepository(private val db: AppDatabase) {

    private val TAG = "FinanceRepository"

    // ── Default categories seeded for every new user ─────────────────────────
    val defaultCategories = listOf(
        "Food", "Transport", "Rent", "Entertainment",
        "Shopping", "Health", "Education", "Utilities", "Other"
    )

    // ── Transactions ──────────────────────────────────────────────────────────

    suspend fun addTransaction(tx: TransactionEntity): Long {
        Log.d(TAG, "Adding transaction: type=${tx.type}, amount=${tx.amount}, cat=${tx.category}")
        val id = db.transactionDao().insert(tx)
        if (tx.type == "expense") {
            db.budgetDao().addSpent(tx.userId, tx.category, tx.amount)
            Log.d(TAG, "Updated budget spent for category=${tx.category}")
        }
        return id
    }

    fun getAllTransactions(userId: Long): Flow<List<TransactionEntity>> =
        db.transactionDao().getAllForUser(userId)

    fun getTransactionsByDateRange(userId: Long, start: String, end: String): Flow<List<TransactionEntity>> =
        db.transactionDao().getByDateRange(userId, start, end)

    fun getExpenses(userId: Long): Flow<List<TransactionEntity>> =
        db.transactionDao().getExpenses(userId)

    fun getExpensesByDateRange(userId: Long, start: String, end: String): Flow<List<TransactionEntity>> =
        db.transactionDao().getExpensesByDateRange(userId, start, end)

    suspend fun getTotalIncome(userId: Long): Double =
        db.transactionDao().getTotalIncome(userId) ?: 0.0

    suspend fun getTotalExpenses(userId: Long): Double =
        db.transactionDao().getTotalExpenses(userId) ?: 0.0

    suspend fun getCategoryBreakdown(userId: Long): List<CategoryTotal> =
        db.transactionDao().getExpensesByCategory(userId)

    suspend fun getCategoryBreakdownInRange(userId: Long, start: String, end: String): List<CategoryTotal> =
        db.transactionDao().getExpensesByCategoryInRange(userId, start, end)

    suspend fun getRecentTransactions(userId: Long, limit: Int = 5): List<TransactionEntity> =
        db.transactionDao().getRecent(userId, limit)

    suspend fun updateReceiptPhoto(txId: Long, path: String) {
        db.transactionDao().updateReceiptPhoto(txId, path)
    }

    // ── Budgets ───────────────────────────────────────────────────────────────

    suspend fun addBudget(budget: BudgetEntity): Long {
        Log.d(TAG, "Adding budget: category=${budget.category}, limit=${budget.limitAmount}")
        return db.budgetDao().insert(budget)
    }

    fun getBudgets(userId: Long): Flow<List<BudgetEntity>> =
        db.budgetDao().getAllForUser(userId)

    suspend fun updateBudget(budget: BudgetEntity) = db.budgetDao().update(budget)

    suspend fun deleteBudget(budget: BudgetEntity) = db.budgetDao().delete(budget)

    // ── Goals ─────────────────────────────────────────────────────────────────

    suspend fun addGoal(goal: GoalEntity): Long {
        Log.d(TAG, "Adding goal: name=${goal.name}, target=${goal.targetAmount}")
        return db.goalDao().insert(goal)
    }

    fun getGoals(userId: Long): Flow<List<GoalEntity>> =
        db.goalDao().getAllForUser(userId)

    suspend fun contributeToGoal(goalId: Long, amount: Double, userId: Long, goalName: String) {
        Log.d(TAG, "Contributing R$amount to goal $goalId ($goalName)")
        db.goalDao().addContribution(goalId, amount)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        db.transactionDao().insert(
            TransactionEntity(
                userId = userId,
                type = "goal_contribution",
                amount = amount,
                category = "Savings",
                description = "Contribution to $goalName",
                date = today
            )
        )
    }

    // ── Categories ────────────────────────────────────────────────────────────

    suspend fun seedDefaultCategories(userId: Long) {
        val entities = defaultCategories.map { CategoryEntity(userId = userId, name = it, isCustom = false) }
        db.categoryDao().insertAll(entities)
        Log.d(TAG, "Seeded ${entities.size} default categories for userId=$userId")
    }

    fun getCategories(userId: Long): Flow<List<CategoryEntity>> =
        db.categoryDao().getAllForUser(userId)

    suspend fun getCategoriesOnce(userId: Long): List<CategoryEntity> =
        db.categoryDao().getAllForUserOnce(userId)

    suspend fun addCustomCategory(userId: Long, name: String): Boolean {
        val existing = db.categoryDao().findByName(userId, name)
        if (existing != null) {
            Log.w(TAG, "Category '$name' already exists for userId=$userId")
            return false
        }
        db.categoryDao().insert(CategoryEntity(userId = userId, name = name, isCustom = true))
        Log.d(TAG, "Added custom category: $name")
        return true
    }

    suspend fun deleteCustomCategory(userId: Long, name: String) {
        db.categoryDao().deleteByName(userId, name)
        Log.d(TAG, "Deleted custom category: $name")
    }

    // ── User Prefs ────────────────────────────────────────────────────────────

    fun getUserPrefs(userId: Long): Flow<UserPrefsEntity?> =
        db.userPrefsDao().getForUser(userId)

    suspend fun getUserPrefsOnce(userId: Long): UserPrefsEntity? =
        db.userPrefsDao().getForUserOnce(userId)

    suspend fun createDefaultPrefs(userId: Long) {
        db.userPrefsDao().insert(UserPrefsEntity(userId = userId))
    }

    suspend fun updateMonthlyIncome(userId: Long, income: Double) {
        db.userPrefsDao().updateMonthlyIncome(userId, income)
    }

    suspend fun updateBudgetGoal(userId: Long, goal: Double) {
        db.userPrefsDao().updateBudgetGoal(userId, goal)
    }

    suspend fun updateSpendingGoals(userId: Long, min: Double, max: Double) {
        db.userPrefsDao().updateSpendingGoals(userId, min, max)
    }

    suspend fun updateCurrency(userId: Long, currency: String) {
        db.userPrefsDao().updateCurrency(userId, currency)
    }

    suspend fun markOnboarded(userId: Long) {
        db.userPrefsDao().markOnboarded(userId)
    }

    // ── Gamification ──────────────────────────────────────────────────────────

    fun getBadges(userId: Long): Flow<List<BadgeEntity>> =
        db.badgeDao().getForUser(userId)

    suspend fun awardBadge(userId: Long, type: String, name: String, desc: String, icon: String) {
        if (!db.badgeDao().hasBadge(userId, type)) {
            db.badgeDao().insert(BadgeEntity(userId = userId, badgeType = type, name = name, description = desc, icon = icon))
            Log.d(TAG, "Badge awarded: $name for userId=$userId")
        }
    }

    suspend fun checkAndAwardBadges(userId: Long) {
        // Achievement: First Transaction
        val count = db.transactionDao().getRecent(userId, 1).size
        if (count > 0) {
            awardBadge(userId, "FIRST_ENTRY", "Welcome Aboard!", "Logged your first transaction.", "🚀")
        }

        // Achievement: High Spender (more than 10 transactions)
        val allExpenses = db.transactionDao().getRecent(userId, 11).size
        if (allExpenses > 10) {
            awardBadge(userId, "ACTIVE_USER", "Data Collector", "Logged more than 10 transactions.", "📊")
        }

        // Achievement: Budget Master (stayed under budget this month)
        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val totalSpent = db.transactionDao().getTotalExpensesInRange(userId, "$currentMonth-01", "$currentMonth-31") ?: 0.0
        val prefs = db.userPrefsDao().getForUserOnce(userId)
        if (prefs != null && prefs.maxMonthlySpendingGoal > 0 && totalSpent < prefs.maxMonthlySpendingGoal) {
            awardBadge(userId, "BUDGET_MASTER", "Budget Master", "Stayed under your max spending goal this month.", "🏆")
        }
    }

    suspend fun completeOnboarding(userId: Long, monthlyIncome: Double) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        // Record first salary transaction
        db.transactionDao().insert(
            TransactionEntity(
                userId = userId,
                type = "income",
                amount = monthlyIncome,
                category = "Salary",
                description = "Monthly salary",
                date = today
            )
        )
        db.userPrefsDao().updateMonthlyIncome(userId, monthlyIncome)
        db.userPrefsDao().updateBudgetGoal(userId, monthlyIncome)
        db.userPrefsDao().markOnboarded(userId)
        Log.d(TAG, "Onboarding completed for userId=$userId, income=$monthlyIncome")
    }
}
