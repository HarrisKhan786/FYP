package com.example.bodyfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.bodyfit.ui.theme.BodyfitTheme
import com.example.bodyfit.view.AppNavigator
import com.example.bodyfit.view.LoginScreen
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
          BodyfitTheme {
              Scaffold(modifier = Modifier.fillMaxSize()){ innerPadding ->

                  AppNavigator(paddingValues = innerPadding);
              }
          }
        }
    }
}