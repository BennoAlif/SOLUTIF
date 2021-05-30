package com.sabeno.solutif.ui.detail

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabeno.solutif.R
import com.sabeno.solutif.data.source.Report
import com.sabeno.solutif.data.source.User
import com.sabeno.solutif.repository.IReportRepository
import com.sabeno.solutif.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DetailViewModel(private val IReportRepository: IReportRepository) : ViewModel() {
    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?>
        get() = _toast

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _currentReport = MutableLiveData(Report())
    val currentReport: LiveData<Report>
        get() = _currentReport


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

    fun onToastShown() {
        _toast.value = null
    }

    suspend fun getReportById(reportId: String, activity: Activity) {
        launchDataLoad {
            when (val result = IReportRepository.getReportById(reportId)) {
                is Result.Success -> {
                    val report = result.data
                    _currentReport.value = report
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
}