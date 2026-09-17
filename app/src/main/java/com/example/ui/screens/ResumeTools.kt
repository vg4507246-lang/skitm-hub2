package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.MainViewModel
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast

fun exportResumeToPdf(context: Context, uri: Uri, name: String, email: String, education: String, experience: String, skills: String, projects: String) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()

        var currentY = 50f
        val leftMargin = 50f
        val rightMargin = pageInfo.pageWidth - 50f
        val textWidth = rightMargin - leftMargin

        // Draw Name
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.BLACK
        canvas.drawText(name, leftMargin, currentY, paint)
        currentY += 30f

        // Draw Email
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.DKGRAY
        canvas.drawText(email, leftMargin, currentY, paint)
        currentY += 50f

        fun drawSection(title: String, content: String) {
            paint.textSize = 16f
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.rgb(0, 102, 204) // Material Primary roughly
            canvas.drawText(title, leftMargin, currentY, paint)
            currentY += 25f

            paint.textSize = 12f
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.BLACK
            
            val words = content.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > textWidth) {
                    canvas.drawText(line, leftMargin, currentY, paint)
                    currentY += 20f
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, leftMargin, currentY, paint)
                currentY += 40f
            }
        }

        drawSection("EDUCATION", education.ifBlank { "B.Tech Computer Science - SKITM Indore" })
        drawSection("EXPERIENCE", experience.ifBlank { "No experience listed" })
        drawSection("SKILLS", skills.ifBlank { "No skills listed" })
        drawSection("PROJECTS", projects.ifBlank { "No projects listed" })

        pdfDocument.finishPage(page)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()
        Toast.makeText(context, "Resume saved successfully!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save resume", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeBuilderScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var skills by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var projects by remember { mutableStateOf("") }
    var showPreview by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            uri?.let {
                exportResumeToPdf(context, it, userName, userEmail, education, experience, skills, projects)
            }
        }
    )

    LaunchedEffect(userProfile) {
        userProfile?.let {
            skills = it.skills
            education = it.education
            experience = it.experience
            projects = it.projects
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Builder") },
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
                .verticalScroll(rememberScrollState())
        ) {
            if (!showPreview) {
                OutlinedTextField(value = userName, onValueChange = {}, label = { Text("Name") }, readOnly = true, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = userEmail, onValueChange = {}, label = { Text("Email") }, readOnly = true, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = education, onValueChange = { education = it }, label = { Text("Education") }, placeholder = { Text("e.g., B.Tech CS, SKITM") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("Skills") }, placeholder = { Text("e.g., Kotlin, Python, React") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(value = experience, onValueChange = { experience = it }, label = { Text("Experience") }, placeholder = { Text("e.g., Intern at TCS") }, modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 8.dp))
                OutlinedTextField(value = projects, onValueChange = { projects = it }, label = { Text("Projects") }, modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 16.dp))

                Button(
                    onClick = { 
                        viewModel.saveProfile(education, skills, experience, projects)
                        showPreview = true 
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Save & Generate Preview")
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(userEmail, style = MaterialTheme.typography.bodyMedium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("EDUCATION", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(if (education.isBlank()) "B.Tech Computer Science - SKITM Indore" else education)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("EXPERIENCE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(if (experience.isBlank()) "No experience listed" else experience)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SKILLS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(if (skills.isBlank()) "No skills listed" else skills)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("PROJECTS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(if (projects.isBlank()) "No projects listed" else projects)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showPreview = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit Details")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { 
                        createDocumentLauncher.launch("${userName.replace(" ", "_")}_Resume.pdf")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Export as PDF")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ATSScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    var resumeText by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    val atsResult by viewModel.atsResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAtsLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ATS Resume Analyzer") },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text("Paste your resume and an optional target job description to get a percentage-based ATS match score and keyword analysis.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = jobDescription,
                onValueChange = { jobDescription = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                label = { Text("Job Description / Keywords (Optional)") },
                placeholder = { Text("Paste the target job description here...") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = resumeText,
                onValueChange = { resumeText = it },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                label = { Text("Resume Text") },
                placeholder = { Text("Paste your resume here...") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.analyzeResume(resumeText, jobDescription) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = resumeText.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Analyze Resume")
                }
            }
            
            if (atsResult.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("ATS Analysis Result:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(atsResult, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
