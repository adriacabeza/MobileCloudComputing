package com.adriacabeza.rotateimage

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.FileProvider
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class MainActivity : AppCompatActivity() {
    val SELECT_PICTURE : Int = 4567
    val CAMERA_REQUEST_CODE: Int = 4577


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var image: ImageView = findViewById(R.id.imageView)
        var rotate: Button = findViewById(R.id.rotate)
        var upload: Button = findViewById(R.id.upload)

        upload.setOnClickListener {
            showDialog()
        }

        // ROTATION
        rotate.setOnClickListener{
            image.rotation = image.rotation + 90.0.toFloat()
        }

    }

    fun dispatchGalleryIntent(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, SELECT_PICTURE)
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        var image: ImageView = findViewById(R.id.imageView)
        if(requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK){
            try{
                val uri = data!!.data
                image.setImageURI(uri)
            }catch (e : IOException){
                e.printStackTrace()
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            image.setImageBitmap(imageBitmap)
        }

    }

    private fun showDialog() {
        val array = arrayOf("Camera","Gallery", "Cancel")
        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(this)

        // Set a title for alert dialog
        builder.setTitle("Select Options")
        builder.setItems(array) { _, which ->
            // Get the dialog selected item
            val selected = array[which]
            when(selected) {
                    "Gallery" -> dispatchGalleryIntent()
                    "Camera" -> captureFromCamera()
                    "Cancel" -> {}
            }
        }

        // Create a new AlertDialog using builder objecACTION_GET_CONTENTACTION_GET_CONTENTACTION_GET_CONTENTt
        val dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()

    }

    private fun captureFromCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
            }
        }
        startActivityForResult(intent, CAMERA_REQUEST_CODE)


    }


}
