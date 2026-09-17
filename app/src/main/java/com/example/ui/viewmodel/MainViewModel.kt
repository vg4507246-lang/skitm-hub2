package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ExamResource
import com.example.data.PlacementRecord
import com.example.data.SkitmRepository
import com.example.gemini.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: SkitmRepository,
    private val geminiService: GeminiService = GeminiService()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        viewModelScope.launch {
            repository.prePopulateIfNeeded()
            kotlinx.coroutines.delay(1200) // Artificial delay for premium loading transition
            _isLoading.value = false
        }
    }

    // Auth & Profile State
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName
    
    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private val _userProfile = MutableStateFlow<com.example.data.UserProfile?>(null)
    val userProfile: StateFlow<com.example.data.UserProfile?> = _userProfile

    fun login(name: String, email: String) {
        _userName.value = name
        _userEmail.value = email
        
        viewModelScope.launch {
            var profile = repository.getUserProfile(email)
            if (profile == null) {
                profile = com.example.data.UserProfile(email = email, name = name, education = "", skills = "", experience = "", projects = "")
                repository.saveUserProfile(profile)
            }
            _userProfile.value = profile
        }
    }

    fun saveProfile(education: String, skills: String, experience: String, projects: String) {
        val currentProfile = _userProfile.value
        if (currentProfile != null) {
            val updated = currentProfile.copy(
                education = education,
                skills = skills,
                experience = experience,
                projects = projects
            )
            _userProfile.value = updated
            viewModelScope.launch {
                repository.saveUserProfile(updated)
            }
        }
    }

    // Data State
    val placements: StateFlow<List<PlacementRecord>> = repository.placements
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    val resources: StateFlow<List<ExamResource>> = repository.allResources
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allSyllabusInfo: StateFlow<List<com.example.data.SyllabusInfo>> = repository.syllabusInfo
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allExamSchedules: StateFlow<List<com.example.data.ExamSchedule>> = repository.examSchedules
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val attendanceRecords: StateFlow<List<com.example.data.AttendanceRecord>> = repository.attendanceRecords
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val permissionRequests: StateFlow<List<com.example.data.PermissionRequest>> = repository.permissionRequests
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun submitPermissionRequest(reason: String, type: String, date: String) = viewModelScope.launch {
        repository.submitPermissionRequest(com.example.data.PermissionRequest(
            reason = reason,
            type = type,
            date = date,
            status = "Pending"
        ))
    }

    fun markPresent(id: Int) = viewModelScope.launch { repository.markPresent(id) }
    fun markAbsent(id: Int) = viewModelScope.launch { repository.markAbsent(id) }
    fun addSubjectToTrack(subjectName: String) = viewModelScope.launch {
        repository.insertAttendanceRecord(com.example.data.AttendanceRecord(subjectName = subjectName))
    }

    private val _selectedDepartment = MutableStateFlow("All")
    val selectedDepartment: StateFlow<String> = _selectedDepartment

    fun setDepartment(department: String) {
        _selectedDepartment.value = department
    }

    // ATS Analyzer State
    private val _atsResult = MutableStateFlow("")
    val atsResult: StateFlow<String> = _atsResult
    
    private val _isAtsLoading = MutableStateFlow(false)
    val isAtsLoading: StateFlow<Boolean> = _isAtsLoading

    fun analyzeResume(resumeText: String, jobDescription: String) {
        if (resumeText.isBlank()) return
        _isAtsLoading.value = true
        viewModelScope.launch {
            val result = geminiService.analyzeResumeATS(resumeText, jobDescription)
            _atsResult.value = result
            _isAtsLoading.value = false
        }
    }

    // Chat / Local LLM State
    private val _chatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatHistory: StateFlow<List<Pair<String, String>>> = _chatHistory
    
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading

    fun sendChatMessage(query: String) {
        if (query.isBlank()) return
        val currentHistory = _chatHistory.value.toMutableList()
        _isChatLoading.value = true
        
        viewModelScope.launch {
            val reply = geminiService.chatWithOllamaSubstitute(currentHistory, query)
            currentHistory.add(Pair(query, reply))
            _chatHistory.value = currentHistory
            _isChatLoading.value = false
        }
    }
}
