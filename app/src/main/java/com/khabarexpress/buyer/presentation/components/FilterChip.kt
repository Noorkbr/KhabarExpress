package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khabarexpress.buyer.ui.theme.KhabarExpressTheme
import com.khabarexpress.buyer.ui.theme.Primary
import com.khabarexpress.buyer.ui.theme.SurfaceVariant
import com.khabarexpress.buyer.ui.theme.TextPrimary

/**
 * Filter chip component
 * 
 * Used for category selection, filters, etc. with selected/unselected states
 * 
 * @param label Chip label text
 * @param isSelected Whether the chip is selected
 * @param onClick Click handler
 * @param modifier Modifier for customization
 * @param icon Optional leading icon
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val backgroundColor = if (isSelected) Primary else SurfaceVariant
    val contentColor = if (isSelected) Color.White else TextPrimary
    
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

/**
 * Group of filter chips for easy selection
 * 
 * @param items List of chip labels
 * @param selectedItem Currently selected item
 * @param onItemSelect Callback when item is selected
 * @param modifier Modifier for customization
 */
@Composable
fun FilterChipGroup(
    items: List<String>,
    selectedItem: String,
    onItemSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FilterChip(
                label = item,
                isSelected = item == selectedItem,
                onClick = { onItemSelect(item) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipPreview() {
    KhabarExpressTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var selected by remember { mutableStateOf("All") }
            
            FilterChipGroup(
                items = listOf("All", "Pizza", "Burger", "Asian", "Dessert"),
                selectedItem = selected,
                onItemSelect = { selected = it }
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    label = "Selected",
                    isSelected = true,
                    onClick = {}
                )
                
                FilterChip(
                    label = "Not Selected",
                    isSelected = false,
                    onClick = {}
                )
            }
        }
    }
}
