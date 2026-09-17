package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISearchScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local LLM (Ollama) Search") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(chatHistory) { turn ->
                    Column(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
                        Text("You:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(turn.first)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("LLM:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(turn.second)
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
                if (isLoading) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about SKITM, exams, or anything...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendChatMessage(query)
                        query = ""
                    },
                    enabled = query.isNotBlank() && !isLoading
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About SKITM & App") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(16.dp)
        ) {
            Text("About SKITM Indore", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Shivajirao Kadam Institute of Technology & Management (SKITM), Indore (formerly Transnational Knowledge Society's Group of Institutions) was founded with the objective to provide premium quality education in Central India.\n\n" +
                "Advantages of this App System:\n" +
                "• All-In-One Hub: Placements, ATS, and Exams in one place.\n" +
                "• On-Device Efficiency: Utilizing local Room databases for instant access without heavy cloud costs.\n" +
                "• AI-Powered: Features ATS analysis and natural language search powered by advanced AI models.\n" +
                "• Optimized Architecture: Developed natively in Android using Jetpack Compose and Kotlin Coroutines for seamless performance.",
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
        }
    }
}
