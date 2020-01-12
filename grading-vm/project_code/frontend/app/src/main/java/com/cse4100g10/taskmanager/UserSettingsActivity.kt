package com.cse4100g10.taskmanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cse4100g10.taskmanager.utils.LoadPictureDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_user_settings.*

class UserSettingsActivity : AppCompatActivity() {
    lateinit var civProfilePic: CircleImageView
    lateinit var oldPassword: EditText
    lateinit var newPassword:EditText
    lateinit var save: Button
    lateinit var oldImage: Uri
    lateinit var newImage: Uri
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var loadPictureDialog = LoadPictureDialog(this, LoadPictureDialog.CROP_ROUND, "Select Profile Picture")



    private val GALLERY_REQUEST_CODE = 1
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)
        civProfilePic = findViewById(R.id.civ_profilepic)
        oldPassword = findViewById(R.id.originalPasswordText)
        newPassword = findViewById(R.id.passwordText)
        save = findViewById(R.id.Save)
        auth = FirebaseAuth.getInstance()
        oldImage = Uri.EMPTY
        newImage = Uri.EMPTY
        storage = FirebaseStorage.getInstance()

        civProfilePic.setOnClickListener{
            loadPictureDialog.showDialog()
        }
        var storageRef = storage.reference
        //Log.e("loading picture", "users/${auth.currentUser!!.email}/profile_picture.jpg")
        var url = storageRef.child("users/${auth.currentUser!!.email}/profile_picture.jpg").downloadUrl.addOnSuccessListener { url ->
                //Log.e("loading picture", url.toString())
                Picasso.get().load(url.toString()).into(civProfilePic)
                oldImage = url
        }


        save.setOnClickListener {
            val oldPassword = oldPassword.text.toString()
            val newPassword = newPassword.text.toString()
            val oldPasswordLayout: TextInputLayout = findViewById(R.id.oldPasswordLayout)
            val newPasswordLayout: TextInputLayout = findViewById(R.id.newPasswordLayout)

            val user = auth.currentUser
            val API = "https://mcc-fall-2019-g10.appspot.com/api/user/$user"
            val progressDialog: AlertDialog = MaterialAlertDialogBuilder(this).setView(R.layout.dialog_indeterminate_progress).create()
            progressDialog.show()
            if (user != null) {
                val email:String? = user.email
                oldPasswordLayout.isErrorEnabled = false
                newPasswordLayout.isErrorEnabled = false
                if(newPassword =="" && oldPassword!="" ) {
                    newPasswordLayout.error =
                        "Write the new password."
                    progressDialog.dismiss()

                }else if(oldImage != newImage && newPassword != ""){
                    auth = FirebaseAuth.getInstance()
                    uploadPicture(newImage, auth.currentUser!!.email.toString())
                    println("Picture updated")
                   changePassword(email,oldPassword, newPassword, user, progressDialog)

                }
                else if(oldImage != newImage ){
                    auth = FirebaseAuth.getInstance()
                    uploadPicture(newImage, auth.currentUser!!.email.toString())
                    println("Picture updated")
                    this.finish()
                }else if(newPassword != "") {
                    changePassword(email,oldPassword, newPassword, user, progressDialog)
                }

            }


        }

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
                        newImage = uri
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
    private fun changePassword(email:String?,oldPassword:String, newPassword:String, user : FirebaseUser, progressDialog: AlertDialog){
        if (oldPassword != "") {
            if (email != null) {
                auth.signInWithEmailAndPassword(email, oldPassword)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (newPassword != "") {
                                if (newPassword.length >= 6) {
                                    user.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            println("Password updated")
                                            this.finish()
                                        }
                                } else {
                                    newPasswordLayout.error =
                                        "New password not secure enough, write it correctly please."
                                    progressDialog.dismiss()
                                }
                            }
                        } else {
                            oldPasswordLayout.error =
                                "Wrong old password, write it correctly please."
                            progressDialog.dismiss()
                        }
                    }
            }
        } else {
            oldPasswordLayout.error =
                "To update the password, write the old password too."
            progressDialog.dismiss()
        }
    }


}
