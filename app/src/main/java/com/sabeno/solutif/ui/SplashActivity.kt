package com.sabeno.solutif.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.sabeno.solutif.R
import com.sabeno.solutif.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by inject()
    private var currentFirebaseUser: FirebaseUser? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        coroutineScope.launch {
            delay(3_000)
            currentFirebaseUser = authViewModel.checkUserLoggedIn()
            if (currentFirebaseUser == null) {
                switchActivity(LoginActivity())
            } else {
                currentFirebaseUser?.let { firebaseUser ->
                    authViewModel.getUser(firebaseUser.uid, this@SplashActivity)
                }
                switchActivity(MainActivity())
            }
        }
    }

    private fun switchActivity(activity: Activity) {
        val intent = Intent(this@SplashActivity, activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}