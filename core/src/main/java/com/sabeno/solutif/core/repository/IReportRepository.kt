package com.sabeno.solutif.core.repository

import android.content.Context
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.sabeno.solutif.core.data.source.Report
import com.sabeno.solutif.core.data.source.User
import com.sabeno.solutif.core.utils.Result


interface IReportRepository {
    suspend fun registerUser(email: String, password: String, context: Context): Result<FirebaseUser?>
    suspend fun createUserFirestore(user: User): Result<Void?>
    suspend fun loginUser(
        email: String,
        password: String
    ): Result<FirebaseUser?>

    suspend fun logoutUser()

    suspend fun checkUserLoggedIn(): FirebaseUser?

    suspend fun getUser(userId: String): Result<User>?

    suspend fun getReportOptions(): FirestoreRecyclerOptions<Report>
    suspend fun getReports(): Result<List<Report>?>
    suspend fun getReportById(reportId: String): Result<Report>?
    suspend fun createReport(report: Report): Result<Void?>
    suspend fun updateReportStatus(reportId: String, isDone: Boolean): Result<Void?>
    suspend fun deleteReport(reportId: String): Result<Void?>
}