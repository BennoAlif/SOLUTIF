package com.sabeno.solutif.di

import com.sabeno.solutif.core.data.ReportRepository
import com.sabeno.solutif.ui.AuthViewModel
import com.sabeno.solutif.ui.create.CreateReportViewModel
import com.sabeno.solutif.ui.detail.DetailViewModel
import com.sabeno.solutif.ui.map.MapViewModel
import com.sabeno.solutif.ui.report.ReportViewModel
import org.koin.dsl.module

val authViewModelModule = module {
    single { AuthViewModel(ReportRepository()) }
}

val reportViewModelModule = module {
    single { ReportViewModel(ReportRepository()) }
}

val detailViewModelModule = module {
    single { DetailViewModel(ReportRepository()) }
}

val mapViewModelModule = module {
    single { MapViewModel(ReportRepository()) }
}

val createReportViewModelModule = module {
    single { CreateReportViewModel(ReportRepository()) }
}