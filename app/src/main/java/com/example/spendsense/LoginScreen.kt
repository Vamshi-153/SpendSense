package com.example.spendsense

import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {
    var mobileNumber by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB39DDB), // Light Purple
                        Color(0xFF80DEEA)  // Light Blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Name - SpendSense at Top Center
            Text(
                text = "SpendSense",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(400.dp))

            // New Text - "Hey, what's your number?"
            Text(
                text = "Hey, what's your number?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = {
                    if (it.length <= 10) { // Limit to 10 digits
                        mobileNumber = it
                        isError = false
                    }
                },
                label = {
                    Text(
                        text = "Mobile Number",
                        color = Color.Black // Label text color
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(64.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black // Text inside the box
                ),
                isError = isError, // Show error state if true
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
                    focusedBorderColor = if (isError) Color.Red else Color(0xFF6200EE),
                    errorBorderColor = Color.Red
                ),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    if (mobileNumber.length == 10) {
                        isLoading = true
                        // Navigate to OTP screen for verification
                        navController.navigate("otp/$mobileNumber") {
                            popUpTo("login") { inclusive = false }
                        }
                    } else {
                        isError = true
                        vibratePhone(context) // Vibrate if mobile number is invalid
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(text = "Send OTP", color = Color.White, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(78.dp))
        }
    }
}

// Vibrate phone on error
fun vibratePhone(context: Context) {
    val vibrator = getSystemService(context, Vibrator::class.java)
    vibrator?.let {
        if (it.hasVibrator()) {
            val vibrationEffect = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
            it.vibrate(vibrationEffect)
        }
    }
}