package com.example.spendsense

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

@Composable
fun OtpScreen(navController: NavController, mobileNumber: String) {
    val context = LocalContext.current
    val activity = context.findActivity()
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var resendEnabled by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    // Send OTP when screen loads
    LaunchedEffect(mobileNumber) {
        activity?.let {
            sendVerificationCode(
                activity = it,
                phoneNumber = "+91$mobileNumber", // Assuming Indian code, adjust as needed
                onCodeSent = { id ->
                    verificationId = id
                    resendEnabled = true
                },
                onVerificationFailed = { exception ->
                    errorMessage = "Verification failed: ${exception.message}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        } ?: run {
            Toast.makeText(context, "Failed to get Activity context", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFB39DDB),
                        Color(0xFF80DEEA)
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
                text = "SpendSense",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE),
                modifier = Modifier
                    .padding(top = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = "OTP Verification",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Enter the OTP sent to +91 $mobileNumber",
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = {
                    if (it.length <= 6) {
                        otp = it
                        errorMessage = ""
                    }
                },
                label = {
                    Text(
                        text = "Enter 6-digit OTP",
                        color = Color.Black
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(64.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                ),
                singleLine = true
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (otp.length == 6) {
                        isLoading = true
                        verifyOtp(
                            verificationId = verificationId,
                            otp = otp,
                            onSuccess = {
                                isLoading = false
                                if (FileUtils.doesUserExist(context, mobileNumber)) {
                                    navController.navigate("home/$mobileNumber") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("register/$mobileNumber") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            },
                            onFailure = { e ->
                                isLoading = false
                                errorMessage = "Verification failed: ${e.message}"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        errorMessage = "Please enter a valid 6-digit OTP"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                enabled = !isLoading && otp.length == 6
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(text = "Verify OTP", color = Color.White, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    resendEnabled = false
                    errorMessage = ""
                    activity?.let {
                        sendVerificationCode(
                            activity = it,
                            phoneNumber = "+91$mobileNumber",
                            onCodeSent = { id ->
                                verificationId = id
                                resendEnabled = true
                                Toast.makeText(context, "OTP resent successfully", Toast.LENGTH_SHORT).show()
                            },
                            onVerificationFailed = { exception ->
                                errorMessage = "Resend failed: ${exception.message}"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                enabled = resendEnabled && !isLoading
            ) {
                Text(
                    text = "Resend OTP",
                    color = if (resendEnabled && !isLoading) Color(0xFF6200EE) else Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Function to send verification code (Activity version)
private fun sendVerificationCode(
    activity: Activity,
    phoneNumber: String,
    onCodeSent: (String) -> Unit,
    onVerificationFailed: (FirebaseException) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

        override fun onVerificationFailed(e: FirebaseException) {
            onVerificationFailed(e)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            onCodeSent(verificationId)
        }
    }

    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(callbacks)
        .build()

    PhoneAuthProvider.verifyPhoneNumber(options)
}

// Function to verify OTP
private fun verifyOtp(
    verificationId: String,
    otp: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    try {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        val auth = FirebaseAuth.getInstance()

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception ?: Exception("Verification failed"))
                }
            }
    } catch (e: Exception) {
        onFailure(e)
    }
}

// Extension function to get Activity from Context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
