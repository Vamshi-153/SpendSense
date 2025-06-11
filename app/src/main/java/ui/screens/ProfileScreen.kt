package com.example.spendsense.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.spendsense.FileUtils
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@Composable
fun ProfileScreen(navController: NavController, mobileNumber: String, profilePictureUri: String) {
    val context = LocalContext.current
    var profileDetails by remember { mutableStateOf(ProfileDetails("", "", "", "")) }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

    // Load profile data from file using FileUtils
    LaunchedEffect(Unit) {
        profileDetails = loadProfileDetails(context, mobileNumber)
        profilePictureUri = profileDetails.profilePictureUri?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                profilePictureUri = it
                saveProfilePicture(context, mobileNumber, uri.toString())
                Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
            }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Management Text
            Text(
                text = "Manage your Profile here!",
                modifier = Modifier.padding(bottom = 16.dp),
                fontSize = 20.sp,
                color = Color.Black
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 8.dp)
                    .border(
                        width = 2.dp, // Border thickness
                        color = Color.Black, // Border color
                        shape = RoundedCornerShape(60.dp) // Circular border
                    ),
                contentAlignment = Alignment.Center
            ) {
                profilePictureUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE) // Set button color to 0xFF6200EE
                ),
                modifier = Modifier
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Change Profile Picture",
                    color = Color.White // Set text color to white
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Details Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8EAF6)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    ProfileDetailItem(label = "Name", value = profileDetails.name)
                    ProfileDetailItem(label = "Phone Number", value = mobileNumber)
                    ProfileDetailItem(label = "Email", value = profileDetails.email)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text(text = "Logout", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            }
        }
    }
}

// Save updated profile picture URI
fun saveProfilePicture(context: Context, mobileNumber: String, profilePictureUri: String) {
    val userData = FileUtils.getUserData(context, mobileNumber)
    if (userData != null) {
        FileUtils.saveUserData(
            context,
            mobileNumber,
            userData["name"] ?: "",
            userData["email"] ?: "",
            profilePictureUri
        )
    }
}

// Data Class for Profile Details
data class ProfileDetails(
    val name: String,
    val phoneNumber: String,
    val email: String,
    val profilePictureUri: String?
)

// Load profile details from FileUtils
fun loadProfileDetails(context: Context, mobileNumber: String): ProfileDetails {
    val userData = FileUtils.getUserData(context, mobileNumber)
    return if (userData != null) {
        ProfileDetails(
            name = userData["name"] ?: "Unknown",
            phoneNumber = mobileNumber,
            email = userData["email"] ?: "Unknown",
            profilePictureUri = userData["profilePictureUri"]
        )
    } else {
        ProfileDetails("No Data", mobileNumber, "NoData@example.com", "android.resource://com.example.spendsense/drawable/ic_default_profile")
    }
}

// Profile Detail Item Composable
@Composable
fun ProfileDetailItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF6200EE),
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
