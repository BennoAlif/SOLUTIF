package com.sabeno.solutif.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sabeno.solutif.R
import com.sabeno.solutif.databinding.ActivityProfileBinding
import com.sabeno.solutif.ui.AuthViewModel
import com.sabeno.solutif.ui.login.LoginActivity
import org.koin.android.ext.android.inject

class ProfileActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by inject()

    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.title = resources.getString(R.string.title_profile)

        authViewModel.currentUserLD.observe(this, { currentUser ->
            binding?.tvEmail?.text = currentUser.email
            binding?.tvUsername?.text = currentUser.name
            var roles = getString(R.string.petugas)
            when (currentUser.isPetugas) {
                true -> roles = getString(R.string.petugas)
                false -> roles = getString(R.string.pelapor)
            }
            binding?.tvRole?.text = roles
        })

        binding?.btnLogout?.setOnClickListener {
            logout()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun logout() {
        authViewModel.logOutUser()
        startLoginActivity()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}