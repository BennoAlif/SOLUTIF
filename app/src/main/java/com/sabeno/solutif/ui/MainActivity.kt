package com.sabeno.solutif.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.mapbox.mapboxsdk.Mapbox
import com.sabeno.solutif.R
import com.sabeno.solutif.databinding.ActivityMainBinding
import com.sabeno.solutif.ui.create.CreateReportActivity
import com.sabeno.solutif.ui.profile.ProfileActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setupBottomNav()

        binding?.fabCamera?.setOnClickListener(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.top_nav_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
    }

    private fun setupBottomNav() {
        val bottomNavigationView = binding?.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        if (bottomNavigationView != null) {
            NavigationUI.setupWithNavController(
                bottomNavigationView,
                navHostFragment.navController
            )
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            val intent = Intent(this@MainActivity, CreateReportActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Camera Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_camera -> checkPermission()
        }
    }
}