package com.cse4100g10.taskmanager

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class SplashActivity: AppCompatActivity() {
    private val splashTime = 150L
    private lateinit var myHandler : Handler
    private lateinit var auth: FirebaseAuth


    override  fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        myHandler = Handler()
        auth = FirebaseAuth.getInstance()

        Log.e("PROVA", "HI")

        myHandler.postDelayed({
            goToMainActivity()
        }, splashTime)
    }

    private fun goToMainActivity(){
        val mainActivityIntent = Intent(applicationContext, ProjectListActivity::class.java)
        val logInActivity = Intent(applicationContext, LogInActivity::class.java)

        if (auth.uid != null) {
            startActivity(mainActivityIntent)
            finish()
        } else {
            startActivity(logInActivity)
            finish()
        }
    }
}