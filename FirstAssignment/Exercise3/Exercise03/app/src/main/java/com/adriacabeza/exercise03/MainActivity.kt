package com.adriacabeza.exercise03

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {

    var photoList = ArrayList<PhotoList>()
    lateinit var list: ListView
    val adapter = ListViewAdapter(this,photoList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var button = findViewById<Button>(R.id.button)
        var edit = findViewById<EditText>(R.id.editText)
        list = findViewById(R.id.list)



        button.setOnClickListener {
            var urlText = edit.text.toString()
            downloadJson(urlText)
        }

        list.adapter = adapter

        list.setOnItemClickListener{parent, view, position, id ->
            val element = adapter.getItem(position)// The item that was clicked
            val intent = Intent(this, ImageActivity::class.java)
            intent.putExtra("Author", element.author)
            intent.putExtra("Url", element.url)
            startActivity(intent)
        }


        }

    private fun downloadJson(url:String){

        println(url)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    var StringJson = response.body()?.string() as String
                    var Jarray = JSONArray(StringJson)
                    var objeto: JSONObject
                    for (x in (0 until Jarray.length()).toList()){
                        objeto = Jarray.getJSONObject(x)
                        var item = PhotoList(objeto.getString("photo"),objeto.getString("author"))
                        photoList.add(item)
                        this@MainActivity.runOnUiThread {
                            adapter.notifyDataSetChanged()}
                    }

                }

            }
        })
    }


}



