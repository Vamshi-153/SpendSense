package com.example.spendsense

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.spendsense.ui.theme.SpendSenseTheme
import coil.compose.rememberAsyncImagePainter

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mobileNumber = intent.getStringExtra("MOBILE_NUMBER") ?: ""

        setContent {
            SpendSenseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    HomeContent(mobileNumber)
                }
            }
        }
    }
}

@Composable
fun WelcomeText(userName: String, profilePictureUri: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // Use a darker background
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!profilePictureUri.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(Uri.parse(profilePictureUri)),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
            )
        }
//        Text(
//            text = "Welcome, $userName!",
//            color = Color(0xFF6200EE),
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold
//        )

    }
}

@Composable
fun InfoText() {
    Text(
        text = "Manage your expenses easily with SpendSense.",
        fontSize = 18.sp,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
