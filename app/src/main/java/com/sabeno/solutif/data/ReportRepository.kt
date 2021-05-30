package com.sabeno.solutif.data

import android.util.Log
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sabeno.solutif.data.source.Report
import com.sabeno.solutif.data.source.User
import com.sabeno.solutif.repository.IReportRepository
import com.sabeno.solutif.utils.await
import com.sabeno.solutif.utils.Result

class ReportRepository : IReportRepository {
    private val TAG = "ReportRepository"

    private val firestoreInstance = Firebase.firestore
    private val userCollection = firestoreInstance.collection("users")
    private val reportCollection = firestoreInstance.collection("reports")

    private val firebaseAuth = Firebase.auth

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    override suspend fun loginUser(email: String, password: String): Result<FirebaseUser?> {
        try {
            return when (val resultDocumentSnapshot =
                firebaseAuth.signInWithEmailAndPassword(email, password).await()) {
                is Result.Success -> {
                    Log.i(TAG, "Result.Success")
                    val firebaseUser = resultDocumentSnapshot.data.user
                    Result.Success(firebaseUser)
                }
                is Result.Error -> {
                    Log.e(TAG, "${resultDocumentSnapshot.exception}")
                    Result.Error(resultDocumentSnapshot.exception)
                }
                is Result.Canceled -> {
                    Log.e(TAG, "${resultDocumentSnapshot.exception}")
                    Result.Canceled(resultDocumentSnapshot.exception)
                }
            }
        } catch (exception: Exception) {
            return Result.Error(exception)
        }
    }

    override suspend fun logoutUser() {
        firebaseAuth.signOut()
    }

    override suspend fun checkUserLoggedIn(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override suspend fun getUser(userId: String): Result<User>? {
        return try {
            when (val resultDocumentSnapshot = userCollection.document(userId).get().await()) {
                is Result.Success -> {
                    val user = resultDocumentSnapshot.data.toObject(User::class.java)!!
                    Result.Success(user)
                }
                is Result.Error -> Result.Error(resultDocumentSnapshot.exception)
                is Result.Canceled -> Result.Canceled(resultDocumentSnapshot.exception)
            }
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

    override suspend fun getReportOptions(): FirestoreRecyclerOptions<Report> {
        return FirestoreRecyclerOptions.Builder<Report>()
            .setQuery(reportCollection, Report::class.java)
            .build()
    }

    override suspend fun getReports(): Result<List<Report>?> {
        return try {
            when (val resultDocumentSnapshot = reportCollection.get().await()) {
                is Result.Success -> {
                    val reports = ArrayList<Report>()
                    for (response in resultDocumentSnapshot.data) {
                        with(response) {
                            val report = Report(
                                this.id,
                                this.getString("description"),
                                this.getDouble("latitude"),
                                this.getDouble("longitude"),
                                this.getString("photoUrl"),
                                this.getBoolean("isDone"),
                                this.getTimestamp("createdAt")
                            )
                            reports.add(report)
                        }
                    }
                    Result.Success(reports)
                }
                is Result.Error -> Result.Error(resultDocumentSnapshot.exception)
                is Result.Canceled -> Result.Canceled(resultDocumentSnapshot.exception)
            }
        }catch (exception: Exception) {
            Result.Error(exception)
        }
    }

    override suspend fun getReportById(reportId: String): Result<Report>? {
        return try {
            when (val resultDocumentSnapshot = reportCollection.document(reportId).get().await()) {
                is Result.Success -> {
                    val report = resultDocumentSnapshot.data.toObject(Report::class.java)!!
                    Result.Success(report)
                }
                is Result.Error -> Result.Error(resultDocumentSnapshot.exception)
                is Result.Canceled -> Result.Canceled(resultDocumentSnapshot.exception)
            }
        } catch (exception: Exception) {
            Result.Error(exception)
        }
    }

}