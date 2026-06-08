package com.spendsense.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.spendsense.app.data.AppDatabase
import com.spendsense.app.data.ExpenseRepository
import com.spendsense.app.ui.SpendSenseApp
import com.spendsense.app.ui.SpendSenseViewModel
import com.spendsense.app.ui.SpendSenseViewModelFactory
import com.spendsense.app.ui.theme.SpendSenseTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth = FirebaseAuth.getInstance()
    
    // This Client ID must match the Web Client ID in your Firebase Console
    private val webClientId = "607813597743-a3gusb7okt6o748o0thfths32obp233h.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SpendSenseAuth", "MainActivity onCreate")
        
        // Setup Room Database & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = ExpenseRepository(database.expenseDao())
        val syncManager = com.spendsense.app.data.SyncManager(database.expenseDao())
        
        // Instantiate ViewModel using custom Factory
        val viewModel: SpendSenseViewModel by viewModels {
            SpendSenseViewModelFactory(repository, syncManager)
        }
        
        // Initialize Google Sign-In with requested profile and email
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        enableEdgeToEdge()
        setContent {
            SpendSenseTheme {
                // Activity Result Launcher for Google Sign-In
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    Log.d("SpendSenseAuth", "ActivityResult received: ${result.resultCode}")
                    if (result.resultCode == RESULT_OK) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            val idToken = account?.idToken
                            Log.d("SpendSenseAuth", "Google Token received: ${idToken?.take(10)}...")
                            if (idToken != null) {
                                // Firebase authentication with Google credential
                                lifecycleScope.launch {
                                    signInWithGoogle(idToken, viewModel)
                                }
                            } else {
                                Log.e("SpendSenseAuth", "No ID token found in account result!")
                                Toast.makeText(this, "Google Sign-In failed: No ID Token", Toast.LENGTH_SHORT).show()
                                viewModel.onSignInFailed()
                            }
                        } catch (e: ApiException) {
                            Log.e("SpendSenseAuth", "Sign-In task failed with status code: ${e.statusCode}", e)
                            val errorMsg = when (e.statusCode) {
                                10 -> "Developer console is not set up correctly (check SHA-1)."
                                12500 -> "Sign-in failed. Please check your internet or Google Play Services."
                                else -> e.message ?: "Unknown error"
                            }
                            Toast.makeText(this, "Sign-In failed: $errorMsg", Toast.LENGTH_LONG).show()
                            viewModel.onSignInFailed()
                        }
                    } else {
                        Log.e("SpendSenseAuth", "Sign-In result not OK: ${result.resultCode}")
                        Toast.makeText(this, "Sign-In cancelled or failed (Code: ${result.resultCode})", Toast.LENGTH_SHORT).show()
                        viewModel.onSignInFailed()
                    }
                }
                
                // Listen for sign-in trigger from ViewModel
                LaunchedEffect(Unit) {
                    viewModel.signInTrigger.collect { shouldSignIn ->
                        if (shouldSignIn) {
                            Log.d("SpendSenseAuth", "Sign-in trigger observed in MainActivity")
                            initiateGoogleSignIn(launcher)
                        }
                    }
                }
                
                // Additional effect to handle sign-out at the client level (important for re-selecting accounts)
                LaunchedEffect(Unit) {
                    viewModel.currentUser.collect { user ->
                        if (user == null) {
                            Log.d("SpendSenseAuth", "User is null, performing client-side sign out to clear cached accounts")
                            googleSignInClient.signOut()
                        }
                    }
                }
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SpendSenseApp(viewModel = viewModel)
                }
            }
        }
    }
    
    private fun initiateGoogleSignIn(
        launcher: ActivityResultLauncher<android.content.Intent>
    ) {
        Log.d("SpendSenseAuth", "Launching Google Sign-In Intent")
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    
    private suspend fun signInWithGoogle(idToken: String, viewModel: SpendSenseViewModel) {
        try {
            Log.d("SpendSenseAuth", "Starting Firebase Auth with credential")
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user
            Log.d("SpendSenseAuth", "Firebase Sign-In successful for user: ${user?.email}")
            Toast.makeText(this, "Welcome, ${user?.displayName ?: user?.email}", Toast.LENGTH_SHORT).show()
            viewModel.onSignInSuccess()
        } catch (e: Exception) {
            Log.e("SpendSenseAuth", "Firebase authentication failed", e)
            Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
            viewModel.onSignInFailed()
        }
    }
}
