package com.example.gemini

import com.example.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {
    
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            systemInstruction = content { text("You are an expert ATS Analyzer and SKITM Hub Assistant. Answer questions concisely and knowledgeably about SKITM college, exams, and general queries.") }
        )
    }

    suspend fun analyzeResumeATS(resumeText: String, jobDescription: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API key is missing. Please configure it in AI Studio Secrets."
        }
        
        val targetContext = if (jobDescription.isNotBlank()) {
            "the following job description or keywords:\n\"\"\"\n$jobDescription\n\"\"\""
        } else {
            "common Software Engineering / Tech placement job requirements"
        }
        
        val prompt = """
            Act as an Applicant Tracking System (ATS). 
            Analyze the following resume against $targetContext.
            
            Please provide a structured analysis:
            1. ATS Match Score: Provide a percentage-based score (0-100%).
            2. Matched Keywords: List the key skills and terms found in both the resume and the target context.
            3. Missing Keywords: List the important skills or terms expected for this role that are missing from the resume.
            4. Suggestions: Provide 3 actionable tips to improve the resume for this specific target.
            
            Format the output as a clean, highly readable summary.
            
            Resume Text:
            \"\"\"
            $resumeText
            \"\"\"
        """.trimIndent()
        
        try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "No analysis available."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun chatWithOllamaSubstitute(history: List<Pair<String, String>>, newQuery: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: API key missing."
        }
        
        val chatHistory = history.flatMap {
            listOf(
                content(role = "user") { text(it.first) },
                content(role = "model") { text(it.second) }
            )
        }

        try {
            val chat = generativeModel.startChat(history = chatHistory)
            val response = chat.sendMessage(newQuery)
            response.text ?: "I'm having trouble thinking."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
