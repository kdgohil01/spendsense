package com.spendsense.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncManager(private val expenseDao: ExpenseDao) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Uploads all local expenses for the signed-in user to Firestore.
     *
     * IMPORTANT: Must run on Dispatchers.IO. Firestore's gRPC channel is
     * initialised lazily on the first network call. If that happens on the
     * Main thread the SDK hits its own ~3 s internal deadline and throws
     * "Timed out waiting for 3000 ms" before our withTimeout ever fires.
     * withContext(Dispatchers.IO) fixes this completely.
     */
    suspend fun syncWithCloud() = withContext(Dispatchers.IO) {
        val user = auth.currentUser
            ?: throw IllegalStateException("Cannot sync — no authenticated user.")
        val userId = user.uid

        Log.d("SyncManager", "syncWithCloud: starting for uid=$userId")

        // 1. Get local data scoped to this user
        val localExpenses = expenseDao.getExpensesSync(userId)
        Log.d("SyncManager", "syncWithCloud: uploading ${localExpenses.size} expense(s)")

        // 2. Prepare plain-map payload (encrypted strings travel as-is)
        val payload = localExpenses.map { expense ->
            mapOf(
                "id"               to expense.id,
                "userId"           to expense.userId,
                "titleEncrypted"   to expense.titleEncrypted,
                "amountEncrypted"  to expense.amountEncrypted,
                "category"         to expense.category,
                "dayOfMonth"       to expense.dayOfMonth,
                "month"            to expense.month,
                "year"             to expense.year,
                "dayOfWeek"        to expense.dayOfWeek,
                "timestamp"        to expense.timestamp
            )
        }

        // 3. Upload — await() suspends without blocking the IO thread
        firestore.collection("users").document(userId)
            .collection("expenses")
            .document("backup")
            .set(mapOf("data" to payload))
            .await()

        Log.d("SyncManager", "syncWithCloud: upload complete")
    }

    /**
     * Downloads the cloud backup and replaces the user's local expenses.
     * Also runs on Dispatchers.IO for the same gRPC-channel reason as above.
     */
    suspend fun restoreFromCloud() = withContext(Dispatchers.IO) {
        val user = auth.currentUser
            ?: throw IllegalStateException("Cannot restore — no authenticated user.")
        val userId = user.uid

        Log.d("SyncManager", "restoreFromCloud: fetching backup for uid=$userId")

        val doc = firestore.collection("users").document(userId)
            .collection("expenses")
            .document("backup")
            .get()
            .await()

        val data = doc.get("data") as? List<Map<String, Any>>
        if (data == null) {
            Log.d("SyncManager", "restoreFromCloud: no backup data found — skipping restore")
            return@withContext
        }

        val expenses = data.map { map ->
            Expense(
                id              = (map["id"]         as Long).toInt(),
                userId          = userId,
                titleEncrypted  = map["titleEncrypted"]  as String,
                amountEncrypted = map["amountEncrypted"] as String,
                category        = map["category"]        as String,
                dayOfMonth      = (map["dayOfMonth"]  as Long).toInt(),
                month           = (map["month"]       as Long).toInt(),
                year            = (map["year"]        as Long).toInt(),
                dayOfWeek       = (map["dayOfWeek"]   as Long).toInt(),
                timestamp       = map["timestamp"]    as Long
            )
        }

        // Replace this user's local data with the restored set
        expenseDao.clearAll(userId)
        expenseDao.insertExpenses(expenses)
        Log.d("SyncManager", "restoreFromCloud: restored ${expenses.size} expense(s)")
    }
}
