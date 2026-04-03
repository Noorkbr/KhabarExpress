package com.khabarexpress.buyer.presentation.checkout.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.presentation.checkout.CheckoutViewModel
import com.khabarexpress.buyer.presentation.components.CustomTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    navController: NavController,
    viewModel: CheckoutViewModel = hiltViewModel(),
    addressId: String? = null
) {
    var houseNo by remember { mutableStateOf("") }
    var road by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Dhaka") }
    var phone by remember { mutableStateOf("") }
    var addressType by remember { mutableStateOf("Home") }
    var latitude by remember { mutableDoubleStateOf(23.8103) }
    var longitude by remember { mutableDoubleStateOf(90.4125) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (addressId == null) "Add Address" else "Edit Address") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MapPlaceholder(
                latitude = latitude,
                longitude = longitude,
                onLocationSelected = { lat, lon ->
                    latitude = lat
                    longitude = lon
                }
            )
            
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Address Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Address Type Selection
                Text(
                    text = "Save as",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AddressTypeChip(
                        icon = Icons.Default.Home,
                        label = "Home",
                        selected = addressType == "Home",
                        onClick = { addressType = "Home" }
                    )
                    AddressTypeChip(
                        icon = Icons.Default.Work,
                        label = "Work",
                        selected = addressType == "Work",
                        onClick = { addressType = "Work" }
                    )
                    AddressTypeChip(
                        icon = Icons.Default.LocationOn,
                        label = "Other",
                        selected = addressType == "Other",
                        onClick = { addressType = "Other" }
                    )
                }
                
                // Address Form
                CustomTextField(
                    value = houseNo,
                    onValueChange = { houseNo = it },
                    label = "House / Flat No.",
                    placeholder = "e.g., 123, Floor 4"
                )
                
                CustomTextField(
                    value = road,
                    onValueChange = { road = it },
                    label = "Road / Street",
                    placeholder = "e.g., Road 5"
                )
                
                CustomTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = "Area",
                    placeholder = "e.g., Dhanmondi, Gulshan"
                )
                
                CustomTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "City",
                    placeholder = "e.g., Dhaka"
                )
                
                CustomTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone Number",
                    placeholder = "+880 1XXX XXXXXX"
                )
                
                // Save Button
                Button(
                    onClick = {
                        val address = Address(
                            id = addressId ?: "",
                            label = addressType,
                            houseNo = houseNo,
                            roadNo = road,
                            area = area,
                            thana = area, // Using area as thana for simplicity
                            district = city,
                            division = "Dhaka", // Default division
                            postalCode = "1000", // Default postal code
                            latitude = latitude,
                            longitude = longitude,
                            isDefault = false
                        )
                        
                        if (addressId == null) {
                            viewModel.addAddress(address)
                        } else {
                            viewModel.updateAddress(address)
                        }
                        
                        navController.navigateUp()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = houseNo.isNotBlank() && area.isNotBlank() && city.isNotBlank()
                ) {
                    Text(
                        text = "Save Address",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MapPlaceholder(
    latitude: Double,
    longitude: Double,
    onLocationSelected: (Double, Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Map View",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Lat: ${"%.4f".format(latitude)}, Lon: ${"%.4f".format(longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Placeholder for current location button
                OutlinedButton(
                    onClick = {
                        // Get current location and update coordinates
                        // This would use location services
                    }
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use Current Location")
                }
                
                Text(
                    text = "Tap on the map to refine your location",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddressTypeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}
