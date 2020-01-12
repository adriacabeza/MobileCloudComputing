package com.cse4100g10.taskmanager

import android.app.SearchManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.domain.Project
import com.cse4100g10.taskmanager.domain.ProjectType
import com.cse4100g10.taskmanager.ui.main.ProjectListFragment
import com.cse4100g10.taskmanager.utils.MyJsonArrayRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONArray
import org.json.JSONObject

class SearchProjectResultsActivity : AppCompatActivity() {

    lateinit var fragment : ProjectListFragment
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        setContentView(R.layout.activity_search_project_results)
        fragment = supportFragmentManager.findFragmentById(R.id.projectListFragment) as ProjectListFragment
        handleIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val user_id = auth.uid
            val query = intent.getStringExtra(SearchManager.QUERY)
            //Log.e("Query", query)
            //request body: {query: ['key', 'word', 'here']}
            val jsonobj = JSONObject()
            jsonobj.put("on", "projects")
            val typeOfQuery = intent.getStringExtra(SearchManager.EXTRA_SELECT_QUERY)

            if (typeOfQuery == "chip") {
                jsonobj.put("byKeyword", query)
            } else {
                jsonobj.put("byName", query)
            }

            val que = Volley.newRequestQueue(this)
            //Log.e("Query", jsonobj.toString())
            val req = MyJsonArrayRequest(
                Request.Method.POST,
                "https://mcc-fall-2019-g10.appspot.com/api/user/$user_id/search",
                jsonobj,
                Response.Listener { response ->
                    for (i in 0 until response.length()) {
                        val item = response.getJSONObject(i)
                        //Log.e("Query", item["id"] as String)

                        /*var project = Project(
                            "name",
                            "description",
                            "",
                            "as4125gsag"
                        )
                        fragment.projectAdded(i, project)*/

                        ////////////////////////////////////////////////////////////////////////////
                        val id = (item["id"] as String)
                        val PROJECT_API:String = "https://mcc-fall-2019-g10.appspot.com/api/project/"
                        val API_PROJECT = PROJECT_API + id
                        val que_2 = Volley.newRequestQueue(this)
                        val req_2 = JsonObjectRequest(
                            Request.Method.GET, API_PROJECT, jsonobj,
                            Response.Listener { project ->
                                val projectID = project["id"]
                                var keywords= project["keywords"] as JSONArray
                                val hasDeadline: Boolean = project["deadline"] != "0000-00-00T00:00:00.000Z"
                                val currentDay=  if (hasDeadline) project["deadline"].toString().subSequence(0,10).toString() else ""
                                val currentTime= if (hasDeadline) project["deadline"].toString().subSequence(11,16).toString() else ""
                                var isFavourite = false
                                var url: String? = null
                                val hasAttachments = project["attachments"] != JSONArray()
                                val aux:MutableList<String> = mutableListOf()
                                val favorites = project["favoriteOf"] as JSONArray
                                //Log.d("listoffavourites",  favorites.toString())
                                //Log.d("userID", auth.uid)
                                for(j in 0 until favorites.length()){
                                    if(favorites[j].toString() == auth.uid)
                                        isFavourite = true
                                    break
                                }
                                for(j in 0 until keywords.length()){
                                    aux.add(keywords[j].toString())
                                }
                                storageRef.child("projects/$id/icon.jpg").downloadUrl.addOnSuccessListener {
                                    url = it.toString()

                                    val projectObject = Project(
                                        project["name"] as String,
                                        project["description"] as String,
                                        url,
                                        ProjectType.getFromString(project["type"] as String),
                                        currentDay,
                                        currentTime,
                                        aux,
                                        projectID.toString(),
                                        hasAttachments,
                                        isFavourite,
                                        project["updated"] as String,
                                        project["admin"] as String,
                                        (project["members"] as JSONArray).toMutableList()
                                    )

                                    fragment.projectAdded(-1, projectObject, false)
                                }.addOnFailureListener {
                                    val projectObject = Project(
                                        project["name"] as String,
                                        project["description"] as String,
                                        null,
                                        ProjectType.getFromString(project["type"] as String),
                                        currentDay,
                                        currentTime,
                                        aux,
                                        projectID.toString(),
                                        hasAttachments,
                                        isFavourite,
                                        project["updated"] as String,
                                        project["admin"] as String,
                                        (project["members"] as JSONArray).toMutableList()
                                    )

                                    fragment.projectAdded(-1, projectObject, false)
                                }


                                //if(i == ids.length()-1){
                                    //fragment.loadingFinished()
                                //}
                            }, Response.ErrorListener {
                                println("Error")
                                print(it)
                            }
                        )
                        que_2.add(req_2)
                        ////////////////////////////////////////////////////////////////////////////
                    }

                    println(response.toString())
                },
                Response.ErrorListener { error ->
                    //Log.e("Query", "Errorsitoooo: $error")
                }
            )
            que.add(req)

            var tv_query = findViewById<TextView>(R.id.tv_query)
            tv_query.text = "Searched for '"+query+"'"
        }
    }

    private fun JSONArray.toMutableList(): MutableList<String> = MutableList(length(), this::get) as MutableList<String>
}