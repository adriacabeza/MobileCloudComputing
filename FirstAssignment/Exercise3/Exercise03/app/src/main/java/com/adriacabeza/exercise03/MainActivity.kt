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
import java.lang.Exception


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var button = findViewById<Button>(R.id.button)
        var edit = findViewById<EditText>(R.id.editText)
        var list:ListView = findViewById(R.id.list)
        var adapter = ListViewAdapter(this,ArrayList<PhotoList>())

        button.setOnClickListener {
            var photoList = ArrayList<PhotoList>()
            var url = edit.text.toString()
            try{
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
                            }
                            adapter = ListViewAdapter(this@MainActivity, photoList)
                            runOnUiThread {
                                list.adapter = adapter
                            }
                        }

                    }
                })
            } catch (e:Exception){
                println("Error")
            }
        }


        list.setOnItemClickListener{parent, view, position, id ->
            val element = adapter.getItem(position)// The item that was clicked
            val intent = Intent(this, ImageActivity::class.java)
            intent.putExtra("Author", element.author)
            intent.putExtra("Url", element.url)
            startActivity(intent)
        }


        }



}



