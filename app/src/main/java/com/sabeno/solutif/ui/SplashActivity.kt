package com.sabeno.solutif.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sabeno.solutif.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
    }
}