package com.example.spendsense

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spendsense.ui.screens.ProfileScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("otp/{mobileNumber}") { backStackEntry ->
            val mobileNumber = backStackEntry.arguments?.getString("mobileNumber") ?: ""
            OtpScreen(navController, mobileNumber)
        }
        composable("register/{mobileNumber}") { backStackEntry ->
            val mobileNumber = backStackEntry.arguments?.getString("mobileNumber") ?: ""
            RegisterScreen(navController, mobileNumber)
        }
        composable("home/{mobileNumber}") { backStackEntry ->
            val mobileNumber = backStackEntry.arguments?.getString("mobileNumber") ?: ""
            HomeScreen(navController, mobileNumber)
        }
        composable("profile/{mobileNumber}") { backStackEntry ->
            val mobileNumber = backStackEntry.arguments?.getString("mobileNumber") ?: ""
            ProfileScreen(navController, mobileNumber, "android.resource://com.example.spendsense/drawable/ic_default_profile")
        }
    }
}