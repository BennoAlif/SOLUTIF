package com.sabeno.solutif.ui.detail

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabeno.solutif.R
import com.sabeno.solutif.core.repository.IReportRepository
import com.sabeno.solutif.ui.MainActivity
import com.sabeno.solutif.core.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DetailViewModel(private val IReportRepository: IReportRepository) : ViewModel() {
    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?>
        get() = _toast

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

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

    suspend fun updateReportStatus(reportId: String, isDone: Boolean, activity: Activity) {
        launchDataLoad {
            when (val result = IReportRepository.updateReportStatus(reportId, isDone)) {
                is Result.Success -> {
                    _spinner.value = false
                }
                is Result.Error -> {
                    _toast.value = result.exception.message
                    _spinner.value = false
                }
                is Result.Canceled -> {
                    _toast.value = activity.getString(R.string.request_canceled)
                    _spinner.value = false
                }
            }
        }
    }

    suspend fun deleteReport(reportId: String, activity: Activity) {
        launchDataLoad {
            when (val result = IReportRepository.deleteReport(reportId)) {
                is Result.Success -> {
                    val intent = Intent(activity, MainActivity::class.java)
                    activity.startActivity(intent)
                    _spinner.value = false
                }
                is Result.Error -> {
                    _toast.value = result.exception.message
                    _spinner.value = false
                }
                is Result.Canceled -> {
                    _toast.value = activity.getString(R.string.request_canceled)
                    _spinner.value = false
                }
            }
        }
    }

    fun onToastShown() {
        _toast.value = null
    }

}