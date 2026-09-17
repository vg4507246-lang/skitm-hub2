package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.shimmerEffect
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, onNavigate: (Any) -> Unit) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val selectedDepartment by viewModel.selectedDepartment.collectAsStateWithLifecycle()
    val syllabuses by viewModel.allSyllabusInfo.collectAsStateWithLifecycle()
    val placements by viewModel.placements.collectAsStateWithLifecycle()

    val departments = listOf("All", "CSE", "IT", "ECE", "ME", "CE")
    
    val filteredSyllabuses = if (selectedDepartment == "All") syllabuses else syllabuses.filter { it.branch == selectedDepartment }
    val filteredPlacements = if (selectedDepartment == "All") placements else placements.filter { it.department == selectedDepartment }

    val highestPackage = filteredPlacements.maxOfOrNull { it.highestPackageLpa } ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SKITM Hub", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onNavigate(DigitalIDRoute) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            DashboardShimmer(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Hero Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                    painter = painterResource(id = R.drawable.campus_hero_1789668340653),
                    contentDescription = "Campus Hero",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000)),
                                startY = 100f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = userName.ifBlank { "Student" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ScrollableTabRow(
                selectedTabIndex = departments.indexOf(selectedDepartment),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                indicator = {} // Hide default indicator for custom chips
            ) {
                departments.forEachIndexed { index, title ->
                    val isSelected = selectedDepartment == title
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { viewModel.setDepartment(title) }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                text = title,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Active Docs",
                    value = "${filteredSyllabuses.size}",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Highest Pkg",
                    value = "$highestPackage LPA",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Menu
            val menuItems = listOf(
                DashboardItem("Academics", Icons.Default.MenuBook, AcademicsRoute, Color(0xFF3B82F6)),
                DashboardItem("Attendance", Icons.Default.CheckCircle, AttendanceRoute, Color(0xFF10B981)),
                DashboardItem("Timetable", Icons.Default.Schedule, TimetableRoute, Color(0xFFF59E0B)),
                DashboardItem("Results", Icons.Default.Grade, ResultsRoute, Color(0xFF8B5CF6)),
                DashboardItem("Permissions", Icons.Default.Security, PermissionsRoute, Color(0xFFEF4444)),
                DashboardItem("Events", Icons.Default.Event, EventsRoute, Color(0xFFEC4899)),
                DashboardItem("Faculty", Icons.Default.People, FacultyRoute, Color(0xFF6366F1)),
                DashboardItem("Placements", Icons.Default.Work, PlacementsRoute, Color(0xFF14B8A6)),
                DashboardItem("Resume Builder", Icons.Default.Description, ResumeBuilderRoute, Color(0xFF8B5CF6)),
                DashboardItem("ATS Analyzer", Icons.Default.Analytics, ATSRoute, Color(0xFF3B82F6)),
                DashboardItem("Campus Map", Icons.Default.LocationOn, CampusMapRoute, Color(0xFF10B981)),
                DashboardItem("AI Search", Icons.Default.Search, AISearchRoute, Color(0xFFF59E0B))
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(menuItems) { item ->
                    DashboardCard(item = item, onClick = { onNavigate(item.route) })
                }
            }
        }
    }
    }
}

@Composable
private fun DashboardShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .shimmerEffect()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(2) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .shimmerEffect()
                )
            }
        }
        repeat(4) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(2) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, containerColor: Color, contentColor: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = contentColor.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = contentColor)
        }
    }
}

data class DashboardItem(val title: String, val icon: ImageVector, val route: Any, val color: Color)

@Composable
fun DashboardCard(item: DashboardItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    modifier = Modifier.size(24.dp),
                    tint = item.color
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
