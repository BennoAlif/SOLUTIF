package com.sabeno.solutif.repository

import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.sabeno.solutif.data.source.Report
import com.sabeno.solutif.data.source.User
import com.sabeno.solutif.utils.Result


interface IReportRepository {
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
}