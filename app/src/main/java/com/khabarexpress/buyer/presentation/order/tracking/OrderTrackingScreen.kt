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
import androidx.hilt.navigation.compose.hiltViewModel
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
    modifier: Modifier = Modifier,
    viewModel: OrderTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Load order details on first composition
    LaunchedEffect(orderId) {
        viewModel.loadOrderDetails(orderId)
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
        when (val state = uiState) {
            is OrderTrackingUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is OrderTrackingUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry(orderId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is OrderTrackingUiState.Success -> {
                val order = state.order
                val tracking = state.tracking
                val orderStatus = order.status
                val riderName = order.rider?.name ?: "Rider"
                val riderPhone = order.rider?.phone ?: ""
                val estimatedArrival = tracking?.estimatedArrival?.let {
                    val mins = ((it - System.currentTimeMillis()) / 60000).coerceAtLeast(1)
                    "$mins min"
                } ?: "Calculating..."
                
                // Rider location from tracking
                val riderLatitude = tracking?.riderLocation?.latitude ?: order.rider?.currentLatitude ?: 0.0
                val riderLongitude = tracking?.riderLocation?.longitude ?: order.rider?.currentLongitude ?: 0.0
                val destinationLatitude = order.deliveryAddress.latitude
                val destinationLongitude = order.deliveryAddress.longitude
                
                // Build timeline from tracking data, or derive from current status
                val timeline = tracking?.timeline ?: buildTimelineFromStatus(orderStatus)
        
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
                            restaurantLatitude = order.restaurant.latitude,
                            restaurantLongitude = order.restaurant.longitude,
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
                            text = viewModel.getStatusText(orderStatus),
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
                                model = order.rider?.profileImageUrl,
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
                            if (riderPhone.isNotBlank()) {
                                IconButton(
                                    onClick = {
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
            
            items(timeline.size) { index ->
                TimelineItem(event = timeline[index], isLast = index == timeline.lastIndex)
            }
        }
            }
        }
    }
}

@Composable
fun TimelineItem(event: OrderTimelineEvent, isLast: Boolean = false) {
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
            if (!isLast) {
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

/**
 * Build timeline events from the current order status.
 * This is used as a fallback when tracking data is not yet available.
 */
fun buildTimelineFromStatus(status: OrderStatus): List<OrderTimelineEvent> {
    val now = System.currentTimeMillis()
    val statuses = listOf(
        OrderStatus.PENDING to "Order placed successfully",
        OrderStatus.CONFIRMED to "Order confirmed by restaurant",
        OrderStatus.PREPARING to "Restaurant is preparing your order",
        OrderStatus.READY_FOR_PICKUP to "Order is ready for pickup",
        OrderStatus.PICKED_UP to "Order picked up by delivery partner",
        OrderStatus.ON_THE_WAY to "Your order is on the way",
        OrderStatus.DELIVERED to "Order delivered"
    )
    
    val currentIndex = statuses.indexOfFirst { it.first == status }
    if (currentIndex < 0) return emptyList()
    
    return statuses.take(currentIndex + 1).mapIndexed { index, (s, message) ->
        OrderTimelineEvent(
            status = s,
            timestamp = now - ((currentIndex - index) * 5 * 60 * 1000L),
            message = message
        )
    }.reversed()
}
