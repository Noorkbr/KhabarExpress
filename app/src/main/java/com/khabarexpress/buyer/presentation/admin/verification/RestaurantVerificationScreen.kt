package com.khabarexpress.buyer.presentation.admin.verification

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.khabarexpress.buyer.data.remote.dto.PendingRestaurantDto
import com.khabarexpress.buyer.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantVerificationScreen(
    navController: NavController,
    viewModel: RestaurantVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Restaurant Verification",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Gold,
                    navigationIconContentColor = Gold
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Gold)
            }
        } else if (uiState.restaurants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Success
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "All caught up!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                    Text(
                        "No pending restaurants to verify",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Warning)
                            Text(
                                "${uiState.restaurants.size} restaurant(s) pending verification",
                                fontWeight = FontWeight.SemiBold,
                                color = Warning
                            )
                        }
                    }
                }

                items(uiState.restaurants) { restaurant ->
                    PendingRestaurantCard(
                        restaurant = restaurant,
                        isApproving = uiState.processingId == restaurant.id,
                        onApprove = { viewModel.approveRestaurant(restaurant.id) },
                        onReject = { reason -> viewModel.rejectRestaurant(restaurant.id, reason) }
                    )
                }
            }
        }

        // Show snackbar for messages
        uiState.message?.let { message ->
            LaunchedEffect(message) {
                viewModel.clearMessage()
            }
        }
    }
}

@Composable
private fun PendingRestaurantCard(
    restaurant: PendingRestaurantDto,
    isApproving: Boolean,
    onApprove: () -> Unit,
    onReject: (String) -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Restaurant Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        restaurant.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    restaurant.nameBn?.let {
                        Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Warning.copy(alpha = 0.15f)
                ) {
                    Text(
                        "PENDING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Warning
                    )
                }
            }

            HorizontalDivider(color = Divider)

            // Details
            InfoRow(icon = Icons.Default.Phone, label = "Phone", value = restaurant.phone)
            restaurant.email?.let {
                InfoRow(icon = Icons.Default.Email, label = "Email", value = it)
            }
            InfoRow(
                icon = Icons.Default.Category,
                label = "Category",
                value = restaurant.category
            )
            if (restaurant.cuisines.isNotEmpty()) {
                InfoRow(
                    icon = Icons.Default.Fastfood,
                    label = "Cuisines",
                    value = restaurant.cuisines.joinToString(", ")
                )
            }
            restaurant.address?.let { addr ->
                val addressText = listOfNotNull(
                    addr.area, addr.thana, addr.district
                ).joinToString(", ")
                if (addressText.isNotEmpty()) {
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        value = addressText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    enabled = !isApproving
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    enabled = !isApproving
                ) {
                    if (isApproving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve")
                    }
                }
            }
        }
    }

    // Reject Dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Restaurant") },
            text = {
                Column {
                    Text("Provide a reason for rejecting \"${restaurant.name}\":")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (rejectReason.isNotBlank()) {
                            onReject(rejectReason)
                            showRejectDialog = false
                            rejectReason = ""
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "$label: ",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
