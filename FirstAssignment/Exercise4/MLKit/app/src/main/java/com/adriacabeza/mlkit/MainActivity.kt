package com.adriacabeza.mlkit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.io.IOException


class MainActivity : AppCompatActivity() {
    val SELECT_PICTURE : Int = 4567
    lateinit var image:ImageView
    lateinit var uri:Uri

    val detector = FirebaseVision.getInstance().cloudTextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image = findViewById(R.id.imageView)
        var detect: Button = findViewById(R.id.detect)
        var upload: Button = findViewById(R.id.upload)
        var texto: TextView = findViewById(R.id.showText)
        upload.setOnClickListener {
            dispatchGalleryIntent()
        }

        // DETECTION
        detect.setOnClickListener {
            if (uri != null) {
                val image: FirebaseVisionImage
                try {
                    image = FirebaseVisionImage.fromFilePath(this, uri)
                    var result = detector.processImage(image)
                        .addOnSuccessListener { firebaseVisionText ->
                            Toast.makeText(this,  "Task completed succesfully", Toast.LENGTH_LONG).show()
                            texto.text = firebaseVisionText.text
                        }

                        .addOnFailureListener {
                            Toast.makeText(this,  "Task failed", Toast.LENGTH_LONG).show()
                        }


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    fun dispatchGalleryIntent(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_PICTURE)
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK){
            try{
                uri = data!!.data!!
                println("Uri: $uri")
                image.setImageURI(uri)
            }catch (e : IOException){
                e.printStackTrace()
            }
        }

    }


}
