package com.khabarexpress.seller.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.khabarexpress.seller.navigation.RestaurantScreen
import com.khabarexpress.seller.presentation.dashboard.DashboardEvent
import com.khabarexpress.seller.presentation.dashboard.DashboardViewModel

private val Gold = Color(0xFFD4A03C)
private val Navy = Color(0xFF1B2A4A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.NavigateToLogin -> {
                    navController.navigate(RestaurantScreen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                else -> { }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Restaurant status toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Restaurant Status", fontWeight = FontWeight.Bold)
                        Text(
                            if (uiState.isOpen) "Open for orders" else "Closed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.isOpen) Color(0xFF10B981) else Color.Gray
                        )
                    }
                    Switch(
                        checked = uiState.isOpen,
                        onCheckedChange = { viewModel.toggleOpenStatus() },
                        colors = SwitchDefaults.colors(checkedTrackColor = Gold)
                    )
                }
            }

            // Settings items
            SettingsItem(
                icon = Icons.Filled.Restaurant,
                title = "Restaurant Profile",
                subtitle = "Manage your restaurant information",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Filled.AccessTime,
                title = "Business Hours",
                subtitle = "Set your operating hours",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Filled.LocationOn,
                title = "Delivery Area",
                subtitle = "Manage delivery zones and radius",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Filled.Payment,
                title = "Payout Settings",
                subtitle = "Manage payment and payout methods",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Filled.Notifications,
                title = "Notifications",
                subtitle = "Configure notification preferences",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Filled.Help,
                title = "Help & Support",
                subtitle = "Get help with your restaurant",
                onClick = { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logout button
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
