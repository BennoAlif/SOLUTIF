package com.sabeno.solutif.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.mapbox.mapboxsdk.Mapbox
import com.sabeno.solutif.R
import com.sabeno.solutif.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.hide()
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setupBottomNav()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupBottomNav() {
        val bottomNavigationView = binding?.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        if (bottomNavigationView != null) {
            NavigationUI.setupWithNavController(
                bottomNavigationView,
                navHostFragment.navController
            )
        }
    }
}