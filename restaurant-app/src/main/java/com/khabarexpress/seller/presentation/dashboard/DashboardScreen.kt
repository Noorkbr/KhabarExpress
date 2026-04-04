package com.khabarexpress.seller.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.navigation.RestaurantScreen

private val Gold = Color(0xFFD4A03C)
private val Navy = Color(0xFF1B2A4A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNavItem by remember { mutableIntStateOf(0) }

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
                title = {
                    Column {
                        Text("KhabarExpress", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (uiState.isOpen) "Open for orders" else "Closed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isOpen) Color(0xFF10B981) else Color.White.copy(alpha = 0.5f)
                        )
                    }
                },
                actions = {
                    Switch(
                        checked = uiState.isOpen,
                        onCheckedChange = { viewModel.toggleOpenStatus() },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Gold,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = { navController.navigate(RestaurantScreen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Navy) {
                NavigationBarItem(
                    selected = selectedNavItem == 0,
                    onClick = { selectedNavItem = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold,
                        selectedTextColor = Gold,
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f),
                        indicatorColor = Gold.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(RestaurantScreen.Orders.route) },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(RestaurantScreen.Menu.route) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Menu") },
                    label = { Text("Menu") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(RestaurantScreen.Earnings.route) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Earnings") },
                    label = { Text("Earnings") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    )
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Analytics cards
                item {
                    AnalyticsSection(uiState)
                }

                // Pending Orders header
                if (uiState.pendingOrders.isNotEmpty()) {
                    item {
                        Text(
                            "Pending Orders (${uiState.pendingOrders.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    }
                    items(uiState.pendingOrders) { order ->
                        DashboardOrderCard(
                            order = order,
                            onAccept = { viewModel.acceptOrder(order.id) },
                            onReject = { viewModel.rejectOrder(order.id, "Rejected by restaurant") }
                        )
                    }
                }

                // Active Orders header
                if (uiState.activeOrders.isNotEmpty()) {
                    item {
                        Text(
                            "Active Orders (${uiState.activeOrders.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                    }
                    items(uiState.activeOrders) { order ->
                        ActiveOrderCard(
                            order = order,
                            onUpdateStatus = { status -> viewModel.updateOrderStatus(order.id, status) }
                        )
                    }
                }

                // Empty state
                if (uiState.pendingOrders.isEmpty() && uiState.activeOrders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Inbox,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No active orders", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsSection(uiState: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Navy)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Today's Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DashboardStatItem(
                    label = "Orders",
                    value = "${uiState.analytics.totalOrders}",
                    icon = Icons.Default.Receipt,
                    iconTint = Gold
                )
                DashboardStatItem(
                    label = "Revenue",
                    value = "৳${String.format("%.0f", uiState.analytics.totalRevenue / 100)}",
                    icon = Icons.Default.AttachMoney,
                    iconTint = Color(0xFF10B981)
                )
                DashboardStatItem(
                    label = "Avg. Value",
                    value = "৳${String.format("%.0f", uiState.analytics.averageOrderValue / 100)}",
                    icon = Icons.Default.TrendingUp,
                    iconTint = Color(0xFF3B82F6)
                )
            }
        }
    }
}

@Composable
private fun DashboardStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DashboardOrderCard(
    order: Order,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber ?: "#${order.id.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            "Pending",
                            color = Color(0xFFF59E0B),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(order.customerName, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { item ->
                Text("${item.quantity}x ${item.name}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontWeight = FontWeight.Bold)
                Text(
                    "৳${String.format("%.0f", order.total / 100)}",
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Reject")
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text("Accept", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ActiveOrderCard(
    order: Order,
    onUpdateStatus: (String) -> Unit
) {
    val statusColor = when (order.status) {
        "confirmed" -> Color(0xFF3B82F6)
        "preparing" -> Color(0xFF8B5CF6)
        "ready" -> Color(0xFF10B981)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber ?: "#${order.id.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            order.status.replaceFirstChar { it.uppercase() },
                            color = statusColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(order.customerName, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${order.items.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    "৳${String.format("%.0f", order.total / 100)}",
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            when (order.status) {
                "confirmed" -> {
                    Button(
                        onClick = { onUpdateStatus("preparing") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Text("Start Preparing", color = Color.White)
                    }
                }
                "preparing" -> {
                    Button(
                        onClick = { onUpdateStatus("ready") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Mark as Ready", color = Color.White)
                    }
                }
            }
        }
    }
}
