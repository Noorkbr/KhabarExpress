package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.ui.theme.*

/**
 * Address card component
 * 
 * Displays address information with label, full address, default badge,
 * and edit/delete actions.
 * 
 * @param address Address data
 * @param isSelected Whether this address is selected
 * @param onSelect Callback when address is selected
 * @param onEdit Callback when edit button is clicked
 * @param onDelete Callback when delete button is clicked
 * @param showActions Whether to show edit/delete actions
 * @param modifier Modifier for customization
 */
@Composable
fun AddressCard(
    address: Address,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    showActions: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelect,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Address label
                        Surface(
                            color = when (address.label) {
                                "Home" -> Success.copy(alpha = 0.15f)
                                "Work" -> Info.copy(alpha = 0.15f)
                                else -> Primary.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (address.label) {
                                        "Home" -> Icons.Filled.Home
                                        "Work" -> Icons.Filled.Work
                                        else -> Icons.Filled.LocationOn
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = when (address.label) {
                                        "Home" -> Success
                                        "Work" -> Info
                                        else -> Primary
                                    }
                                )
                                Text(
                                    text = address.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when (address.label) {
                                        "Home" -> Success
                                        "Work" -> Info
                                        else -> Primary
                                    }
                                )
                            }
                        }
                        
                        // Default badge
                        if (address.isDefault) {
                            Surface(
                                color = Primary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Default",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Full address
                    Text(
                        text = buildAddressString(address),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!address.deliveryInstructions.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Note: ${address.deliveryInstructions}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                if (showActions) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit address",
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete address",
                                tint = Error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildAddressString(address: Address): String {
    return buildString {
        append("House: ${address.houseNo}, Road: ${address.roadNo}")
        append("\n${address.area}, ${address.thana}")
        append("\n${address.district}, ${address.division} - ${address.postalCode}")
        if (!address.landmark.isNullOrEmpty()) {
            append("\nLandmark: ${address.landmark}")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddressCardPreview() {
    KhabarExpressTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AddressCard(
                address = Address(
                    id = "1",
                    label = "Home",
                    houseNo = "42",
                    roadNo = "12A",
                    area = "Dhanmondi",
                    thana = "Dhanmondi",
                    district = "Dhaka",
                    division = "Dhaka",
                    postalCode = "1209",
                    landmark = "Near Abahani Field",
                    isDefault = true,
                    latitude = 23.8103,
                    longitude = 90.4125
                ),
                isSelected = true
            )
            
            AddressCard(
                address = Address(
                    id = "2",
                    label = "Work",
                    houseNo = "15",
                    roadNo = "7",
                    area = "Uttara",
                    thana = "Uttara",
                    district = "Dhaka",
                    division = "Dhaka",
                    postalCode = "1230",
                    deliveryInstructions = "Call before delivery",
                    isDefault = false,
                    latitude = 23.8103,
                    longitude = 90.4125
                ),
                isSelected = false
            )
        }
    }
}
