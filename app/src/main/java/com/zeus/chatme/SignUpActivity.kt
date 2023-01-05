package com.zeus.chatme

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rengwuxian.materialedittext.MaterialEditText
import com.zeus.adbucks.NetworkConnectionLiveData
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class SignUpActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var reference: DatabaseReference
    lateinit var progress: ProgressDialog

    lateinit var passwordInput: MaterialEditText
    lateinit var userNameInput: MaterialEditText
    lateinit var emailInput: MaterialEditText
    lateinit var signUpBtn: CardView
    lateinit var loginText: TextView
    lateinit var passwordToggle : ImageView
    lateinit var networkConnection : NetworkConnectionLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userNameInput = findViewById(R.id.userNameInput1)
        emailInput = findViewById(R.id.emailInput2)
        signUpBtn = findViewById(R.id.signUpButton1)
        passwordInput = findViewById(R.id.passwordInput2)
        passwordToggle = findViewById(R.id.passwordToggleIV2)
        loginText = findViewById(R.id.loginText1)

        // Initialize firebase and firebase database
//        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        reference = FirebaseDatabase.getInstance().reference.child("Videos")
        networkConnection = NetworkConnectionLiveData(this)

        //initialize progress dialog
        progress = ProgressDialog(this, R.style.MyDialogStyle)

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

        loginText.setOnClickListener {
            loginText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click))
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            Animatoo.animateSlideRight(this)
        }

        //Check internet connection first
        networkConnection.observe(this, Observer { isConnected ->
            if (isConnected) {
                signUpBtn.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(this, R.anim.click)
                    signUpBtn.startAnimation(animation)
                    register()
                }
            } else {
                signUpBtn.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(this, R.anim.click)
                    signUpBtn.startAnimation(animation)

                    //Create an instance of the snackbar
                    val snackBar = Snackbar.make(findViewById(R.id.rootView3), "", Snackbar.LENGTH_SHORT)
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
    }

    private fun register() {
        //Get username and password details entered by the user
        val userNameDetails = userNameInput.text.toString().trim()
        val passwordDetails = passwordInput.text.toString().trim()
        val emailDetails = emailInput.text.toString().trim()
        val passwordResetCode = ""
        val status = "online"
        val lastMessage = ""
        val location = "default"
        val salt = createSalt()
        val hashType = "SHA-512"
        val hashedPassword = generateHashWithSalt(passwordDetails, hashType, salt)

        val reversedEmail = StringBuffer(emailInput.text.toString().trim()).reverse()
        //Create user with Entered Email address and password
        if (TextUtils.isEmpty(userNameDetails)) {
            YoYo.with(Techniques.Shake) // Shake edittext view if it is empty
                .duration(500)
                .playOn(userNameInput)
            userNameInput.error = "Please enter Username"
            return

        }else if(passwordDetails.length < 6){
            YoYo.with(Techniques.Shake)
                .duration(500)
                .playOn(passwordInput)

            passwordInput.error = "Password should be more than 6 digits"
            return

        } else if (TextUtils.isEmpty(emailDetails)) {
            YoYo.with(Techniques.Shake)
                .duration(500)
                .playOn(emailInput)

            emailInput.error = "Please enter Email"
            return
        }
        else if (TextUtils.isEmpty(passwordDetails)) {
            YoYo.with(Techniques.Shake)
                .duration(500)
                .playOn(passwordInput)

            passwordInput.error = "Please enter Password"
            return
        }

        if (userNameDetails.isNotEmpty() && emailDetails.isNotEmpty() && passwordDetails.isNotEmpty() && reversedEmail.substring(0, 10) == "moc.liamg@") {
            //Progress dialog setup
            progress.setMessage("Creating Account")
            progress.setCanceledOnTouchOutside(false)
            progress.setCancelable(false)
            progress.show()

            Thread{
                runOnUiThread {
                    //Create user with Entered Email address and password
                    auth.createUserWithEmailAndPassword(emailDetails, passwordDetails)
                        .addOnCompleteListener(SignUpActivity()) { task ->
                            if (task.isSuccessful) {

                                //If signup was successful, send verification email to user's email address
                                auth.currentUser!!.sendEmailVerification()
                                    .addOnCompleteListener { task1 ->
                                        if (task1.isSuccessful) {
                                            Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                val currentUser = auth.currentUser!!.uid

                                //Store username and password details in hashmap
                                val userMap: HashMap<String, Any> = HashMap()
                                userMap["id"] = currentUser.toString()
                                userMap["username"] = userNameDetails
                                userMap["search"] = userNameDetails.toLowerCase()
                                userMap["about"] = "Hi, I am using ChatMe."
                                userMap["imageURL"] = "default"
                                userMap["imageURL2"] = "default"
                                userMap["email"] = emailDetails.toString()
                                userMap["password"] = hashedPassword.toString()
                                userMap["oporokpo"] = salt.toString()
                                userMap["dateJoined"] = System.currentTimeMillis().toString()
                                userMap["lastmessage"] = lastMessage.toString()
                                userMap["location"] = location.toString()
                                userMap["status"] = System.currentTimeMillis().toString()

                                //Store user's details in firebase realtime database
                                databaseReference.child(currentUser).setValue(userMap).addOnCompleteListener(SignUpActivity()) { task2 ->
                                    if (task2.isSuccessful) {

                                            userNameInput.setText("")
                                            emailInput.setText("")
                                            passwordInput.setText("")

                                            progress.dismiss()//Stop Progress dialog
                                            Toast.makeText(this, "Registration successful, Please verify Email", Toast.LENGTH_LONG).show()
                                            Handler().postDelayed({
                                                startActivity(Intent(this, LoginActivity::class.java))
                                                finish()
                                            }, 500)

                                    }
                                }

                            } else {
                                //Stop progress dialog and show error details
                                progress.dismiss()
                                YoYo.with(Techniques.Shake)
                                    .duration(500)
                                    .playOn(userNameInput)

                                YoYo.with(Techniques.Shake)
                                    .duration(500)
                                    .playOn(passwordInput)

                                YoYo.with(Techniques.Shake)
                                    .duration(500)
                                    .playOn(emailInput)

                                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                            }
                        }
                }
                Thread.sleep(500)
            }.start()
        } else {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
        }

    }

    //Create Password Encryptor
    @Throws(NoSuchAlgorithmException::class)
    private fun generateHashWithSalt(data: String, algorithm: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance(algorithm)
        digest.reset()
        digest.update(salt)
        val hash = digest.digest(data.toByteArray())
        return bytesToStringHex(hash)
    }

    private val hexArray = "0123456789ABCDEF".toCharArray()
    private fun bytesToStringHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v: Int = 0xFF and bytes[j].toInt()
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    private fun createSalt(): ByteArray {
        val bytes: ByteArray = ByteArray(20)
        val random = SecureRandom()
        random.nextBytes(bytes)
        return bytes
    }

    override fun onDestroy() {
        super.onDestroy()
        networkConnection.removeObservers(this)
    }

    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Animatoo.animateSlideRight(this)
    }


}