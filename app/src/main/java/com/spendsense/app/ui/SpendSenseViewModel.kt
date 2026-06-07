package com.spendsense.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendsense.app.data.DecryptedExpense
import com.spendsense.app.data.ExpenseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SpendSenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

    // General app states
    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser.asStateFlow()

    val isGoogleSignedIn: StateFlow<Boolean> = _currentUser.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), auth.currentUser != null)

    val showAuthScreen: StateFlow<Boolean> = combine(_currentUser, _isGuestMode) { user, guest ->
        user == null && !guest
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), auth.currentUser == null)

    private val _selectedTab = MutableStateFlow(0) // 0 for Weekly View, 1 for Monthly View
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedWeekDay = MutableStateFlow(getCurrentDayOfWeek()) // 1 (Mon) .. 7 (Sun)
    val selectedWeekDay: StateFlow<Int> = _selectedWeekDay.asStateFlow()

    private val _selectedMonthDay = MutableStateFlow(getCurrentDayOfMonth()) // 1 .. 31
    val selectedMonthDay: StateFlow<Int> = _selectedMonthDay.asStateFlow()

    private val _budgetLimit = MutableStateFlow(800.0) // Monthly/weekly budget setting
    val budgetLimit: StateFlow<Double> = _budgetLimit.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("Never synced")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()
    
    private val _signInTrigger = MutableStateFlow(false)
    val signInTrigger: StateFlow<Boolean> = _signInTrigger.asStateFlow()

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
        
        // Listen for Auth changes
        auth.addAuthStateListener {
            val user = it.currentUser
            _currentUser.value = user
            android.util.Log.d("SpendSenseAuth", "Auth state changed. User: ${user?.email ?: "Signed out"}")
        }
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

    fun enterAsGuest() {
        _isGuestMode.value = true
    }

    // Google Sign-In trigger
    fun signInGoogleAccount() {
        viewModelScope.launch {
            _isSyncing.value = true
            _signInTrigger.value = true
        }
    }
    
    fun onSignInSuccess() {
        _isSyncing.value = false
        _signInTrigger.value = false
        _currentUser.value = auth.currentUser
        android.util.Log.d("SpendSenseAuth", "Sign-in success handler called. User: ${auth.currentUser?.email}")
    }
    
    fun onSignInFailed() {
        _isSyncing.value = false
        _signInTrigger.value = false
        android.util.Log.e("SpendSenseAuth", "Sign-in failed handler called")
    }

    fun signOutGoogleAccount() {
        android.util.Log.d("SpendSenseAuth", "Signing out user: ${auth.currentUser?.email}")
        auth.signOut()
        _currentUser.value = null
        _isGuestMode.value = false
        _lastSyncTime.value = "Never synced"
    }

    // Seamless Encrypted Cloud Backup & Restore simulation
    fun triggerCloudSync() {
        if (_currentUser.value == null) return
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
