package com.adriacabeza.exercise01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var count = 0
        var inputText = findViewById<EditText>(R.id.editText)
        var buttonUpperLower = findViewById<Button>(R.id.upperLowerButton)
        var buttonCount = findViewById<Button>(R.id.charCount)
        var textCount= findViewById<TextView>(R.id.countChar)
        var textUpperLower= findViewById<TextView>(R.id.textUpperLower)



        buttonCount.setOnClickListener{
            var texto = inputText.text
            textCount.text = "Count: "+ texto.length.toString()
        }

        buttonUpperLower.setOnClickListener{
            var texto = inputText.text
            println("Text: $texto")
            if(count %2 == 0) {
                textUpperLower.text = "Uppercase/Lowercase: "+ texto.toString().toUpperCase()
                println(textUpperLower.text)
            }
            else{
                textUpperLower.text = "Uppercase/Lowercase: "+texto.toString().toLowerCase()
                println(textUpperLower.text)
            }
            count += 1
        }

    }
}
