package com.spendsense.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendsense.app.data.DecryptedExpense
import com.spendsense.app.data.ExpenseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.*

class SpendSenseViewModel(
    private val repository: ExpenseRepository,
    private val syncManager: com.spendsense.app.data.SyncManager
) : ViewModel() {

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

    // Mutex ensures only one Firestore sync runs at a time (prevents write conflicts
    // when addExpense / deleteExpense fire back-to-back rapid calls).
    private val syncMutex = Mutex()
    
    private val _signInTrigger = MutableStateFlow(false)
    val signInTrigger: StateFlow<Boolean> = _signInTrigger.asStateFlow()

    // Retrieve active month/year parameters dynamically
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-indexed
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonthName: String = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())

    val currentUserId: StateFlow<String> = combine(_currentUser, _isGuestMode) { user, guest ->
        when {
            user != null -> user.uid
            guest -> "guest"
            else -> "guest"
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "guest")

    // All raw decrypted expenses collected dynamically from the safe repository
    @OptIn(ExperimentalCoroutinesApi::class)
    val rawExpenses: StateFlow<List<DecryptedExpense>> = currentUserId
        .flatMapLatest { userId ->
            repository.getAllExpensesDecrypted(userId)
        }
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
        
        // Auto-restore from cloud on successful sign in
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                syncManager.restoreFromCloud()
                android.util.Log.d("SpendSenseAuth", "Cloud restore completed successfully on sign-in")
            } catch (e: Exception) {
                android.util.Log.e("SpendSenseAuth", "Failed to restore from cloud on sign-in", e)
            } finally {
                _isSyncing.value = false
            }
        }
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

    // Seamless Encrypted Cloud Backup & Restore
    fun triggerCloudSync() {
        if (_currentUser.value == null) return
        // If a sync is already in flight, skip — the mutex will still serialise
        // any queued calls so we never fire two Firestore writes simultaneously.
        if (syncMutex.isLocked) return
        viewModelScope.launch {
            syncMutex.withLock {
                _isSyncing.value = true
                try {
                    syncManager.syncWithCloud()
                    val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    _lastSyncTime.value = formatter.format(Date())
                    Log.d("SpendSenseSync", "Cloud sync successful")
                } catch (e: Exception) {
                    Log.e("SpendSenseSync", "Sync failed", e)
                    // Provide a clean, readable status rather than the raw exception string
                    _lastSyncTime.value = when {
                        e is kotlinx.coroutines.TimeoutCancellationException ->
                            "Failed: request timed out — check your connection"
                        e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                            "Failed: Firestore permission denied — check security rules"
                        e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                            "Failed: server unavailable — will retry next time"
                        else -> "Failed: ${e.localizedMessage ?: e.message ?: e.toString()}"
                    }
                } finally {
                    _isSyncing.value = false
                }
            }
        }
    }

    // Expenses database actions
    fun addExpense(title: String, amount: Double, category: String, dayOfMonth: Int, dayOfWeek: Int) {
        viewModelScope.launch {
            repository.insertExpense(
                userId = currentUserId.value,
                title = title,
                amount = amount,
                category = category,
                dayOfMonth = dayOfMonth,
                month = currentMonth,
                year = currentYear,
                dayOfWeek = dayOfWeek
            )
            // Trigger sync AFTER the DB write completes, from the same coroutine
            // (avoids the nested-launch race that caused duplicate _isSyncing toggles).
            triggerCloudSync()
        }
    }

    fun deleteExpense(id: Int, category: String, dayOfMonth: Int, dayOfWeek: Int) {
        viewModelScope.launch {
            repository.deleteExpense(currentUserId.value, id, category, dayOfMonth, currentMonth, currentYear, dayOfWeek)
            // Same fix: trigger sync after the delete is committed.
            triggerCloudSync()
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

class SpendSenseViewModelFactory(
    private val repository: ExpenseRepository,
    private val syncManager: com.spendsense.app.data.SyncManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpendSenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpendSenseViewModel(repository, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
