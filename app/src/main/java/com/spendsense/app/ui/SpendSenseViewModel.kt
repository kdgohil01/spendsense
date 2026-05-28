package com.spendsense.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendsense.app.data.DecryptedExpense
import com.spendsense.app.data.ExpenseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SpendSenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // General app states
    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0 for Weekly View, 1 for Monthly View
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedWeekDay = MutableStateFlow(getCurrentDayOfWeek()) // 1 (Mon) .. 7 (Sun)
    val selectedWeekDay: StateFlow<Int> = _selectedWeekDay.asStateFlow()

    private val _selectedMonthDay = MutableStateFlow(getCurrentDayOfMonth()) // 1 .. 31
    val selectedMonthDay: StateFlow<Int> = _selectedMonthDay.asStateFlow()

    private val _budgetLimit = MutableStateFlow(800.0) // Monthly/weekly budget setting
    val budgetLimit: StateFlow<Double> = _budgetLimit.asStateFlow()

    // Google synchronization simulated states
    private val _isGoogleSignedIn = MutableStateFlow(false)
    val isGoogleSignedIn: StateFlow<Boolean> = _isGoogleSignedIn.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("Never synced")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    // Retrieve active month/year parameters dynamically
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-indexed
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonthName: String = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())

    // All raw decrypted expenses collected dynamically from the safe repository
    val rawExpenses: StateFlow<List<DecryptedExpense>> = repository.allExpensesDecrypted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Run splash screen animation pause
        viewModelScope.launch {
            delay(1800)
            _showSplash.value = false
        }
        
        // Populate sample data if DB is empty to offer premium first-launch visual feedback
        /* viewModelScope.launch {
            rawExpenses.collect { list ->
                if (list.isEmpty()) {
                    populateSmartSampleData()
                }
            }
        } */
    }

    // Interactive navigation setters
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectWeekDay(day: Int) {
        _selectedWeekDay.value = day
    }

    fun selectMonthDay(day: Int) {
        _selectedMonthDay.value = day
    }

    fun updateBudgetLimit(newLimit: Double) {
        if (newLimit > 0.0) {
            _budgetLimit.value = newLimit
        }
    }

    // Google Sign-In trigger
    fun signInGoogleAccount() {
        viewModelScope.launch {
            _isSyncing.value = true
            delay(1000) // Realistic interactive delay
            _isGoogleSignedIn.value = true
            _isSyncing.value = false
            triggerCloudSync()
        }
    }

    fun signOutGoogleAccount() {
        _isGoogleSignedIn.value = false
        _lastSyncTime.value = "Never synced"
    }

    // Seamless Encrypted Cloud Backup & Restore simulation
    fun triggerCloudSync() {
        if (!_isGoogleSignedIn.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            delay(1500) // Simulates generating payload, encrypting and uploading to private Drive directory
            val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            _lastSyncTime.value = formatter.format(Date())
            _isSyncing.value = false
        }
    }

    // Expenses database actions
    fun addExpense(title: String, amount: Double, category: String, dayOfMonth: Int, dayOfWeek: Int) {
        viewModelScope.launch {
            repository.insertExpense(
                title = title,
                amount = amount,
                category = category,
                dayOfMonth = dayOfMonth,
                month = currentMonth,
                year = currentYear,
                dayOfWeek = dayOfWeek
            )
            triggerCloudSync() // Auto-save backup when internet is on (seamless cloud synchronisation)
        }
    }

    fun deleteExpense(id: Int, category: String, dayOfMonth: Int, dayOfWeek: Int) {
        viewModelScope.launch {
            repository.deleteExpense(id, category, dayOfMonth, currentMonth, currentYear, dayOfWeek)
            triggerCloudSync() // Auto backup updates
        }
    }

    // Helper functions for dates representation
    private fun getCurrentDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        // Convert to standard 1 (Monday) to 7 (Sunday) format
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        return when (day) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    private fun getCurrentDayOfMonth(): Int {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    }

    private suspend fun populateSmartSampleData() {
        val todayCalendar = Calendar.getInstance()
        val currentDay = todayCalendar.get(Calendar.DAY_OF_MONTH)
        
        // Add a few beautiful mock entries to prevent initial launch visual blank space
        repository.insertExpense("Organic Groceries", 45.50, "Groceries", currentDay, currentMonth, currentYear, getCurrentDayOfWeek())
        repository.insertExpense("Netflix Subscription", 15.49, "Bills", Math.max(1, currentDay - 2), currentMonth, currentYear, Math.max(1, getCurrentDayOfWeek() - 2))
        repository.insertExpense("Double Matcha Latte", 6.75, "Food", currentDay, currentMonth, currentYear, getCurrentDayOfWeek())
        repository.insertExpense("Gasoline Refill", 32.20, "Travel", Math.max(1, currentDay - 4), currentMonth, currentYear, Math.max(1, getCurrentDayOfWeek() - 4))
        repository.insertExpense("Cinema Ticket", 12.00, "Leisure", Math.max(1, currentDay - 1), currentMonth, currentYear, Math.max(1, getCurrentDayOfWeek() - 1))
    }
}

class SpendSenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpendSenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpendSenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
