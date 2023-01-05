package com.zeus.chatme

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo

class DeveloperInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_info)

        val contactBtn: Button = findViewById(R.id.contactMeBtn)
        try {
            //code to direct users to email app to send email to me on contact button click
            contactBtn.setOnClickListener {
                contactBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click))

                val link = "https://divineabiloro.dorik.io/"

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setPackage("com.android.chrome")
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    intent.setPackage(null)
                    startActivity(Intent.createChooser(intent, "Select Browser"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val backBtn: ImageView = findViewById(R.id.backBtnDeveloperInfo)
        backBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
            Animatoo.animateSlideRight(this)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
        Animatoo.animateSlideRight(this)
    }

}