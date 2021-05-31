package com.sabeno.solutif

import android.app.Application
import com.sabeno.solutif.di.*
import com.sabeno.solutif.di.mapViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(
                listOf(
                    authViewModelModule,
                    reportViewModelModule,
                    detailViewModelModule,
                    mapViewModelModule,
                    createReportViewModelModule
                )
            )
        }
    }
}