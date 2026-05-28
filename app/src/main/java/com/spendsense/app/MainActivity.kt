package com.spendsense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.spendsense.app.data.AppDatabase
import com.spendsense.app.data.ExpenseRepository
import com.spendsense.app.ui.SpendSenseApp
import com.spendsense.app.ui.SpendSenseViewModel
import com.spendsense.app.ui.SpendSenseViewModelFactory
import com.spendsense.app.ui.theme.SpendSenseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup Room Database & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = ExpenseRepository(database.expenseDao())
        
        // Instantiate ViewModel using custom Factory
        val viewModel: SpendSenseViewModel by viewModels {
            SpendSenseViewModelFactory(repository)
        }

        enableEdgeToEdge()
        setContent {
            SpendSenseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Main layout padding is safely integrated inside SpendSenseApp LazyColumn spacers
                    // so we do not double pad edge-to-edge content.
                    SpendSenseApp(viewModel = viewModel)
                }
            }
        }
    }
}
