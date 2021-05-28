package com.sabeno.solutif

import android.app.Application
import com.sabeno.solutif.di.authViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(listOf(authViewModelModule))
        }
    }
}