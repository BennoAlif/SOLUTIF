package com.sabeno.solutif.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sabeno.solutif.R
import com.sabeno.solutif.databinding.ActivityLoginBinding
import com.sabeno.solutif.ui.AuthViewModel
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by inject()

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnLogin?.setOnClickListener {
            if (validateEmail() && validatePassword()) {
                authViewModel.loginUser(
                    binding?.tietLoginEmail?.text.toString(),
                    binding?.tietLoginPassword?.text.toString(),
                    this
                )
            }
        }

        authViewModel.spinner.observe(this, { value ->
            value.let { show ->
                binding?.spinnerLogin?.visibility = if (show) View.VISIBLE else View.GONE
            }
        })

        authViewModel.toast.observe(this, { message ->
            message?.let {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                authViewModel.onToastShown()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun validateEmail(): Boolean {
        val email = binding?.tietLoginEmail?.text.toString().trim()

        return if (!email.contains("@") && !email.contains(".")) {
            binding?.tietLoginEmail?.error = resources.getString(R.string.enter_valid_email)
            false
        } else {
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = binding?.tietLoginPassword?.text.toString().trim()

        return if (password.length <= 6) {
            binding?.tietLoginPassword?.error = resources.getString(R.string.enter_valid_password)
            false
        } else {
            true
        }
    }
}