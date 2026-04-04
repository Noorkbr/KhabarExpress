package com.khabarexpress.seller.presentation.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

private val Gold = Color(0xFFD4A03C)
private val Navy = Color(0xFF1B2A4A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemScreen(
    navController: NavController,
    viewModel: MenuViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("15") }
    var isHalal by remember { mutableStateOf(true) }
    var isAvailable by remember { mutableStateOf(true) }
    var spiceLevel by remember { mutableStateOf("None") }

    val spiceLevels = listOf("None", "Mild", "Medium", "Hot", "Extra Hot")

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MenuEvent.ShowSuccess -> navController.navigateUp()
                is MenuEvent.ShowError -> { /* Show error via snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Menu Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category *") },
                placeholder = { Text("e.g., Main Course, Appetizer") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Price (৳) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = prepTime,
                onValueChange = { prepTime = it.filter { c -> c.isDigit() } },
                label = { Text("Prep Time (minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Spice Level Selection
            Text("Spice Level", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spiceLevels.forEach { level ->
                    FilterChip(
                        selected = spiceLevel == level,
                        onClick = { spiceLevel = level },
                        label = { Text(level, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Gold.copy(alpha = 0.2f),
                            selectedLabelColor = Gold
                        )
                    )
                }
            }

            // Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Halal")
                Switch(
                    checked = isHalal,
                    onCheckedChange = { isHalal = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Gold)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Available")
                Switch(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Gold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val priceValue = price.toDoubleOrNull()
                    if (name.isNotBlank() && category.isNotBlank() && priceValue != null) {
                        viewModel.createMenuItem(
                            name = name,
                            description = description.ifBlank { null },
                            category = category,
                            price = priceValue * 100, // Convert to paisa
                            isAvailable = isAvailable,
                            isHalal = isHalal,
                            spiceLevel = spiceLevel,
                            prepTime = prepTime.toIntOrNull() ?: 15
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                enabled = name.isNotBlank() && category.isNotBlank() && price.isNotBlank()
            ) {
                Text("Add Item", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
