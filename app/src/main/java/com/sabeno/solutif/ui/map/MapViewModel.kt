package com.sabeno.solutif.ui.map

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sabeno.solutif.R
import com.sabeno.solutif.core.data.source.Report
import com.sabeno.solutif.core.repository.IReportRepository
import com.sabeno.solutif.core.utils.Result

class MapViewModel(private var IReportRepository: IReportRepository) : ViewModel() {

    private var listReport = MutableLiveData<List<Report>?>()

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?>
        get() = _toast

    suspend fun setReports(activity: Activity) {
        when (val result = IReportRepository.getReports()) {
            is Result.Success -> {
                listReport.postValue(result.data)
            }
            is Result.Error -> {
                _toast.value = result.exception.message
            }
            is Result.Canceled -> {
                _toast.value = activity.getString(R.string.request_canceled)
            }
        }
    }

    fun getReports() : LiveData<List<Report>?> {
        return listReport
    }

    fun onToastShown() {
        _toast.value = null
    }
}