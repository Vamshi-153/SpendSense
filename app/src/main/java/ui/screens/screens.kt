package com.example.spendsense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransactionsScreenv2() {
    ScreenContent(title = "Transactions")
}

@Composable
fun InvestmentsScreenv2() {
    ScreenContent(title = "Investments")
}

@Composable
fun ProfileScreenv2() {
    ScreenContent(title = "Profile")
}

@Composable
fun ScreenContent(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome to $title Screen",
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}
