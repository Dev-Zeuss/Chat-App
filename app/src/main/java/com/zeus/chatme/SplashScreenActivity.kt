package com.zeus.chatme

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        auth = FirebaseAuth.getInstance()

        //check if user is logged in and if email address is verified
        val currentUser = auth.currentUser

        Handler().postDelayed({
            if (currentUser != null && currentUser.isEmailVerified) {
                startActivity(Intent(this, ChatHomeActivity::class.java))
                Animatoo.animateSwipeLeft(this)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                Animatoo.animateSlideLeft(this)
            }
        },3000)
    }

    override fun onBackPressed() {

    }

}