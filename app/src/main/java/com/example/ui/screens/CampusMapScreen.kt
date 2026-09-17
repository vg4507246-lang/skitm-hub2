package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CampusLocation(
    val id: String,
    val name: String,
    val type: String,
    val description: String,
    val relativeX: Float,
    val relativeY: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMapScreen(onBack: () -> Unit) {
    val locations = remember {
        listOf(
            CampusLocation("A", "Block A", "Department", "Computer Science (CSE) and Information Technology (IT). Contains main server rooms.", 0.2f, 0.2f),
            CampusLocation("B", "Block B", "Department", "Mechanical (ME) and Civil (CE) Engineering. Features heavy machinery labs.", 0.8f, 0.3f),
            CampusLocation("C", "Block C", "Department", "Electronics & Communication (ECE). Features embedded systems lab.", 0.6f, 0.5f),
            CampusLocation("LIB", "Central Library", "Library", "Multi-story library with reading halls and digital resources access.", 0.4f, 0.4f),
            CampusLocation("LAB", "Computer Labs", "Lab", "Advanced computing labs and AI research center.", 0.3f, 0.6f),
            CampusLocation("CAN", "Main Canteen", "Facility", "Food court and main seating area for students.", 0.2f, 0.8f),
            CampusLocation("SPO", "Sports Ground", "Facility", "Football, cricket, and athletic track.", 0.7f, 0.8f)
        )
    }

    var selectedLocation by remember { mutableStateOf<CampusLocation?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive Campus Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.8f, 4f)
                        offset += pan
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                // Background pathways abstraction
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val width = maxWidth
                        val height = maxHeight

                        locations.forEach { location ->
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = width * location.relativeX - 40.dp,
                                        y = height * location.relativeY - 40.dp
                                    )
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (location.type) {
                                            "Department" -> MaterialTheme.colorScheme.primary
                                            "Library" -> MaterialTheme.colorScheme.tertiary
                                            "Lab" -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.secondaryContainer
                                        }
                                    )
                                    .clickable { selectedLocation = location }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = location.name,
                                    color = if (location.type == "Facility") MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Legend
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Legend", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LegendItem("Departments", MaterialTheme.colorScheme.primary)
                    LegendItem("Library", MaterialTheme.colorScheme.tertiary)
                    LegendItem("Labs", MaterialTheme.colorScheme.secondary)
                    LegendItem("Facilities", MaterialTheme.colorScheme.secondaryContainer)
                }
            }
        }

        if (selectedLocation != null) {
            AlertDialog(
                onDismissRequest = { selectedLocation = null },
                title = { Text(selectedLocation!!.name) },
                text = {
                    Column {
                        Text(
                            text = selectedLocation!!.type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = selectedLocation!!.description)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedLocation = null }) {
                        Text("Close")
                    }
                },
                icon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }
    }
}

@Composable
fun LegendItem(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
