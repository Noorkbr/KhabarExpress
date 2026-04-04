package com.khabarexpress.buyer.presentation.auth.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.khabarexpress.buyer.navigation.Screen
import com.khabarexpress.buyer.ui.theme.Gold
import com.khabarexpress.buyer.ui.theme.Navy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { showContent = true }
    LaunchedEffect(phone) { phoneError = null }
    LaunchedEffect(password) { passwordError = null }
    LaunchedEffect(confirmPassword) { confirmError = null }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Navy, Color(0xFF0F1D36))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Back button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600)) + slideInVertically(
                    tween(600, easing = EaseOutBack),
                    initialOffsetY = { -40 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Storefront,
                        contentDescription = "Staff",
                        modifier = Modifier.size(48.dp),
                        tint = Gold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Staff Registration",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create your restaurant staff account",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Phone Field
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(
                    tween(600, delayMillis = 200),
                    initialOffsetY = { 30 }
                )
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 11) phone = filtered
                    },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("01XXXXXXXXX") },
                    prefix = {
                        Text(
                            text = "+880  ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Gold
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = Gold)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Gold,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    isError = phoneError != null,
                    supportingText = phoneError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically(
                    tween(600, delayMillis = 300),
                    initialOffsetY = { 30 }
                )
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide" else "Show",
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Gold,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    isError = passwordError != null,
                    supportingText = passwordError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 400)) + slideInVertically(
                    tween(600, delayMillis = 400),
                    initialOffsetY = { 30 }
                )
            ) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide" else "Show",
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Gold,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    isError = confirmError != null,
                    supportingText = confirmError?.let {
                        { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 500))
            ) {
                Button(
                    onClick = {
                        var hasError = false
                        val fullNumber = if (phone.startsWith("0")) phone else "0$phone"
                        if (!fullNumber.matches(Regex("^01[3-9]\\d{8}$"))) {
                            phoneError = "Please enter a valid Bangladeshi phone number"
                            hasError = true
                        }
                        if (password.length < 6) {
                            passwordError = "Password must be at least 6 characters"
                            hasError = true
                        }
                        if (password != confirmPassword) {
                            confirmError = "Passwords do not match"
                            hasError = true
                        }
                        if (!hasError) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Navy
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "CREATE ACCOUNT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 600))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Already have an account?",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Sign In", color = Gold, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
