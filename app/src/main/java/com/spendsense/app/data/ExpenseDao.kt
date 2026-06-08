package com.spendsense.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllExpenses(userId: String): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    suspend fun getExpensesSync(userId: String): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
