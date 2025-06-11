package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.spendsense.ui.theme.SpendSenseTheme
import androidx.compose.ui.layout.ContentScale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpendSenseTheme {
                val showSplash = remember { mutableStateOf(true) }

                // Show Splash for 2 seconds, then navigate to Login
                LaunchedEffect(Unit) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        showSplash.value = false
                    }, 2000) // Delay 2 seconds
                }

                if (showSplash.value) {
                    SplashScreenUI()
                } else {
                    AppNavigation() // Main Navigation for Login/Register/Home
                }
            }
        }
    }
}

@Composable
fun SplashScreenUI() {
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
    Image(
        painter = painterResource(R.drawable.mainpagelogo),
        contentDescription = "App Logo",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}
//}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavGraph(navController = navController)
}
