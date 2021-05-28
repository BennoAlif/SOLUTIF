package com.sabeno.solutif.repository

import com.google.firebase.auth.FirebaseUser
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
}