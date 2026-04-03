package com.khabarexpress.buyer.presentation.auth.otp

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.khabarexpress.buyer.presentation.auth.login.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPVerificationScreen(
    phoneNumber: String,
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var otpValue by remember { mutableStateOf("") }
    var isResendEnabled by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(60) }
    val scope = rememberCoroutineScope()
    
    // Countdown timer
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        isResendEnabled = true
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify OTP") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Enter Verification Code",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "We've sent a 6-digit code to\n$phoneNumber",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // OTP Input Fields
            OTPInputField(
                otpValue = otpValue,
                onOtpChange = { newValue ->
                    if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                        otpValue = newValue
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resend OTP
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Didn't receive code? ",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = {
                        if (isResendEnabled) {
                            countdown = 60
                            isResendEnabled = false
                            scope.launch {
                                while (countdown > 0) {
                                    delay(1000L)
                                    countdown--
                                }
                                isResendEnabled = true
                            }
                        }
                    },
                    enabled = isResendEnabled
                ) {
                    Text(
                        text = if (isResendEnabled) "Resend" else "Resend in ${countdown}s",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Verify Button
            Button(
                onClick = {
                    // Handle OTP verification
                    if (otpValue.length == 6) {
                        // viewModel.verifyOTP(phoneNumber, otpValue)
                        navController.navigateUp()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = otpValue.length == 6
            ) {
                Text(
                    text = "Verify & Continue",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun OTPInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            val char = otpValue.getOrNull(index)?.toString() ?: ""
            val isFocused = otpValue.length == index
            
            OTPDigitBox(
                digit = char,
                isFocused = isFocused
            )
        }
    }
    
    // Hidden text field for keyboard input
    BasicTextField(
        value = otpValue,
        onValueChange = onOtpChange,
        modifier = Modifier.size(0.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
    )
}

@Composable
fun OTPDigitBox(
    digit: String,
    isFocused: Boolean
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .border(
                width = 2.dp,
                color = when {
                    digit.isNotEmpty() -> MaterialTheme.colorScheme.primary
                    isFocused -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (digit.isNotEmpty()) 
                MaterialTheme.colorScheme.onSurface 
            else 
                Color.Transparent
        )
    }
}
