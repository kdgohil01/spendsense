package com.spendsense.app.data

import com.spendsense.app.crypto.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DecryptedExpense(
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val dayOfMonth: Int,
    val month: Int,
    val year: Int,
    val dayOfWeek: Int,
    val timestamp: Long
)

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getAllExpensesDecrypted(userId: String): Flow<List<DecryptedExpense>> = expenseDao.getAllExpenses(userId)
        .map { list ->
            list.map { expense ->
                val decryptedTitle = CryptoManager.decrypt(expense.titleEncrypted)
                val decryptedAmountStr = CryptoManager.decrypt(expense.amountEncrypted)
                val decryptedAmount = decryptedAmountStr.toDoubleOrNull() ?: 0.0
                DecryptedExpense(
                    id = expense.id,
                    title = decryptedTitle,
                    amount = decryptedAmount,
                    category = expense.category,
                    dayOfMonth = expense.dayOfMonth,
                    month = expense.month,
                    year = expense.year,
                    dayOfWeek = expense.dayOfWeek,
                    timestamp = expense.timestamp
                )
            }
        }

    suspend fun insertExpense(userId: String, title: String, amount: Double, category: String, dayOfMonth: Int, month: Int, year: Int, dayOfWeek: Int) {
        val encryptedTitle = CryptoManager.encrypt(title)
        val encryptedAmount = CryptoManager.encrypt(amount.toString())
        val expense = Expense(
            userId = userId,
            titleEncrypted = encryptedTitle,
            amountEncrypted = encryptedAmount,
            category = category,
            dayOfMonth = dayOfMonth,
            month = month,
            year = year,
            dayOfWeek = dayOfWeek
        )
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(userId: String, id: Int, category: String, dayOfMonth: Int, month: Int, year: Int, dayOfWeek: Int) {
        // Find dummy or reconstruct standard
        val expense = Expense(
            id = id,
            userId = userId,
            titleEncrypted = "",
            amountEncrypted = "",
            category = category,
            dayOfMonth = dayOfMonth,
            month = month,
            year = year,
            dayOfWeek = dayOfWeek
        )
        expenseDao.deleteExpense(expense)
    }

    suspend fun getEncryptedBackupPayload(userId: String): List<Expense> {
        return expenseDao.getExpensesSync(userId)
    }

    suspend fun restoreFromBackupPayload(userId: String, expenses: List<Expense>) {
        expenseDao.clearAll(userId)
        expenseDao.insertExpenses(expenses)
    }
}
