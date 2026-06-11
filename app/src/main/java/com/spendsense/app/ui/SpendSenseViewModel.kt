package com.spendsense.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import com.spendsense.app.data.DecryptedExpense
import com.spendsense.app.data.ExpenseRepository
import com.spendsense.app.data.SyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class SpendSenseViewModel(
    private val repository: ExpenseRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val authListener = FirebaseAuth.AuthStateListener {
        val user = it.currentUser
        _currentUser.value = user
        Log.d("SpendSenseAuth", "Auth state changed. User: ${user?.email ?: "Signed out"}")
    }

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    val isGoogleSignedIn: StateFlow<Boolean> =
        _currentUser.map { it != null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), auth.currentUser != null)

    val showAuthScreen: StateFlow<Boolean> =
        combine(_currentUser, _isGuestMode) { user, guest ->
            user == null && !guest
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), auth.currentUser == null)

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedWeekDay = MutableStateFlow(getCurrentDayOfWeek())
    val selectedWeekDay: StateFlow<Int> = _selectedWeekDay.asStateFlow()

    private val _selectedMonthDay = MutableStateFlow(getCurrentDayOfMonth())
    val selectedMonthDay: StateFlow<Int> = _selectedMonthDay.asStateFlow()

    private val _budgetLimit = MutableStateFlow(800.0)
    val budgetLimit: StateFlow<Double> = _budgetLimit.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("Never synced")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    // NEW: separate sync error state (lightweight improvement)
    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    private val syncMutex = Mutex()

    private val _signInTrigger = MutableStateFlow(false)
    val signInTrigger: StateFlow<Boolean> = _signInTrigger.asStateFlow()

    // NEW: lightweight sync trigger to avoid spam
    private val syncTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonthName: String =
        SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())

    val currentUserId: StateFlow<String> =
        combine(_currentUser, _isGuestMode) { user, guest ->
            when {
                user != null -> user.uid
                guest -> "guest"
                else -> ""   // FIX: avoid mixing anonymous + guest data
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    @OptIn(ExperimentalCoroutinesApi::class)
    val rawExpenses: StateFlow<List<DecryptedExpense>> =
        currentUserId.flatMapLatest { userId ->
            if (userId.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.getAllExpensesDecrypted(userId)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init {
        viewModelScope.launch {
            delay(1800.milliseconds)
            _showSplash.value = false
        }

        auth.addAuthStateListener(authListener)

        // NEW: sync collector (prevents spam sync calls)
        viewModelScope.launch {
            syncTrigger
                .collect {
                    runCloudSync()
                }
        }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }

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

    fun signInGoogleAccount() {
        _signInTrigger.value = true
    }

    fun onSignInSuccess() {
        _signInTrigger.value = false
        _currentUser.value = auth.currentUser
        Log.d("SpendSenseAuth", "Sign-in success: ${auth.currentUser?.email}")

        viewModelScope.launch { runCloudRestore() }
    }

    fun onSignInFailed() {
        _isSyncing.value = false
        _signInTrigger.value = false
        Log.e("SpendSenseAuth", "Sign-in failed")
    }

    fun signOutGoogleAccount() {
        auth.signOut()
        _currentUser.value = null
        _isGuestMode.value = false
        _lastSyncTime.value = "Never synced"
        _syncError.value = null
    }

    fun triggerCloudSync() {
        syncTrigger.tryEmit(Unit) // FIX: prevents multiple direct sync calls
    }

    fun addExpense(
        title: String,
        amount: Double,
        category: String,
        dayOfMonth: Int,
        dayOfWeek: Int
    ) {
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
            syncTrigger.tryEmit(Unit)
        }
    }

    fun deleteExpense(
        id: Int,
        category: String,
        dayOfMonth: Int,
        dayOfWeek: Int
    ) {
        viewModelScope.launch {
            repository.deleteExpense(
                currentUserId.value,
                id,
                category,
                dayOfMonth,
                currentMonth,
                currentYear,
                dayOfWeek
            )
            syncTrigger.tryEmit(Unit)
        }
    }

    private suspend fun runCloudSync() {
        if (_currentUser.value == null) return

        syncMutex.withLock {
            _isSyncing.value = true
            try {
                syncManager.syncWithCloud()
                _lastSyncTime.value = formatSyncTime()
                _syncError.value = null
            } catch (e: Exception) {
                _syncError.value = syncErrorDetail(e)
                _lastSyncTime.value = "Failed"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private suspend fun runCloudRestore() {
        syncMutex.withLock {
            _isSyncing.value = true
            try {
                syncManager.restoreFromCloud()
                _lastSyncTime.value = formatSyncTime()
                _syncError.value = null
            } catch (e: Exception) {
                _syncError.value = syncErrorDetail(e)
                _lastSyncTime.value = "Restore failed"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private fun formatSyncTime(): String {
        val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun syncErrorDetail(e: Exception): String = when (e) {
        is TimeoutCancellationException -> "request timed out"
        is FirebaseFirestoreException ->
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "permission denied"
                FirebaseFirestoreException.Code.UNAVAILABLE -> "server unavailable"
                else -> e.message ?: "unknown error"
            }
        else -> e.localizedMessage ?: "unknown error"
    }

    private fun getCurrentDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
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

    private fun getCurrentDayOfMonth(): Int =
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
}
