package com.sabeno.solutif.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sabeno.solutif.R
import com.sabeno.solutif.databinding.ActivityRegisterBinding
import com.sabeno.solutif.ui.AuthViewModel
import com.sabeno.solutif.ui.login.LoginActivity
import org.koin.android.ext.android.inject

class RegisterActivity : AppCompatActivity() {

    private var _binding: ActivityRegisterBinding? = null
    private val binding get() = _binding

    private val authViewModel: AuthViewModel by inject()

    private var isPetugas: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnRegister?.setOnClickListener {
            if (validateEmail() && validatePassword() && validateName()) {
                authViewModel.registerUser(
                    binding?.tietRegisterName?.text.toString(),
                    binding?.tietRegisterEmail?.text.toString(),
                    binding?.tietRegisterPassword?.text.toString(),
                    isPetugas,
                    this
                )
            }
        }

        authViewModel.toast.observe(this, { message ->
            message?.let {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                authViewModel.onToastShown()
            }
        })

        authViewModel.spinner.observe(this, { value ->
            value.let { show ->
                binding?.spinnerLogin?.visibility = if (show) View.VISIBLE else View.GONE
            }
        })

        binding?.tvLogin?.setOnClickListener {
            startLoginActivity()
        }

        val roles = resources.getStringArray(R.array.roles_array)

        val vRoles = binding?.role
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        if (vRoles != null) {
            vRoles.adapter = adapter
        }

        if (vRoles != null) {
            vRoles.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    isPetugas = position != 0
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun validateName(): Boolean {
        val password = binding?.tietRegisterName?.text.toString().trim()
        return if (password.length < 4) {
            binding?.tietRegisterName?.error = resources.getString(R.string.name_validate)
            false
        } else {
            true
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding?.tietRegisterEmail?.text.toString().trim()
        return if (!email.contains("@") && !email.contains(".")) {
            binding?.tietRegisterEmail?.error = resources.getString(R.string.enter_valid_email)
            false
        } else {
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = binding?.tietRegisterPassword?.text.toString().trim()
        return if (password.length < 7) {
            binding?.tietRegisterPassword?.error = resources.getString(R.string.password_validate)
            false
        } else {
            true
        }
    }
}