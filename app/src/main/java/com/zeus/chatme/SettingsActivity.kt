package com.zeus.chatme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.zeus.chatme.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    lateinit var binding : ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtnSettings.setOnClickListener {
            startActivity(Intent(this , ChatHomeActivity::class.java))
            finish()
            Animatoo.animateSlideRight(this)
        }

        binding.developerInfoBtn.setOnClickListener {
            startActivity(Intent(this , DeveloperInfoActivity::class.java))
            finish()
            Animatoo.animateSlideLeft(this)
        }

        binding.feedbackBtn.setOnClickListener {
            startActivity(Intent(this , FeedBackActivity::class.java))
            finish()
            Animatoo.animateSlideLeft(this)
        }

        binding.accountBtn.setOnClickListener {
            startActivity(Intent(this , AccountActivity::class.java))
            finish()
            Animatoo.animateSlideLeft(this)
        }

        binding.shareBtn.setOnClickListener {
            //Change to app download link later
//            val sharebody = "Hey , i earn money for free by watching and clicking on videos on AdMoney , you can download it at -: https://play.google.com/store/apps/details?id=com.zeus.admoney"
//            val intent = Intent(Intent.ACTION_SEND)
//
//            //Setting type of data shared as text
//            intent.type = "text/plain"
//
//            //Adding the text to share using put extra
//            intent.putExtra(Intent.EXTRA_TEXT, sharebody)
//            startActivity(Intent.createChooser(intent, "Share AdMoney Via"))
        }


    }

    override fun onBackPressed() {
        startActivity(Intent(this, ChatHomeActivity::class.java))
        finish()
        Animatoo.animateSlideRight(this)
    }

}