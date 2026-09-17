package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "placement_records")
data class PlacementRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val company: String,
    val highestPackageLpa: Double,
    val medianPackageLpa: Double,
    val requiredSkills: String,
    val year: String,
    val department: String
)

@Serializable
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val name: String,
    val education: String,
    val skills: String,
    val experience: String,
    val projects: String
)

@Serializable
@Entity(tableName = "syllabus_info")
data class SyllabusInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val branch: String,
    val semester: Int,
    val details: String,
    val link: String
)

@Serializable
@Entity(tableName = "exam_schedules")
data class ExamSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val department: String
)

@Serializable
@Entity(tableName = "exam_resources")
data class ExamResource(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val type: String, // "Syllabus", "PYQ", "Timetable", "CampusOps"
    val content: String,
    val link: String
)

@Serializable
@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val totalClasses: Int = 0,
    val attendedClasses: Int = 0
)

@Serializable
@Entity(tableName = "permission_requests")
data class PermissionRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reason: String,
    val type: String, // Event, Leave, Outpass
    val date: String,
    val status: String // Pending, Approved, Rejected
)

@Dao
interface SkitmDao {
    @Query("SELECT * FROM placement_records ORDER BY highestPackageLpa DESC")
    fun getPlacementRecords(): Flow<List<PlacementRecord>>

    @Query("SELECT * FROM exam_resources WHERE type = :type")
    fun getResourcesByType(type: String): Flow<List<ExamResource>>

    @Query("SELECT * FROM exam_resources")
    fun getAllResources(): Flow<List<ExamResource>>

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getUserProfile(email: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("SELECT * FROM syllabus_info")
    fun getSyllabusInfo(): Flow<List<SyllabusInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabus(syllabus: List<SyllabusInfo>)

    @Query("SELECT * FROM exam_schedules")
    fun getExamSchedules(): Flow<List<ExamSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamSchedules(schedules: List<ExamSchedule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacements(records: List<PlacementRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<ExamResource>)
    
    @Query("SELECT COUNT(*) FROM placement_records")
    suspend fun getPlacementsCount(): Int

    @Query("SELECT * FROM attendance_records")
    fun getAttendanceRecords(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecord>)

    @Query("UPDATE attendance_records SET attendedClasses = attendedClasses + 1, totalClasses = totalClasses + 1 WHERE id = :id")
    suspend fun markPresent(id: Int)

    @Query("UPDATE attendance_records SET totalClasses = totalClasses + 1 WHERE id = :id")
    suspend fun markAbsent(id: Int)

    @Query("SELECT COUNT(*) FROM attendance_records")
    suspend fun getAttendanceCount(): Int

    @Query("SELECT * FROM permission_requests ORDER BY id DESC")
    fun getPermissionRequests(): Flow<List<PermissionRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermissionRequest(request: PermissionRequest)
}

@Database(
    entities = [
        PlacementRecord::class, 
        ExamResource::class, 
        UserProfile::class, 
        SyllabusInfo::class, 
        ExamSchedule::class,
        AttendanceRecord::class,
        PermissionRequest::class
    ], 
    version = 5, 
    exportSchema = false
)
abstract class SkitmDatabase : RoomDatabase() {
    abstract fun skitmDao(): SkitmDao
}

class SkitmRepository(private val dao: SkitmDao) {
    val placements: Flow<List<PlacementRecord>> = dao.getPlacementRecords()
    val allResources: Flow<List<ExamResource>> = dao.getAllResources()
    val syllabusInfo: Flow<List<SyllabusInfo>> = dao.getSyllabusInfo()
    val examSchedules: Flow<List<ExamSchedule>> = dao.getExamSchedules()
    val attendanceRecords: Flow<List<AttendanceRecord>> = dao.getAttendanceRecords()
    val permissionRequests: Flow<List<PermissionRequest>> = dao.getPermissionRequests()

    suspend fun getUserProfile(email: String) = dao.getUserProfile(email)
    suspend fun saveUserProfile(profile: UserProfile) = dao.saveUserProfile(profile)

    fun getResourcesByType(type: String) = dao.getResourcesByType(type)
    
    suspend fun insertAttendanceRecord(record: AttendanceRecord) = dao.insertAttendanceRecord(record)
    suspend fun markPresent(id: Int) = dao.markPresent(id)
    suspend fun markAbsent(id: Int) = dao.markAbsent(id)

    suspend fun submitPermissionRequest(request: PermissionRequest) = dao.insertPermissionRequest(request)

    suspend fun prePopulateIfNeeded() {
        if (dao.getAttendanceCount() == 0) {
            val attendance = listOf(
                AttendanceRecord(subjectName = "Computer Networks", totalClasses = 20, attendedClasses = 16),
                AttendanceRecord(subjectName = "Data Structures", totalClasses = 25, attendedClasses = 18),
                AttendanceRecord(subjectName = "Operating Systems", totalClasses = 15, attendedClasses = 10)
            )
            dao.insertAttendanceRecords(attendance)
        }

        if (dao.getPlacementsCount() == 0) {
            val placements = listOf(
                PlacementRecord(company = "TCS Digital", highestPackageLpa = 7.5, medianPackageLpa = 4.0, requiredSkills = "Java, DSA, DBMS, Aptitude", year = "2023", department = "CSE"),
                PlacementRecord(company = "Infosys", highestPackageLpa = 8.0, medianPackageLpa = 4.5, requiredSkills = "Python, OOPS, Logical Reasoning", year = "2023", department = "IT"),
                PlacementRecord(company = "Wipro", highestPackageLpa = 6.5, medianPackageLpa = 4.0, requiredSkills = "C++, Networking, Communication", year = "2023", department = "ECE"),
                PlacementRecord(company = "Cognizant", highestPackageLpa = 10.0, medianPackageLpa = 5.0, requiredSkills = "Java, Spring Boot, React", year = "2023", department = "CSE"),
                PlacementRecord(company = "Amazon (AWS)", highestPackageLpa = 28.0, medianPackageLpa = 18.0, requiredSkills = "Advanced DSA, System Design, Cloud", year = "2023", department = "CSE"),
                PlacementRecord(company = "L&T Construction", highestPackageLpa = 6.0, medianPackageLpa = 4.0, requiredSkills = "AutoCAD, Structural Analysis", year = "2023", department = "CE"),
                PlacementRecord(company = "Tata Motors", highestPackageLpa = 8.5, medianPackageLpa = 5.5, requiredSkills = "Thermodynamics, CAD, Mechanics", year = "2023", department = "ME")
            )
            dao.insertPlacements(placements)

            val resources = listOf(
                ExamResource(subject = "Data Structures", type = "PYQ", content = "2020-2022 Question Papers", link = "https://skitm.ac.in/cs/ds-pyq"),
                ExamResource(subject = "Operating Systems", type = "PYQ", content = "2021 Question Paper", link = "https://skitm.ac.in/cs/os-pyq"),
                ExamResource(subject = "Computer Networks", type = "Syllabus", content = "RGPV Scheme Syllabus", link = "https://skitm.ac.in/cs/cn-syllabus"),
                ExamResource(subject = "Mid Semester Exam", type = "Timetable", content = "Starts 15th Oct. 10AM - 1PM", link = ""),
                ExamResource(subject = "Hostel & Bus", type = "CampusOps", content = "Bus Routes updated for Winter. Hostel fee submission deadline is 25th Nov.", link = ""),
                ExamResource(subject = "Library Info", type = "CampusOps", content = "Library timings extended to 8 PM during exam week.", link = "")
            )
            dao.insertResources(resources)
            
            val syllabuses = listOf(
                SyllabusInfo(subject = "Computer Networks", branch = "CSE", semester = 5, details = "OSI Model, TCP/IP, Routing Protocols", link = "https://skitm.ac.in/cs/cn-syllabus"),
                SyllabusInfo(subject = "Data Structures", branch = "CSE", semester = 3, details = "Trees, Graphs, DP, Sorting", link = "https://skitm.ac.in/cs/ds-syllabus")
            )
            dao.insertSyllabus(syllabuses)

            val schedules = listOf(
                ExamSchedule(title = "Mid Semester Exam - CN", date = "15th Oct 2024", time = "10:00 AM - 1:00 PM", location = "Block A, Room 101", department = "CSE"),
                ExamSchedule(title = "End Semester Exam - DS", date = "20th Nov 2024", time = "2:00 PM - 5:00 PM", location = "Main Hall", department = "CSE"),
                ExamSchedule(title = "Thermodynamics Exam", date = "18th Nov 2024", time = "10:00 AM - 1:00 PM", location = "Block B, Room 202", department = "ME"),
                ExamSchedule(title = "Circuit Theory Exam", date = "16th Oct 2024", time = "2:00 PM - 5:00 PM", location = "Block C, Room 301", department = "ECE"),
                ExamSchedule(title = "Data Mining", date = "21st Nov 2024", time = "10:00 AM - 1:00 PM", location = "Block A, Room 104", department = "IT")
            )
            dao.insertExamSchedules(schedules)
        }
    }
}
