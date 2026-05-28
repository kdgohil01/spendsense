package com.spendsense.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titleEncrypted: String,
    val amountEncrypted: String,
    val category: String, // e.g. "Food", "Bills", "Groceries", "Leisure", "Travel", "Other"
    val dayOfMonth: Int,  // e.g. 1 to 31
    val month: Int,       // e.g. 1 to 12
    val year: Int,        // e.g. 2026
    val dayOfWeek: Int,   // e.g. 1 (Monday) to 7 (Sunday)
    val timestamp: Long = System.currentTimeMillis()
)
