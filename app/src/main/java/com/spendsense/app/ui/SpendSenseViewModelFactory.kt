package com.spendsense.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spendsense.app.data.ExpenseRepository
import com.spendsense.app.data.SyncManager

class SpendSenseViewModelFactory(
    private val repository: ExpenseRepository,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpendSenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpendSenseViewModel(repository, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
