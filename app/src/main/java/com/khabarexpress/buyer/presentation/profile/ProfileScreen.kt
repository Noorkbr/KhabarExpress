package com.khabarexpress.buyer.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.khabarexpress.buyer.navigation.Screen
import com.khabarexpress.buyer.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture
                    AsyncImage(
                        model = "https://via.placeholder.com/150",
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "John Doe",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "john.doe@example.com",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+1 (555) 123-4567",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Menu Items
            item {
                ProfileMenuItem(
                    icon = Icons.Filled.Edit,
                    title = "Edit Profile",
                    onClick = { navController.navigate(Screen.EditProfile.route) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ProfileMenuItem(
                    icon = Icons.Filled.LocationOn,
                    title = "Saved Addresses",
                    onClick = { navController.navigate(Screen.SavedAddresses.route) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ProfileMenuItem(
                    icon = Icons.Filled.History,
                    title = "Order History",
                    onClick = { navController.navigate(Screen.OrderHistory.route) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ProfileMenuItem(
                    icon = Icons.Filled.Favorite,
                    title = "Favorites",
                    onClick = { navController.navigate(Screen.Favorites.route) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ProfileMenuItem(
                    icon = Icons.Filled.Payment,
                    title = "Payment Methods",
                    onClick = { navController.navigate(Screen.PaymentMethod.route) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ProfileMenuItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    onClick = { /* Notifications settings - Coming soon */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ProfileMenuItem(
                    icon = Icons.Filled.Settings,
                    title = "Settings",
                    onClick = { /* Settings - Coming soon */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Help & Support",
                    onClick = { /* Help & Support - Coming soon */ }
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                // Logout Button
                Button(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
