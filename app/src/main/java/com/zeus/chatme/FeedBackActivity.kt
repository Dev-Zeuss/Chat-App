package com.zeus.chatme

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.android.material.snackbar.Snackbar
import com.rengwuxian.materialedittext.MaterialEditText

class FeedBackActivity : AppCompatActivity() {

    lateinit var emailSubjectET: MaterialEditText
    lateinit var emailMessageET: MaterialEditText
    lateinit var sendFeedbackBtn: Button
    lateinit var backBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_back)

        emailSubjectET = findViewById(R.id.emailSubject)
        emailMessageET = findViewById(R.id.emailMessage)
        sendFeedbackBtn = findViewById(R.id.sendFeedbackBtn)
        backBtn = findViewById(R.id.backBtnFeedBack)

        backBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
            Animatoo.animateSlideRight(this)
        }

        //code to direct users to email app to send email to me on contact button click
        if (checkForInternet(this)) {
            try {
                sendFeedbackBtn.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(this, R.anim.click)
                    sendFeedbackBtn.startAnimation(animation)

                    if (TextUtils.isEmpty(emailSubjectET.text.toString())) {
                        emailSubjectET.error = "Please enter Subject"
                        return@setOnClickListener

                    } else if (TextUtils.isEmpty(emailMessageET.text.toString())) {
                        emailMessageET.error = "Please enter Message"
                        return@setOnClickListener
                    }

                    val intent = Intent(Intent.ACTION_SEND)
                    intent.data = Uri.parse("Email")
                    val myEmail = "ztechservice397@gmail.com"  // Add to build.gradle later
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(myEmail))
                    intent.putExtra(Intent.EXTRA_SUBJECT, emailSubjectET.text.toString())
                    intent.putExtra(Intent.EXTRA_TEXT, emailMessageET.text.toString())

                    intent.type = "message/rfc822"
                    try {
                        emailMessageET.setText("")
                        emailSubjectET.setText("")
                        val chooser = Intent.createChooser(intent, "Send Email")
                        startActivity(chooser)
                    } catch (e: Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            sendFeedbackBtn.setOnClickListener {
                val animation = AnimationUtils.loadAnimation(this, R.anim.click)
                sendFeedbackBtn.startAnimation(animation)

                //Create an instance of the snackbar
                val snackBar = Snackbar.make(it, "No Internet Connection", Snackbar.LENGTH_LONG)
                snackBar.setAction("DISMISS", View.OnClickListener {
                    snackBar.dismiss()
                })
                snackBar.show()

            }
        }

    }

    //Check for internet connection in App
    private fun checkForInternet(context: Context): Boolean {
        //Register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //If the android version is equa to 'M' or higher, we need to use the network capabilities to check what type of network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Returns a network object corresponding to the currently active default data network
            val network = connectivityManager.activeNetwork ?: return false

            //Representation of the capabilities of an active network
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                //Indicates this network uses a wi-fi transport or wi-fi has network capabilities
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                //Indicates this network has a cellular network or cellular has network capabilities
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            //If android version is below 'M'

            @Suppress("DEPRECIATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false

            @Suppress("DEPRECIATION")
            return networkInfo.isConnected
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
        Animatoo.animateSlideRight(this)
    }

}