package com.zeus.chatme

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.rengwuxian.materialedittext.MaterialEditText
import com.zeus.adbucks.NetworkConnectionLiveData

class LoginActivity : AppCompatActivity() {
    lateinit var signUpText: TextView

    lateinit var auth: FirebaseAuth

    lateinit var progress: ProgressDialog
    lateinit var loginButton: CardView
    lateinit var emailInput: MaterialEditText
    lateinit var passwordInput: MaterialEditText
    lateinit var passwordToggle : ImageView
    lateinit var networkConnection: NetworkConnectionLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.loginButton1)
        emailInput = findViewById(R.id.emailInput1)
        passwordInput = findViewById(R.id.passwordInput1)
        passwordToggle = findViewById(R.id.passwordToggleIV)
        signUpText = findViewById(R.id.signUpText1)
        networkConnection = NetworkConnectionLiveData(this)

        signUpText.setOnClickListener {
            signUpText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click))
            startActivity(Intent(this, SignUpActivity::class.java))
            Animatoo.animateSlideLeft(this)
            finish()
        }

        //Initialize firebase and progress dialog
        auth = FirebaseAuth.getInstance()
        progress = ProgressDialog(this, R.style.MyDialogStyle)

        //Check Internet Connection when Login button is clicked
        networkConnection.observe(this, Observer { isConnected ->
            if (isConnected) {
                loginButton.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(this, R.anim.click)
                    loginButton.startAnimation(animation)
                    login()
                }
            } else {
                loginButton.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(this, R.anim.click)
                    loginButton.startAnimation(animation)

                    //Create an instance of the snackbar
                    val snackBar = Snackbar.make(findViewById(R.id.rootView2), "", Snackbar.LENGTH_SHORT)
                    //Inflate the custom snackbar layout created
                    var customSnackView = layoutInflater.inflate(R.layout.custom_snackbar, null)

                    val noInternetText = customSnackView.findViewById<TextView>(R.id.text3)
                    noInternetText.text = ("No Internet Connection")

                    //Set the background of the default snackbar to transparent
                    snackBar.view.setBackgroundColor(Color.TRANSPARENT)
                    //Change the layout o the snackbar
                    var snackBarLayout = snackBar.view as Snackbar.SnackbarLayout
                    //Set padding of all the corners as 0
                    snackBarLayout.setPadding(0, 0, 0, 0)

                    //Add the custom snackbar layout to snackbar layout
                    snackBarLayout.addView(customSnackView, 0)

                    snackBar.show()
                }
            }
        })

        var click = 1
        passwordToggle.setOnClickListener {
            passwordToggle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click))
            click += 1
            if (click % 2 == 0) {
                // Set password Hide Icon
                passwordToggle.setImageResource(R.drawable.ic_invisible)
                // Show Password
                passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                passwordInput.setSelection(passwordInput.text!!.length) // Set cursor at the end of text

            } else {
                // Set password_show Icon
                passwordToggle.setImageResource(R.drawable.ic_visible)
                //Hide Password
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordInput.setSelection(passwordInput.text!!.length) // Set cursor at the end of text

            }

        }

    }
    private fun login() {
        val emailDetails = emailInput.text.toString().trim()
        val passwordDetails = passwordInput.text.toString().trim()

        if (TextUtils.isEmpty(emailDetails)) {
            YoYo.with(Techniques.Shake) //shake edittext view if empty
                .duration(500)
                .playOn(emailInput)

            emailInput.error = "Please enter Email"
            return
        } else if (TextUtils.isEmpty(passwordDetails)) {
            YoYo.with(Techniques.Shake)
                .duration(500)
                .playOn(passwordInput)

            passwordInput.error = "Please enter Password"
            return
        } else if (passwordDetails.length < 6) {
            YoYo.with(Techniques.Shake)
                .duration(500)
                .playOn(passwordInput)

            passwordInput.error = "Password should be more than 6 digits"
            return
        }

        if (emailDetails.isNotEmpty() && passwordDetails.isNotEmpty()) {
            //Set up progress Dialog details
            progress.setMessage("Loading")
            progress.setCanceledOnTouchOutside(false)
            progress.setCancelable(false)
            progress.show()

            //Create another thread
            Thread {
                runOnUiThread {
                    //Sign in user with provided email address and password
                    auth.signInWithEmailAndPassword(emailDetails, passwordDetails)
                        .addOnCompleteListener(LoginActivity()){ task ->
                            if (task.isSuccessful) {

                                //If user has verified email address, allow user to log into account
                                if (auth.currentUser!!.isEmailVerified) {
                                    emailInput.setText("")
                                    passwordInput.setText("")
                                    progress.dismiss()//Stop progress dialog

                                    startActivity(Intent(this, ChatHomeActivity::class.java)
                                        .putExtra("LOGIN", true))
                                    Animatoo.animateSlideLeft(this)
                                    finish()
                                } else {
                                    progress.dismiss()//Stop progress dialog

                                    //Show message to user to go and verify his/her email address
                                    Toast.makeText(this, "Please verify Email", Toast.LENGTH_LONG).show()
                                    emailInput.setText("")
                                    passwordInput.setText("")
                                }

                            } else {
                                //Stop progress dialog and show error message
                                progress.dismiss()
                                YoYo.with(Techniques.Shake) //shake edittext view if there is error
                                    .duration(500)
                                    .playOn(emailInput)

                                YoYo.with(Techniques.Shake)
                                    .duration(500)
                                    .playOn(passwordInput)

                                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                            }
                        }
                }
                Thread.sleep(500)
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkConnection.removeObservers(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        finishAffinity()
        Animatoo.animateSlideRight(this)
    }


}