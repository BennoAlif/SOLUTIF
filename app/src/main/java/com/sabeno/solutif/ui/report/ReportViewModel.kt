package com.sabeno.solutif.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sabeno.solutif.core.data.source.Report
import com.sabeno.solutif.core.repository.IReportRepository
import kotlinx.coroutines.launch

class ReportViewModel(private var IReportRepository: IReportRepository) : ViewModel() {

    fun getReports(): FirestoreRecyclerOptions<Report>? {
        var firestoreOptions: FirestoreRecyclerOptions<Report>? = null
        viewModelScope.launch {
            firestoreOptions = IReportRepository.getReportOptions()
        }
        return firestoreOptions
    }
}