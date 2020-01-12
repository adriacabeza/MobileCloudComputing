package com.cse4100g10.taskmanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.utils.LoadPictureDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random


class SignUpActivity : AppCompatActivity() {
    lateinit var civProfilePic: CircleImageView
    lateinit var storage: FirebaseStorage
    lateinit var btCreateAccount: Button
    var photoURL: Uri? = null
    private lateinit var auth: FirebaseAuth
    private val API:String = "https://mcc-fall-2019-g10.appspot.com/api/user"
    private var loadPictureDialog = LoadPictureDialog(this@SignUpActivity, LoadPictureDialog.CROP_ROUND, "Select Profile Picture")

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        storage = FirebaseStorage.getInstance()
        civProfilePic = findViewById(R.id.civ_profilepic)

        //val color = ContextCompat.getColor(this, R.color.colorAccentLight)
        //civProfilePic.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)

        civProfilePic.setOnClickListener{
            loadPictureDialog.showDialog()
        }

        auth = FirebaseAuth.getInstance()



        btCreateAccount = findViewById(R.id.bt_create_account)
        btCreateAccount.setOnClickListener {
            createUser()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            when (requestCode){
                LoadPictureDialog.ACTION_GALLERY -> loadPictureDialog.handleGalleryResult(data)
                LoadPictureDialog.ACTION_CAMERA -> loadPictureDialog.handleCameraResult(data)
                UCrop.REQUEST_CROP -> {
                    var uri = loadPictureDialog.handleCropResult(data)
                    civProfilePic.setImageURI(uri)
                    civProfilePic.alpha = 1f
                    if (uri != null) {
                        photoURL = uri
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (loadPictureDialog.hasRequiredPermissions())
            loadPictureDialog.commitPendingAction()
        else
            Snackbar.make(findViewById(R.id.cc_root), R.string.permissions_required_error, Snackbar.LENGTH_SHORT).show()
    }



    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun uploadPicture(uri: Uri?, email:String) {
        var progressDialog: AlertDialog =
            MaterialAlertDialogBuilder(this).setView(R.layout.dialog_indeterminate_progress)
                .create()
        progressDialog.show()
        var storageRef = storage.reference
        //Log.e("Image reference","users/${email}/profile_picture.jpg" )
        var url = storageRef.child("users/${email}/profile_picture.jpg")

        var uploadTask = uri?.let { url.putFile(it) }

        uploadTask!!.addOnFailureListener {
            progressDialog.dismiss()
        }.addOnSuccessListener {
            //Log.d("Image uploaded", it.metadata.toString())
        }
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun createUser(){
        val password = findViewById<EditText>(R.id.et_password).text.toString()
        val email = findViewById<EditText>(R.id.et_email).text.toString()
        val name = findViewById<EditText>(R.id.et_username).text.toString()

        val bt_create_account = findViewById<MaterialButton>(R.id.bt_create_account)


        //Log.d("email",email)
        //Log.d("password", password)

        var requestedFilled = false

        if (email == "") {
            val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
            emailInputLayout.error = "Email is empty."
            requestedFilled = false
        } else {
            val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
            emailInputLayout.isErrorEnabled = false
        }

        if (password == "") {
            val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
            passwordInputLayout.error = "Password is empty."
            requestedFilled = false
        } else {
            val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
            passwordInputLayout.isErrorEnabled = false
        }

        if (name == "") {
            val nameInputLayout: TextInputLayout = findViewById(R.id.nameInputLayout)
            nameInputLayout.error = "Username is empty."
            requestedFilled = false
        } else {
            val nameInputLayout: TextInputLayout = findViewById(R.id.nameInputLayout)
            nameInputLayout.isErrorEnabled = false
        }

        if (password != "" && email != "" && name != "") requestedFilled = true

        if (requestedFilled) {
            if(photoURL != null){
                uploadPicture(photoURL, email)
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        print("createUserWithEmail:success")
                        val user = auth.currentUser
                        if(user != null){
                            val jsonobj = JSONObject()
                            if(photoURL == null){
                                jsonobj.put("name", name)
                                jsonobj.put("email", email)
                                jsonobj.put("id", user.uid)
                            }
                            else{
                                jsonobj.put("name", name)
                                jsonobj.put("email", email)
                                jsonobj.put("id", user.uid)
                                jsonobj.put("photoUrl", "true")
                            }

                            val que = Volley.newRequestQueue(this)
                            val req = JsonObjectRequest(
                                Request.Method.POST,API,jsonobj,
                                Response.Listener {
                                        response ->
                                    //println(response["message"].toString())
                                    println(response.toString())
                                    Snackbar.make(findViewById(R.id.cl_root), R.string.user_created_success, Snackbar.LENGTH_SHORT).show()

                                    val i = Intent(this, ProjectListActivity::class.java)
                                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(i)
                                }, Response.ErrorListener {
                                    val nameInputLayout: TextInputLayout = findViewById(R.id.nameInputLayout)
                                    val list: MutableList<String> = generateUsernames(name)
                                    nameInputLayout.error = "The username is already in use by another account. Try with similar names such as \"${list[0]}\", \"${list[1]}\" or \"${list[2]}\"."
                                    requestedFilled = false
                                }
                            )
                            que.add(req)
                        }
                    } else {
                        requestedFilled = false
                        // println(task.exception.toString())
                        when (task.exception.toString()) {
                            "com.google.firebase.auth.FirebaseAuthWeakPasswordException: The given password is invalid. [ Password should be at least 6 characters ]" -> {
                                val passwordInputLayout: TextInputLayout = findViewById(R.id.passwordInputLayout)
                                passwordInputLayout.error = "Password should be at least 6 characters long."
                            }

                            "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException: The email address is badly formatted." -> {
                                val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
                                emailInputLayout.error = "The email address is badly formatted."
                            }

                            "com.google.firebase.auth.FirebaseAuthUserCollisionException: The email address is already in use by another account." -> {
                                val emailInputLayout: TextInputLayout = findViewById(R.id.emailInputLayout)
                                emailInputLayout.error = "The email address is already in use by another account."
                            }
                        }
                    }
                }
        }
    }

    private fun generateUsernames(name: String): MutableList<String> {
        val newNames = mutableListOf("Its$name",
            "By$name",
            "i${name}HD",
            "$name${Random.nextInt(0, 100)}",
            "$name$name",
            "The$name",
            "Best${name}inMCC",
            "Dockerizing$name",
            "${name}Vagrant",
            "A${name}V${name}C$name",
            "CR$name",
            "${name}YT",
            "QWERTY$name",
            "ASDFG$name",
            "ZXCV$name",
            "I${name}LoveFinland",
            "ChooseThis$name",
            "PleaseStopRepeatingNames$name")

        var foundThree = false
        while (!foundThree) {
            newNames.shuffle()
            foundThree = true
            for (i in 0..2) {
                if (checkIfNameExists(newNames[i])) foundThree = false
            }
        }

        return newNames.subList(0, 3)
    }

    private fun checkIfNameExists(name: String): Boolean {
        var exists: Boolean? = null
        val que = Volley.newRequestQueue(this)
        val req = JsonArrayRequest(
            Request.Method.GET, "$API/search?pre=$name", JSONArray(),
            Response.Listener {response ->
                exists = response.length() != 0
            }, Response.ErrorListener {
                println("MAN")
            }
        )
        que.add(req)

        return false // exists as Boolean
    }
}
