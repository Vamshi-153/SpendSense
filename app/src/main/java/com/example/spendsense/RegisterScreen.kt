package com.example.spendsense

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController

@Composable
fun RegisterScreen(navController: NavController, mobileNumber: String) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }

    // Default profile icon URI (used in home if not updated)
    val defaultProfileUri = "android.resource://com.example.spendsense/drawable/ic_default_profile"

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
            Text(
                text = "Register",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Default Profile Icon (Displayed During Registration)
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile Icon",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .shadow(8.dp, shape = CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(
                        text = "Full Name",
                        color = Color.Black // Label color
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(64.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black // Text color
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        text = "Email Address",
                        color = Color.Black // Label color
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(64.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black // Text color
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email // Email keyboard with suggestions
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Save user data with default profile URI
                    FileUtils.saveUserData(
                        navController.context,
                        mobileNumber,
                        name.text,
                        email.text,
                        defaultProfileUri // Save default URI
                    )
                    Toast.makeText(navController.context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home/$mobileNumber")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text(text = "Complete Registration", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
