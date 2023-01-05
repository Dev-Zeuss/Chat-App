package com.zeus.chatme

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rengwuxian.materialedittext.MaterialEditText
import com.squareup.picasso.Picasso
import com.zeus.adbucks.NetworkConnectionLiveData
import com.zeus.chatme.databinding.ActivityAccountBinding
import de.hdodenhof.circleimageview.CircleImageView

class AccountActivity : AppCompatActivity() {

    lateinit var binding: ActivityAccountBinding

    lateinit var profileImage: CircleImageView
    lateinit var changePicBtn: CircleImageView
    lateinit var editSaveBtn: CardView
    lateinit var usernameEt: MaterialEditText
    lateinit var editSaveText: TextView
    private var check = false

    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference
    lateinit var progress: ProgressDialog
    private lateinit var imageUri2: Uri
    lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var mCurrentPhotoPath: String
    lateinit var networkConnection: NetworkConnectionLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileImage = binding.profileImage
        changePicBtn = binding.changePicBtn
        editSaveBtn = binding.editSaveBtn
        usernameEt = binding.usernameET
        editSaveText = binding.editSaveText
        networkConnection = NetworkConnectionLiveData(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid)
        storageReference = FirebaseStorage.getInstance().getReference("Uploads").child(auth.currentUser!!.uid)

        progress = ProgressDialog(this, R.style.MyDialogStyle)
        progress.setCancelable(false)
        progress.setMessage("Loading")
        progress.show()

        networkConnection.observe(this, Observer { isConnected ->
            if (isConnected) {
                //Fetch details from database
                Thread {
                    runOnUiThread {
                        database.get().addOnSuccessListener {
                            if (it.exists()) {
                                val result = it.child("imageURL").value
                                val result2 = it.child("username").value

                                usernameEt.setText(result2.toString())

                                if (result != null) {
                                    val url = result.toString()

                                    //Profile pic not set
                                    if (url.equals("default")) {
                                        profileImage.setImageResource(R.drawable.default_dp_2)
                                    } else { // Profile pic set so load it
                                        Picasso.get().load(url).into(profileImage)
                                    }
                                }
                                progress.dismiss()

                            }
                        }.addOnFailureListener {
                            progress.dismiss()
                            Toast.makeText(this, "An error Occurred", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, SettingsActivity::class.java))
                            finish()
                            Animatoo.animateSlideRight(this)
                        }
                    }
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }.start()

                editSaveBtn.setOnClickListener {
                    editSaveBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click))

                    //save clicked
                    val usernameDetails = usernameEt.text.toString()

                    if (TextUtils.isEmpty(usernameDetails)) {
                        usernameEt.error = "Please enter Username"
                        return@setOnClickListener
                    }

                    if (usernameDetails.isNotEmpty()) {
                        progress.setMessage("Saving Details")
                        progress.show()

                        Thread {
                            runOnUiThread {
                                //upload username to firebase under username and search node to enable user name to be search
                                val hashMap: HashMap<String, Any> = HashMap()
                                hashMap["username"] = usernameEt.text.toString().trim()
                                hashMap["search"] = usernameEt.text.toString().toLowerCase()
                                database.updateChildren(hashMap).addOnSuccessListener {
                                    progress.dismiss()
                                    Toast.makeText(
                                        this,
                                        "Username Set Successfully.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }.addOnFailureListener {
                                    progress.dismiss()
                                    Toast.makeText(this, "An error occurred,", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            Thread.sleep(500)
                        }.start()

                    } else {
                        Toast.makeText(this, "Please input username.", Toast.LENGTH_LONG).show()
                    }
                }

                changePicBtn.setOnClickListener {
                    changePicBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            check = true
                            chooseImage2()

                        } else {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                PERMISSION_REQUEST
                            )
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                PERMISSION_REQUEST
                            )
                        }
                    } else {
                        check = true
                        chooseImage2()
                    }
                }

            } else {
                try {
                    if (progress != null)
                        progress.dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
                Animatoo.animateSlideRight(this)

                editSaveBtn.setOnClickListener {
                    editSaveBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click))

                    Toast.makeText(
                        this,
                        "Please connect to the internet to change username.",
                        Toast.LENGTH_LONG
                    ).show()

                }

                changePicBtn.setOnClickListener {
                    changePicBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.click))

                    Toast.makeText(
                        this,
                        "Please connect to the internet to change profile picture.",
                        Toast.LENGTH_LONG
                    ).show()

                }

            }
        })

        binding.backBtnAccount.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
            Animatoo.animateSlideRight(this)
        }

    }
    private fun chooseImage2() {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.type = "image/*"
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver: ContentResolver = getContentResolver()
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            try {
                imageUri = data.data!!

                uploadImage()

                this.grantUriPermission(
                    this.packageName,
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.contentResolver.takePersistableUriPermission(imageUri, takeFlags)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
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

    private fun uploadImage() {
        val progress2 = ProgressDialog(this, R.style.MyDialogStyle)
        progress2.setMessage("Setting Profile Picture")
        progress2.setCancelable(false)
        progress2.setCanceledOnTouchOutside(false)
        progress2.show()

        if(checkForInternet(this)) {
            if (imageUri != null) {
                val fileReference = storageReference.child(
                    System.currentTimeMillis().toString() + "." + getFileExtension(imageUri)
                )
                fileReference.putFile(imageUri).addOnCompleteListener {
                    if (it.isSuccessful) {
                        fileReference.downloadUrl.addOnSuccessListener { task ->
                            var imageUrl = task.toString()

                            val map: HashMap<String, Any> = HashMap()
                            map["imageURL"] = imageUrl
                            database.updateChildren(map).addOnSuccessListener {
                                //Set The Image on ImageView
                                Picasso.get().load(imageUrl).into(profileImage)
                                progress2.dismiss()
                                Toast.makeText(this, "Profile Photo Changed.", Toast.LENGTH_SHORT).show()

                            }.addOnFailureListener {
                                progress2.dismiss()
                                Toast.makeText(
                                    this,
                                    "Failed to upload image due to ${it.message}.",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }
                    }
                }
            }
        }else {
            progress2.dismiss()
            Toast.makeText(
                this,
                "Error uploading image. No internet connection",
                Toast.LENGTH_SHORT
            ).show()
        }

//        networkConnection.observe(this, Observer { isConnected ->
//            if (!isConnected) {
//                progress2.dismiss()
//                Toast.makeText(
//                    this,
//                    "Error uploading image. No internet connection",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
    }

    companion object {
        const val PERMISSION_REQUEST = 210
        const val CAMERA_REQUEST = 320
        const val GALLERY_REQUEST = 450
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    check = true
                    chooseImage2()

                } else {
                    check = false
                    Toast.makeText(
                        this,
                        "Permission Denied. Grant Permission to be able to access App's full features.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
        Animatoo.animateSlideRight(this)
    }

}