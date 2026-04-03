package com.khabarexpress.seller.presentation.dashboard

import androidx.compose.foundation.background
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
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("নতুন (3)", "প্রস্তুত হচ্ছে (2)", "প্রস্তুত (1)")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KhabarExpress Restaurant", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { /* Navigate to notifications */ }) {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text("3")
                        }
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* Navigate to settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("ড্যাশবোর্ড") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navigate to orders */ },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                    label = { Text("অর্ডার") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navigate to menu */ },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Menu") },
                    label = { Text("মেনু") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navigate to analytics */ },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                    label = { Text("বিশ্লেষণ") }
                )
            }
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
                TodayStatsSection()
            }
            
            // Tab selector
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
            
            // Orders based on selected tab
            items(3) { index ->
                OrderCard(
                    orderNumber = "#${5678 + index}",
                    customerName = "গ্রাহক ${index + 1}",
                    items = listOf("বিরিয়ানি x2", "চিকেন কারি x1"),
                    totalAmount = "৳৬৮৫",
                    time = "${5 + index} মিনিট আগে",
                    status = when (selectedTab) {
                        0 -> OrderStatus.NEW
                        1 -> OrderStatus.PREPARING
                        else -> OrderStatus.READY
                    },
                    onAccept = { /* Handle accept */ },
                    onReject = { /* Handle reject */ },
                    onMarkPreparing = { /* Handle mark preparing */ },
                    onMarkReady = { /* Handle mark ready */ }
                )
            }
        }
    }
}

@Composable
fun TodayStatsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "আজকের পরিসংখ্যান",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "অর্ডার", value = "৪৮", icon = Icons.Default.Receipt, color = Color(0xFF2196F3))
                StatItem(label = "আয়", value = "৳২৫,৬৮০", icon = Icons.Default.AttachMoney, color = Color(0xFF4CAF50))
                StatItem(label = "রেটিং", value = "৪.৭", icon = Icons.Default.Star, color = Color(0xFFFFC107))
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class OrderStatus {
    NEW, PREPARING, READY, COMPLETED
}

@Composable
fun OrderCard(
    orderNumber: String,
    customerName: String,
    items: List<String>,
    totalAmount: String,
    time: String,
    status: OrderStatus,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onMarkPreparing: () -> Unit,
    onMarkReady: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                OrderStatus.NEW -> Color(0xFFE3F2FD)
                OrderStatus.PREPARING -> Color(0xFFFFF3E0)
                OrderStatus.READY -> Color(0xFFE8F5E9)
                OrderStatus.COMPLETED -> Color(0xFFF5F5F5)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = orderNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Items
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item, style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "মোট: $totalAmount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (status) {
                    OrderStatus.NEW -> {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাতিল")
                        }
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("গ্রহণ করুন")
                        }
                    }
                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = onMarkReady,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("প্রস্তুত")
                        }
                    }
                    OrderStatus.READY -> {
                        Button(
                            onClick = { /* Notify rider */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Icon(Icons.Default.DeliveryDining, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("রাইডার অপেক্ষারত")
                        }
                    }
                    OrderStatus.COMPLETED -> {}
                }
            }
        }
    }
}
