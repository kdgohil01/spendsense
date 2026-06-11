package com.spendsense.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.spendsense.app.data.DecryptedExpense
import com.spendsense.app.ui.components.CategoryColors
import com.spendsense.app.ui.components.CategoryDonutChart
import com.spendsense.app.ui.components.WeeklyBarChart
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendSenseApp(viewModel: SpendSenseViewModel) {
    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val showAuthScreen by viewModel.showAuthScreen.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (showSplash) {
            SplashAnimationScreen()
        } else if (showAuthScreen) {
            AuthenticationScreen(viewModel)
        } else {
            MainAppDashboard(viewModel)
        }
    }
}

@Composable
fun AuthenticationScreen(viewModel: SpendSenseViewModel) {
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome to SpendSense",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Secure your expenses with end-to-end encryption and cloud backup.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { if (!isSyncing) viewModel.signInGoogleAccount() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Sign in with Google", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { viewModel.enterAsGuest() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSyncing,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("Continue as Guest", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun SplashAnimationScreen() {
    var scale by remember { mutableStateOf(0.7f) }
    var alpha by remember { mutableStateOf(0f) }
    
    // Scale and Alpha transitions
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0.7f,
            targetValue = 1.1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) { valValue, _ -> scale = valValue }
    }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1200, easing = LinearOutSlowInEasing)
        ) { valValue, _ -> alpha = valValue }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF070B11), Color(0xFF111927))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulsing Emerald Logo Ring
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                // Background radial glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x2200C897), Color.Transparent),
                            radius = size.width / 1.5f
                        )
                    )
                }
                
                // Actual Wallet Symbol
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF33D9B2), Color(0xFF00C897))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet Logo",
                        tint = Color(0xFF070B11),
                        modifier = Modifier.size(45.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            
            // App Name with high-end tracking
            Text(
                text = "SpendSense",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF8FAFC),
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "End-to-End Encrypted Expense Tracker",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(alpha)
            )
        }
        
        // Circular Loading progress at the bottom
        CircularProgressIndicator(
            color = Color(0xFF00C897),
            strokeWidth = 3.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .size(32.dp)
                .alpha(alpha)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppDashboard(viewModel: SpendSenseViewModel) {
    val expenses by viewModel.rawExpenses.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedWeekDay by viewModel.selectedWeekDay.collectAsStateWithLifecycle()
    val selectedMonthDay by viewModel.selectedMonthDay.collectAsStateWithLifecycle()
    val budgetLimit by viewModel.budgetLimit.collectAsStateWithLifecycle()
    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSyncSheet by remember { mutableStateOf(false) }

    // Computations
    val monthlyTotal = remember(expenses) {
        expenses.filter { it.month == viewModel.currentMonth && it.year == viewModel.currentYear }.sumOf { it.amount }
    }

    val weeklyTotal = remember(expenses) {
         expenses.filter { 
             // Simplistic current week matching
             it.dayOfWeek in 1..7 && it.month == viewModel.currentMonth
         }.sumOf { it.amount }
    }

    val currentViewTotal = if (selectedTab == 0) weeklyTotal else monthlyTotal

    // Day level filtered list display
    val activeFilteredExpenses = remember(expenses, selectedTab, selectedWeekDay, selectedMonthDay) {
        if (selectedTab == 0) {
            expenses.filter { it.dayOfWeek == selectedWeekDay && it.month == viewModel.currentMonth }
        } else {
            expenses.filter { it.dayOfMonth == selectedMonthDay && it.month == viewModel.currentMonth }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("spendsense_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Savings, 
                            contentDescription = "Dollar Badge",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SpendSense",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    // Sync icon indicating backup status
                    if (isGoogleSignedIn) {
                        IconButton(
                            onClick = { viewModel.triggerCloudSync() },
                            enabled = !isSyncing,
                            modifier = Modifier.testTag("dashboard_sync_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = "Synced",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    // Profile/Sign-In Trigger Icon
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .background(
                                color = if (isGoogleSignedIn) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable { showSyncSheet = true }
                            .testTag("google_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGoogleSignedIn && currentUser != null) {
                            if (currentUser?.photoUrl != null) {
                                AsyncImage(
                                    model = currentUser?.photoUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                val initials = currentUser?.displayName?.split(" ")
                                    ?.mapNotNull { it.firstOrNull()?.toString() }
                                    ?.take(2)
                                    ?.joinToString("") ?: currentUser?.email?.take(1)?.uppercase() ?: "U"
                                    
                                Text(
                                    initials,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Sign In Profile",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("add_expense_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = "Add Expense Icon",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Budget Stats Cards Header
            item {
                BudgetStatsSection(
                    totalSpent = currentViewTotal,
                    budgetLimit = budgetLimit,
                    selectedTab = selectedTab,
                    onBudgetEditRequested = { viewModel.updateBudgetLimit(it) }
                )
            }

            // 2. Visual Canvas charts
            item {
                if (selectedTab == 0) {
                    WeeklyBarChart(
                        expenses = expenses,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CategoryDonutChart(
                        expenses = expenses,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. Tab row switcher ("7 Days of Week" & "31 Days of Month")
            item {
                TabSelectionRow(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }

            // 4. Calendar Horizontal Day Picker
            item {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                    }, 
                    label = "CalendarPickerTransition"
                ) { targetTab ->
                    if (targetTab == 0) {
                        WeeklyDaySegment(
                            selectedDay = selectedWeekDay,
                            expenses = expenses,
                            onDayClick = { viewModel.selectWeekDay(it) }
                        )
                    } else {
                        MonthlyGridPicker(
                            selectedDay = selectedMonthDay,
                            expenses = expenses,
                            onDayClick = { viewModel.selectMonthDay(it) }
                        )
                    }
                }
            }

            // 5. Section Header representing current day focus
            item {
                val focusHeader = if (selectedTab == 0) {
                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    "${days[selectedWeekDay - 1]}'s Active Expenses"
                } else {
                    "Day $selectedMonthDay of ${viewModel.currentMonthName}"
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = focusHeader,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Total Spending: ₹${String.format("%.2f", activeFilteredExpenses.sumOf { it.amount })}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 6. Active Expenses List
            if (activeFilteredExpenses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "No receipts",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No expenses recorded for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(activeFilteredExpenses) { expense ->
                    ExpenseRowItem(
                        expense = expense,
                        onDeleteClick = { viewModel.deleteExpense(expense.id, expense.category, expense.dayOfMonth, expense.dayOfWeek) }
                    )
                }
            }
            
            // Bottom spacer for list scroll comfort
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // Modal dialogs
    if (showAddDialog) {
        AddExpenseDialog(
            preselectedDayOfWeek = selectedWeekDay,
            preselectedDayOfMonth = selectedMonthDay,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amt ->
                viewModel.addExpense(title, amt, "Other", selectedMonthDay, selectedWeekDay)
                showAddDialog = false
            }
        )
    }

    if (showSyncSheet) {
        GoogleSyncDialog(
            currentUser = currentUser,
            isSyncing = isSyncing,
            lastSyncTime = lastSyncTime,
            onDismiss = { showSyncSheet = false },
            onSignInClick = { viewModel.signInGoogleAccount() },
            onSignOutClick = { viewModel.signOutGoogleAccount() },
            onSyncClick = { viewModel.triggerCloudSync() }
        )
    }
}

@Composable
fun BudgetStatsSection(
    totalSpent: Double,
    budgetLimit: Double,
    selectedTab: Int,
    onBudgetEditRequested: (Double) -> Unit
) {
    val overBudget = totalSpent > budgetLimit
    val fraction = if (budgetLimit > 0.1) (totalSpent / budgetLimit).coerceIn(0.0..1.0) else 1.0
    var isEditingBudget by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf(budgetLimit.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedTab == 0) "WEEKLY RECORD TOTAL" else "MONTHLY RECORD TOTAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "₹${String.format("%.2f", totalSpent)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Real-time dynamic gauge indicator badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (overBudget) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (overBudget) "Over limit!" else "${(fraction * 100).toInt()}% Used",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Linear progress meter
            LinearProgressIndicator(
                progress = fraction.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Budget configuration details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditingBudget) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = budgetInput,
                            onValueChange = { budgetInput = it },
                            label = { Text("Limit") },
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(120.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                        )
                        IconButton(
                            onClick = {
                                budgetInput.toDoubleOrNull()?.let { onBudgetEditRequested(it) }
                                isEditingBudget = false
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Confirm", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clickable { isEditingBudget = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit, 
                            contentDescription = "Edit budget",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Budget Limit: ₹${String.format("%.2f", budgetLimit)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Text(
                    text = "Remaining: ₹${String.format("%.2f", (budgetLimit - totalSpent).coerceAtLeast(0.0))}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (overBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TabSelectionRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(4.dp)
    ) {
        val tabNames = listOf("7 Days of Week", "31 Days of Month")
        tabNames.forEachIndexed { idx, name ->
            val isSelected = selectedTab == idx
            val scaleAnimation = animateFloatAsState(if (isSelected) 1.03f else 1f)
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(scaleAnimation.value)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onTabSelected(idx) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun WeeklyDaySegment(
    selectedDay: Int,
    expenses: List<DecryptedExpense>,
    onDayClick: (Int) -> Unit
) {
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEachIndexed { index, name ->
                val dayId = index + 1
                val isSelected = selectedDay == dayId
                val daySpent = expenses.filter { it.dayOfWeek == dayId }.sumOf { it.amount }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onDayClick(dayId) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (daySpent > 0f) "₹${daySpent.toInt()}" else "—",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (daySpent > 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyGridPicker(
    selectedDay: Int,
    expenses: List<DecryptedExpense>,
    onDayClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Tap on any day to record/view spending details",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dynamic grid list of 31 days
            val items = (1..31).toList()
            val chunked = items.chunked(7) // 7 cols per row

            chunked.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { dayNum ->
                        val isSelected = selectedDay == dayNum
                        val daySpent = expenses.filter { it.dayOfMonth == dayNum }.sumOf { it.amount }
                        val hasExpenses = daySpent > 0.0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        hasExpenses -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onDayClick(dayNum) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNum.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                if (hasExpenses && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(4.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Fill remaining column cells if the last row isn't complete
                    val remaining = 7 - row.size
                    if (remaining > 0) {
                        for (j in 0 until remaining) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseRowItem(
    expense: DecryptedExpense,
    onDeleteClick: () -> Unit
) {
    val catColor = CategoryColors[expense.category] ?: Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category accent indicator stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(catColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Icon representation
            val icon = getCategoryIcon(expense.category)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(catColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = expense.category,
                    tint = catColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Main details block
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = expense.category,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Amount and Action
            Text(
                text = "-₹${String.format("%.2f", expense.amount)}",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.testTag("delete_expense_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    preselectedDayOfWeek: Int,
    preselectedDayOfMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Secure Expense Entry",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Any transaction data entered here is locally protected using 256-bit AES end-to-end KeyStore encryption.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g., Dinner)") },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_title")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)") },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_amount")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 1.0
                    if (title.isNotBlank()) {
                        onConfirm(title, amt)
                    }
                },
                modifier = Modifier.testTag("save_expense_confirm")
            ) {
                Text("Save Encrypted")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GoogleSyncDialog(
    currentUser: com.google.firebase.auth.FirebaseUser?,
    isSyncing: Boolean,
    lastSyncTime: String,
    onDismiss: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    val isGoogleSignedIn = currentUser != null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Cloud logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Cloud Synchronization", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Link SpendSense with your Google Account for seamless automated encrypted database backups on demand.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (isGoogleSignedIn) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (currentUser?.photoUrl != null) {
                                    AsyncImage(
                                        model = currentUser.photoUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.size(40.dp).clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initial = currentUser?.displayName?.firstOrNull()?.toString() 
                                            ?: currentUser?.email?.firstOrNull()?.toString() ?: "U"
                                        Text(initial.uppercase(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(currentUser?.displayName ?: "Google User", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(currentUser?.email ?: "No email available", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text("Signed in via Google", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Last sync backup:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(lastSyncTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSyncClick,
                            modifier = Modifier.weight(1f).testTag("dialog_sync_now"),
                            enabled = !isSyncing
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Sync Now")
                            }
                        }
                        
                        OutlinedButton(
                            onClick = onSignOutClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Disconnect")
                        }
                    }
                } else {
                    Button(
                        onClick = onSignInClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("dialog_sign_in_google"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // A custom Google circle icon drawing
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Google Sync Icon",
                                tint = MaterialTheme.colorScheme.surface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sign in with Google", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Security Shield Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security, 
                            contentDescription = "Shield Guard",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("End-To-End Encrypted Backups", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Your records are completely encrypted locally on your chipset using standard AES 256-bit GCM prior to syncing, making it impossible for anybody (including Google or SpendSense) to view details.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

// Map corresponding icons
private fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Restaurant
        "Bills" -> Icons.Default.CreditCard
        "Groceries" -> Icons.Default.ShoppingCart
        "Leisure" -> Icons.Default.LocalActivity
        "Travel" -> Icons.Default.FlightTakeoff
        else -> Icons.Default.AccountBalanceWallet
    }
}
