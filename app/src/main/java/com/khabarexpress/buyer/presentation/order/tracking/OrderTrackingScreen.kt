package com.khabarexpress.buyer.presentation.order.tracking

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.khabarexpress.buyer.domain.model.OrderStatus
import com.khabarexpress.buyer.domain.model.OrderTimelineEvent
import com.khabarexpress.buyer.presentation.components.map.DeliveryTrackingMap
import com.khabarexpress.buyer.ui.theme.Primary
import com.khabarexpress.buyer.ui.theme.Success
import java.text.SimpleDateFormat
import java.util.*

/**
 * Order Tracking Screen
 * Displays real-time order tracking with rider location on map
 * 
 * Note: Sample data is used for demonstration. In production:
 * - Order data should come from ViewModel/Repository
 * - Location updates should use SocketManager.riderLocationUpdates flow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Sample data - in production this would come from ViewModel with SocketManager integration
    val orderStatus = OrderStatus.ON_THE_WAY
    val timeline = getSampleTimeline()
    val riderName = "John Smith"
    val riderPhone = "+1 (555) 987-6543"
    val estimatedArrival = "15 min"
    
    // Sample coordinates for Dhaka, Bangladesh (typical delivery scenario)
    // In production, these would come from SocketManager.riderLocationUpdates flow
    var riderLatitude by remember { mutableDoubleStateOf(23.7937) }
    var riderLongitude by remember { mutableDoubleStateOf(90.4066) }
    val destinationLatitude = 23.7806
    val destinationLongitude = 90.4195
    val restaurantLatitude = 23.8103
    val restaurantLongitude = 90.4125
    
    // Simulate rider movement for demo (replace with SocketManager.riderLocationUpdates in production)
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        riderLatitude = 23.7880
        riderLongitude = 90.4100
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Order") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order ID
            item {
                Text(
                    text = "Order #$orderId",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Live Tracking Map (when order is picked up or on the way)
            if (orderStatus == OrderStatus.PICKED_UP || orderStatus == OrderStatus.ON_THE_WAY) {
                item {
                    Column {
                        Text(
                            text = "Live Tracking",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        DeliveryTrackingMap(
                            riderLatitude = riderLatitude,
                            riderLongitude = riderLongitude,
                            destinationLatitude = destinationLatitude,
                            destinationLongitude = destinationLongitude,
                            restaurantLatitude = restaurantLatitude,
                            restaurantLongitude = restaurantLongitude,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when (orderStatus) {
                                OrderStatus.PENDING -> Icons.Filled.Schedule
                                OrderStatus.CONFIRMED -> Icons.Filled.CheckCircle
                                OrderStatus.PREPARING -> Icons.Filled.Restaurant
                                OrderStatus.READY_FOR_PICKUP -> Icons.Filled.Done
                                OrderStatus.PICKED_UP -> Icons.Filled.LocalShipping
                                OrderStatus.ON_THE_WAY -> Icons.Filled.LocalShipping
                                OrderStatus.DELIVERED -> Icons.Filled.TaskAlt
                                OrderStatus.CANCELLED -> Icons.Filled.Cancel
                            },
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = orderStatus.name.replace("_", " "),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estimated arrival: $estimatedArrival",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Rider Info (when order is picked up or on the way)
            if (orderStatus == OrderStatus.PICKED_UP || orderStatus == OrderStatus.ON_THE_WAY) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = "https://via.placeholder.com/60",
                                contentDescription = "Rider Photo",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = riderName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Delivery Partner",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    // Launch phone dialer with rider phone number
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$riderPhone")
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Phone,
                                    contentDescription = "Call",
                                    tint = Primary
                                )
                            }
                        }
                    }
                }
            }
            
            // Timeline
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Order Timeline",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            items(timeline) { event ->
                TimelineItem(event = event)
            }
        }
    }
}

@Composable
fun TimelineItem(event: OrderTimelineEvent) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Success
            )
            if (event != getSampleTimeline().last()) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .padding(vertical = 4.dp)
                ) {
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.message,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateFormat.format(Date(event.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getSampleTimeline() = listOf(
    OrderTimelineEvent(
        status = OrderStatus.ON_THE_WAY,
        timestamp = System.currentTimeMillis(),
        message = "Your order is on the way"
    ),
    OrderTimelineEvent(
        status = OrderStatus.PICKED_UP,
        timestamp = System.currentTimeMillis() - 10 * 60 * 1000,
        message = "Order picked up by delivery partner"
    ),
    OrderTimelineEvent(
        status = OrderStatus.PREPARING,
        timestamp = System.currentTimeMillis() - 20 * 60 * 1000,
        message = "Restaurant is preparing your order"
    ),
    OrderTimelineEvent(
        status = OrderStatus.CONFIRMED,
        timestamp = System.currentTimeMillis() - 25 * 60 * 1000,
        message = "Order confirmed by restaurant"
    ),
    OrderTimelineEvent(
        status = OrderStatus.PENDING,
        timestamp = System.currentTimeMillis() - 30 * 60 * 1000,
        message = "Order placed successfully"
    )
)
