package com.spendsense.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class SyncManager(private val expenseDao: ExpenseDao) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val SYNC_TIMEOUT_MS = 15_000L
    }

    /**
     * Uploads all local expenses for the signed-in user to Firestore.
     *
     * IMPORTANT: Must run on Dispatchers.IO. Firestore's gRPC channel is
     * initialised lazily on the first network call. If that happens on the
     * Main thread the SDK hits its own ~3 s internal deadline and throws
     * "Timed out waiting for 3000 ms" before our withTimeout ever fires.
     */
    suspend fun syncWithCloud() = withContext(Dispatchers.IO) {
        val user = auth.currentUser
            ?: throw IllegalStateException("Cannot sync — no authenticated user.")
        val userId = user.uid

        Log.d("SyncManager", "syncWithCloud: starting for uid=$userId")

        val localExpenses = expenseDao.getExpensesSync(userId)
        Log.d("SyncManager", "syncWithCloud: uploading ${localExpenses.size} expense(s)")

        val payload = localExpenses.map { expense ->
            mapOf(
                "id" to expense.id.toLong(),
                "userId" to expense.userId,
                "titleEncrypted" to expense.titleEncrypted,
                "amountEncrypted" to expense.amountEncrypted,
                "category" to expense.category,
                "dayOfMonth" to expense.dayOfMonth.toLong(),
                "month" to expense.month.toLong(),
                "year" to expense.year.toLong(),
                "dayOfWeek" to expense.dayOfWeek.toLong(),
                "timestamp" to expense.timestamp
            )
        }

        withTimeout(SYNC_TIMEOUT_MS) {
            firestore.collection("users").document(userId)
                .collection("expenses")
                .document("backup")
                .set(mapOf("data" to payload))
                .await()
        }

        Log.d("SyncManager", "syncWithCloud: upload complete")
    }

    /**
     * Downloads the cloud backup and replaces the user's local expenses.
     */
    suspend fun restoreFromCloud() = withContext(Dispatchers.IO) {
        val user = auth.currentUser
            ?: throw IllegalStateException("Cannot restore — no authenticated user.")
        val userId = user.uid

        Log.d("SyncManager", "restoreFromCloud: fetching backup for uid=$userId")

        val doc = withTimeout(SYNC_TIMEOUT_MS) {
            firestore.collection("users").document(userId)
                .collection("expenses")
                .document("backup")
                .get()
                .await()
        }

        if (!doc.exists()) {
            Log.d("SyncManager", "restoreFromCloud: no backup document found — skipping restore")
            return@withContext
        }

        val data = doc.get("data") as? List<*>
        if (data.isNullOrEmpty()) {
            Log.d("SyncManager", "restoreFromCloud: no backup data found — skipping restore")
            return@withContext
        }

        val expenses = data.mapNotNull { entry ->
            val map = entry as? Map<*, *> ?: return@mapNotNull null
            try {
                Expense(
                    id = map.numberAsInt("id"),
                    userId = userId,
                    titleEncrypted = map["titleEncrypted"] as String,
                    amountEncrypted = map["amountEncrypted"] as String,
                    category = map["category"] as String,
                    dayOfMonth = map.numberAsInt("dayOfMonth"),
                    month = map.numberAsInt("month"),
                    year = map.numberAsInt("year"),
                    dayOfWeek = map.numberAsInt("dayOfWeek"),
                    timestamp = map.numberAsLong("timestamp")
                )
            } catch (e: Exception) {
                Log.e("SyncManager", "restoreFromCloud: Skipping malformed expense entry", e)
                null
            }
        }

        if (expenses.isEmpty()) {
            Log.d("SyncManager", "restoreFromCloud: No valid expenses in backup — skipping restore")
            return@withContext
        }

        expenseDao.clearAll(userId)
        expenseDao.insertExpenses(expenses)
        Log.d("SyncManager", "restoreFromCloud: restored ${expenses.size} expense(s)")
    }

    private fun Map<*, *>.numberAsInt(key: String): Int = when (val value = this[key]) {
        is Long -> value.toInt()
        is Int -> value
        is Double -> value.toInt()
        else -> throw IllegalArgumentException("Invalid numeric field: $key")
    }

    private fun Map<*, *>.numberAsLong(key: String): Long = when (val value = this[key]) {
        is Long -> value
        is Int -> value.toLong()
        is Double -> value.toLong()
        else -> throw IllegalArgumentException("Invalid numeric field: $key")
    }
}
