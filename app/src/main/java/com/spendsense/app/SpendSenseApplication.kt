package com.spendsense.app

import android.app.Application
import com.google.firebase.FirebaseApp

class SpendSenseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
