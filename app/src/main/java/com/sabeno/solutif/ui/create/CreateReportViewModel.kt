package com.sabeno.solutif.ui.create

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sabeno.solutif.R
import com.sabeno.solutif.core.data.source.Report
import com.sabeno.solutif.core.repository.IReportRepository
import com.sabeno.solutif.ui.MainActivity
import com.sabeno.solutif.core.utils.Result
import com.sabeno.solutif.core.utils.await
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class CreateReportViewModel(private var IReportRepository: IReportRepository) : ViewModel() {

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?>
        get() = _toast

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    suspend fun uploadPhoto(
        description: String,
        latitude: Double,
        longitude: Double,
        filePath: Uri, activity: Activity
    ) {
        val photoRef = storageRef.child("reports/" + UUID.randomUUID().toString())
        launchDataLoad {
            when (val resultDocumentSnapshot = photoRef.putFile(filePath).await()) {
                is Result.Success -> {
                    val uploadTask = resultDocumentSnapshot.data.task
                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        photoRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        val downloadUri = task.result.toString()
                        val report = Report(
                            description,
                            latitude,
                            longitude,
                            downloadUri,
                            false,
                            Timestamp.now()
                        )
                        viewModelScope.launch {
                            createReport(report, activity)
                        }
                    }
                    _spinner.value = false
                }
                is Result.Error -> _toast.value = resultDocumentSnapshot.exception.message
                is Result.Canceled -> _toast.value = resultDocumentSnapshot.exception?.message
            }
        }
    }

    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                _toast.value = error.message
            }
        }
    }

    private suspend fun createReport(report: Report, activity: Activity) {
        when (val result = IReportRepository.createReport(report)) {
            is Result.Success -> {
                val intent = Intent(activity, MainActivity::class.java)
                activity.startActivity(intent)
            }
            is Result.Error -> {
                _toast.value = result.exception.message
            }
            is Result.Canceled -> {
                _toast.value = activity.getString(R.string.request_canceled)
            }
        }
    }

    fun onToastShown() {
        _toast.value = null
    }
}
