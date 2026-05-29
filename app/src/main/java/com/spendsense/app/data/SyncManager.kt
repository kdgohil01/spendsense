package com.spendsense.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.spendsense.app.crypto.CryptoManager
import kotlinx.coroutines.tasks.await

class SyncManager(private val expenseDao: ExpenseDao) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun syncWithCloud() {
        val user = auth.currentUser ?: return
        val userId = user.uid

        // 1. Get local data
        val localExpenses = expenseDao.getExpensesSync()

        // 2. Prepare payload (already encrypted by ExpenseRepository before saving to Dao)
        val payload = localExpenses.map { expense ->
            mapOf(
                "id" to expense.id,
                "titleEncrypted" to expense.titleEncrypted,
                "amountEncrypted" to expense.amountEncrypted,
                "category" to expense.category,
                "dayOfMonth" to expense.dayOfMonth,
                "month" to expense.month,
                "year" to expense.year,
                "dayOfWeek" to expense.dayOfWeek,
                "timestamp" to expense.timestamp
            )
        }

        // 3. Upload to Firestore
        firestore.collection("users").document(userId)
            .collection("expenses")
            .document("backup")
            .set(mapOf("data" to payload))
            .await()
    }

    suspend fun restoreFromCloud() {
        val user = auth.currentUser ?: return
        val userId = user.uid

        val doc = firestore.collection("users").document(userId)
            .collection("expenses")
            .document("backup")
            .get()
            .await()

        val data = doc.get("data") as? List<Map<String, Any>> ?: return

        val expenses = data.map { map ->
            Expense(
                id = (map["id"] as Long).toInt(),
                titleEncrypted = map["titleEncrypted"] as String,
                amountEncrypted = map["amountEncrypted"] as String,
                category = map["category"] as String,
                dayOfMonth = (map["dayOfMonth"] as Long).toInt(),
                month = (map["month"] as Long).toInt(),
                year = (map["year"] as Long).toInt(),
                dayOfWeek = (map["dayOfWeek"] as Long).toInt(),
                timestamp = map["timestamp"] as Long
            )
        }

        expenseDao.clearAll()
        expenseDao.insertExpenses(expenses)
    }
}
