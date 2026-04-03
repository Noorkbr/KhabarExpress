package com.khabarexpress.buyer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.khabarexpress.buyer.domain.model.MenuItem

data class CustomizationOption(
    val id: String,
    val name: String,
    val price: Double
)

data class CustomizationGroup(
    val id: String,
    val name: String,
    val required: Boolean = false,
    val multiSelect: Boolean = false,
    val options: List<CustomizationOption>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDetailSheet(
    menuItem: MenuItem,
    onDismiss: () -> Unit,
    onAddToCart: (Int, List<String>, String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedSize by remember { mutableStateOf<String?>(null) }
    var selectedAddons by remember { mutableStateOf<Set<String>>(emptySet()) }
    var spiceLevel by remember { mutableStateOf<String?>(null) }
    var specialInstructions by remember { mutableStateOf("") }
    
    // Mock customization groups (in real app, these come from MenuItem)
    val customizationGroups = remember {
        listOf(
            CustomizationGroup(
                id = "size",
                name = "Size",
                required = true,
                multiSelect = false,
                options = listOf(
                    CustomizationOption("small", "Small", 0.0),
                    CustomizationOption("medium", "Medium", 2.0),
                    CustomizationOption("large", "Large", 4.0)
                )
            ),
            CustomizationGroup(
                id = "addons",
                name = "Add-ons",
                required = false,
                multiSelect = true,
                options = listOf(
                    CustomizationOption("extra_cheese", "Extra Cheese", 1.5),
                    CustomizationOption("bacon", "Bacon", 2.0),
                    CustomizationOption("mushrooms", "Mushrooms", 1.0),
                    CustomizationOption("olives", "Olives", 0.5)
                )
            ),
            CustomizationGroup(
                id = "spice",
                name = "Spice Level",
                required = false,
                multiSelect = false,
                options = listOf(
                    CustomizationOption("mild", "Mild", 0.0),
                    CustomizationOption("medium", "Medium", 0.0),
                    CustomizationOption("hot", "Hot", 0.0),
                    CustomizationOption("extra_hot", "Extra Hot", 0.0)
                )
            )
        )
    }
    
    // Calculate total price
    val basePrice = menuItem.price
    val sizePrice = customizationGroups.find { it.id == "size" }
        ?.options?.find { it.id == selectedSize }?.price ?: 0.0
    val addonsPrice = customizationGroups.find { it.id == "addons" }
        ?.options?.filter { it.id in selectedAddons }?.sumOf { it.price } ?: 0.0
    val totalPrice = (basePrice + sizePrice + addonsPrice) * quantity
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with image and close button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = menuItem.imageUrl,
                        contentDescription = menuItem.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
            
            // Item name and description
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = menuItem.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = menuItem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "৳${menuItem.price}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Customization groups
            items(customizationGroups) { group ->
                CustomizationGroupSection(
                    group = group,
                    selectedOptions = when (group.id) {
                        "size" -> selectedSize?.let { setOf(it) } ?: emptySet()
                        "addons" -> selectedAddons
                        "spice" -> spiceLevel?.let { setOf(it) } ?: emptySet()
                        else -> emptySet()
                    },
                    onOptionSelected = { optionId ->
                        when (group.id) {
                            "size" -> selectedSize = optionId
                            "addons" -> {
                                selectedAddons = if (optionId in selectedAddons) {
                                    selectedAddons - optionId
                                } else {
                                    selectedAddons + optionId
                                }
                            }
                            "spice" -> spiceLevel = optionId
                        }
                    },
                    multiSelect = group.multiSelect
                )
            }
            
            // Special instructions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Special Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    OutlinedTextField(
                        value = specialInstructions,
                        onValueChange = { specialInstructions = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Any special requests?") },
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }
            
            // Quantity selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    QuantitySelector(
                        quantity = quantity,
                        onQuantityChange = { quantity = it.coerceIn(1, 99) }
                    )
                }
            }
        }
        
        // Bottom bar with Add to Cart button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "৳${"%.2f".format(totalPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = {
                        val customizations = buildList {
                            selectedSize?.let { add("Size: $it") }
                            if (selectedAddons.isNotEmpty()) {
                                add("Addons: ${selectedAddons.joinToString()}")
                            }
                            spiceLevel?.let { add("Spice: $it") }
                        }
                        onAddToCart(quantity, customizations, specialInstructions)
                        onDismiss()
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Cart")
                }
            }
        }
    }
}

@Composable
fun CustomizationGroupSection(
    group: CustomizationGroup,
    selectedOptions: Set<String>,
    onOptionSelected: (String) -> Unit,
    multiSelect: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (group.required) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Required",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            group.options.forEach { option ->
                CustomizationOptionItem(
                    option = option,
                    selected = option.id in selectedOptions,
                    onClick = { onOptionSelected(option.id) },
                    multiSelect = multiSelect
                )
            }
        }
    }
}

@Composable
fun CustomizationOptionItem(
    option: CustomizationOption,
    selected: Boolean,
    onClick: () -> Unit,
    multiSelect: Boolean
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        },
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (multiSelect) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = null
                    )
                } else {
                    RadioButton(
                        selected = selected,
                        onClick = null
                    )
                }
                
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (option.price > 0) {
                Text(
                    text = "+৳${"%.2f".format(option.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
