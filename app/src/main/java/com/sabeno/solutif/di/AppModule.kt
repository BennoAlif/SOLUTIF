package com.sabeno.solutif.di

import com.sabeno.solutif.data.ReportRepository
import com.sabeno.solutif.ui.AuthViewModel
import org.koin.dsl.module

val authViewModelModule = module {
    single { AuthViewModel(ReportRepository()) }
}