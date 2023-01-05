package com.zeus.chatme

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.zeus.chatme.chatSection.ChatDetailActivity

class PrePostActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private var GALLERY_REQUEST = 192
    private var IMAGE_REQUEST = 291

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val choice = intent.getStringExtra("__CHOICE_")

        //To avoid Uri Error
        var builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        try {
            if (choice.equals("image")) {
                val intent = Intent()
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                intent.type = "image/*"
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                startActivityForResult(intent, IMAGE_REQUEST)
//                CropImage.activity().setAspectRatio(1, 1).start(this)
            } else {
                galleryVideo()
            }
        } catch (e : Exception) {
            e.printStackTrace()
            Toast.makeText(this, "An Error Occurred.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun galleryVideo() {
        //Intent to pick and video
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).also {
//            it.addCategory(Intent.CATEGORY_OPENABLE)
//            it.type = "video/*"
//            it.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }

        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.type = "video/*"
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            try {
                imageUri = data.data!!

                saveUri(imageUri.toString())
                //got to post activity with image uri as extra
                startActivity(
                    Intent(this, PostActivity::class.java)
                        .putExtra("_CROP_", imageUri.toString())
                        .putExtra("TYPE__", "image")
                )
                finish()
                Animatoo.animateSlideLeft(this)

                this.grantUriPermission(
                    this.packageName,
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.contentResolver.takePersistableUriPermission(imageUri, takeFlags)
            }
            catch(e: Exception) {
                e.printStackTrace()
            }
        } else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            try {
                imageUri = data.data!!

                saveUri(imageUri.toString())

                //got to post activity with image uri as extra
                startActivity(
                    Intent(this, PostActivity::class.java)
                        .putExtra("_CROP_", imageUri.toString())
                        .putExtra("TYPE__", "video")
                )
                finish()
                Animatoo.animateSlideLeft(this)

                this.grantUriPermission(
                    this.packageName,
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.contentResolver.takePersistableUriPermission(imageUri, takeFlags)
            }
            catch(e: Exception) {
                e.printStackTrace()
            }
        } else {
                startActivity(Intent(this, ChatDetailActivity::class.java))
                finish()
                Animatoo.animateSlideRight(this)
        }
    }

    private fun saveUri(uri : String) {
        val sharedPref = getSharedPreferences("CHOSEN_URI", Context.MODE_PRIVATE).edit()
        sharedPref.putString("_URI_", uri)
        sharedPref.apply()
    }

    override fun onBackPressed() {
            startActivity(Intent(this, ChatDetailActivity::class.java))
            finish()
            Animatoo.animateSlideRight(this)
    }
}