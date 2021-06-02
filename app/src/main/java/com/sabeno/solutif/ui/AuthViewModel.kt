package com.sabeno.solutif.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.sabeno.solutif.R
import com.sabeno.solutif.data.source.User
import com.sabeno.solutif.repository.IReportRepository
import com.sabeno.solutif.ui.login.LoginActivity
import com.sabeno.solutif.ui.register.RegisterActivity
import com.sabeno.solutif.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AuthViewModel(private val IReportRepository: IReportRepository) : ViewModel() {

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?>
        get() = _toast

    private val _spinner = MutableLiveData(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _currentUserMLD = MutableLiveData(User())
    val currentUserLD: LiveData<User>
        get() = _currentUserMLD

    fun loginUser(email: String, password: String, activity: Activity) {
        launchDataLoad {
            viewModelScope.launch {
                when (val result = IReportRepository.loginUser(email, password)) {
                    is Result.Success -> {
                        result.data?.let { firebaseUser ->
                            getUser(firebaseUser.uid, activity)
                        }
                        _spinner.value = false
                    }
                    is Result.Error -> {
                        _toast.value = result.exception.message
                        _spinner.value = false
                    }
                    is Result.Canceled -> {
                        _toast.value = activity.getString(R.string.request_canceled)
                        _spinner.value = false
                    }
                }
            }
        }
    }

    fun registerUser(name: String, email: String, password: String, activity: Activity) {
        launchDataLoad {
            viewModelScope.launch {
                when (val result =
                    IReportRepository.registerUser(email, password, activity.applicationContext)) {
                    is Result.Success -> {
                        result.data?.let { firebaseUser ->
                            createUserFirestore(createUserObject(firebaseUser, name, email), activity)
                        }
                        _spinner.value = false
                    }
                    is Result.Error -> {
                        _toast.value = result.exception.message
                        _spinner.value = false
                    }
                    is Result.Canceled -> {
                        _toast.value = activity.getString(R.string.request_canceled)
                        _spinner.value = false
                    }
                }
            }
        }
    }

    fun logOutUser() {
        viewModelScope.launch {
            IReportRepository.logoutUser()
        }
    }

    private suspend fun createUserFirestore(user: User, activity: Activity) {
        when (val result = IReportRepository.createUserFirestore(user)) {
            is Result.Success -> {
                when (activity) {
                    is RegisterActivity -> {
                        _toast.value = activity.getString(R.string.registration_successful)
                    }
                    is LoginActivity -> {
                        _toast.value = activity.getString(R.string.login_successful)
                    }
                }
                _currentUserMLD.value = user
                startMainActivity(activity)
            }
            is Result.Error -> {
                _toast.value = result.exception.message
            }
            is Result.Canceled -> {
                _toast.value = activity.getString(R.string.request_canceled)
            }
        }
    }

    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                _toast.value = error.message
            }
        }
    }

    fun onToastShown() {
        _toast.value = null
    }

    suspend fun getUser(userId: String, activity: Activity) {
        when (val result = IReportRepository.getUser(userId)) {
            is Result.Success -> {
                val user = result.data
                _currentUserMLD.value = user
                startMainActivity(activity)
            }
            is Result.Error -> {
                _toast.value = result.exception.message
            }
            is Result.Canceled -> {
                _toast.value = activity.getString(R.string.request_canceled)
            }
        }
    }

    private fun startMainActivity(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
    }

    fun checkUserLoggedIn(): FirebaseUser? {
        var firebaseUser: FirebaseUser? = null
        viewModelScope.launch {
            firebaseUser = IReportRepository.checkUserLoggedIn()
        }
        return firebaseUser
    }
    private fun createUserObject(
        firebaseUser: FirebaseUser,
        name: String,
        email: String
    ): User {
        return User(id = firebaseUser.uid, email, name)
    }
}