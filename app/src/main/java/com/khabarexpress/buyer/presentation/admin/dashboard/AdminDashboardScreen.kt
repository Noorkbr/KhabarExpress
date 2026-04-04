package com.khabarexpress.buyer.presentation.admin.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.khabarexpress.buyer.navigation.Screen
import com.khabarexpress.buyer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Admin Panel",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "KhabarExpress Control Center",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Gold
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Gold
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's Stats
            item {
                Text(
                    "Today's Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ShoppingCart,
                        label = "Orders Today",
                        value = "${uiState.todayOrders}",
                        color = Gold
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AccountBalance,
                        label = "Revenue Today",
                        value = "৳${String.format("%.0f", uiState.todayRevenue / 100)}",
                        color = Success
                    )
                }
            }

            // Platform Totals
            item {
                Text(
                    "Platform Totals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.People,
                        label = "Total Users",
                        value = "${uiState.totalUsers}",
                        color = Info
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Restaurant,
                        label = "Restaurants",
                        value = "${uiState.totalRestaurants}",
                        color = Warning
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.DeliveryDining,
                        label = "Active Orders",
                        value = "${uiState.activeOrders}",
                        color = DeliveryBadge
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ListAlt,
                        label = "Total Orders",
                        value = "${uiState.totalOrders}",
                        color = Navy
                    )
                }
            }

            // Admin Profit Summary
            item {
                Text(
                    "Profit Summary (5% Admin Share)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldLight)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Payments", color = Navy)
                            Text(
                                "৳${String.format("%.2f", uiState.totalPayments / 100)}",
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Admin Profit (5%)", color = Success)
                            Text(
                                "৳${String.format("%.2f", uiState.totalAdminProfit / 100)}",
                                fontWeight = FontWeight.Bold,
                                color = Success
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Restaurant Payouts", color = Navy)
                            Text(
                                "৳${String.format("%.2f", uiState.totalRestaurantPayout / 100)}",
                                fontWeight = FontWeight.Bold,
                                color = Navy
                            )
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Verified,
                        label = "Restaurant\nVerification",
                        badge = if (uiState.pendingRestaurants > 0) "${uiState.pendingRestaurants}" else null,
                        onClick = {
                            navController.navigate(Screen.AdminRestaurantVerification.route)
                        }
                    )
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Assessment,
                        label = "Payment\nReports",
                        onClick = {
                            navController.navigate(Screen.AdminPaymentReports.route)
                        }
                    )
                }
            }

            // Pending Approvals Alert
            if (uiState.pendingRestaurants > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.AdminRestaurantVerification.route)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Warning
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${uiState.pendingRestaurants} Pending Approvals",
                                    fontWeight = FontWeight.Bold,
                                    color = Warning
                                )
                                Text(
                                    "Restaurants waiting for verification",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "View",
                                tint = Warning
                            )
                        }
                    }
                }
            }

            // Error display
            if (uiState.error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f))
                    ) {
                        Text(
                            uiState.error ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = Error
                        )
                    }
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Gold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(32.dp))
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gold,
                    lineHeight = 16.sp
                )
            }
            if (badge != null) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    containerColor = Error
                ) {
                    Text(badge, color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}
