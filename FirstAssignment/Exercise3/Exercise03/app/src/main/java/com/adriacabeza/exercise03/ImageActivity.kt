package com.adriacabeza.exercise03

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class ImageActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_view)

        var text = findViewById<TextView>(R.id.showAuthor)
        var image = findViewById<ImageView>(R.id.showImage)

        var temp_author =  intent.getStringExtra("Author")
        var temp_image = intent.getStringExtra("Url")

        text.text = temp_author
        Picasso.get().load(temp_image).into(image)
    }

}