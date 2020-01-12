package com.cse4100g10.taskmanager

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.domain.Project
import com.cse4100g10.taskmanager.domain.ProjectType
import com.cse4100g10.taskmanager.ui.main.ProjectListPagerAdapter
import com.cse4100g10.taskmanager.utils.InputStreamVolleyRequest
import com.cse4100g10.taskmanager.utils.toMutableList
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ProjectListActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var USER_API:String = "https://mcc-fall-2019-g10.appspot.com/api/user/p/"
    private var TOKEN_API:String=  "https://mcc-fall-2019-g10.appspot.com/api/user/"
    private var PROJECT_API:String = "https://mcc-fall-2019-g10.appspot.com/api/project/"
    private var PDF_API:String = "https://us-central1-mcc-fall-2019-g10.cloudfunctions.net/pdf/"
    private lateinit var pagerAdapter: ProjectListPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        USER_API += auth.uid
        setContentView(R.layout.activity_project_list)
        pagerAdapter = ProjectListPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = pagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)?.setIcon(pagerAdapter.TAB_ICONS[0])
        tabs.getTabAt(1)?.setIcon(pagerAdapter.TAB_ICONS[1])
        tabs.getTabAt(2)?.setIcon(pagerAdapter.TAB_ICONS[2])
        viewPager.currentItem = 1
        viewPager.offscreenPageLimit = 4
        FirebaseApp.initializeApp(this)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            val intent = Intent(this, CreateProjectActivity::class.java)
            startActivity(intent)
        }

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("error", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token
                Log.e("token", token.toString())
                if (token != null) {
                    sendRegistrationToServer(token)
                }
            })

    }


    fun sendRegistrationToServer(token:String){
        val jsonobj = JSONObject()
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        jsonobj.put("msgToken",token)
        val que = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(
            Request.Method.PUT,TOKEN_API+auth.uid,jsonobj,
            Response.Listener {
                    response ->
                //println(response["message"].toString())
                println(response.toString())
            }, Response.ErrorListener {
                println("Error")
            }
        )
        que.add(req)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addProjectFavourites(project: Project){
        project.isFavourite = true
        pagerAdapter.getFavouritesFragment()?.projectAdded(0, project, false)
        pagerAdapter.getMainFragment()?.dataChanged()
        pagerAdapter.getUpcomingFragment()?.dataChanged()
    }
    fun removeProjectFavourites(project: Project){
        project.isFavourite = false
        pagerAdapter.getFavouritesFragment()?.projectRemoved(project)
        pagerAdapter.getMainFragment()?.dataChanged()
        pagerAdapter.getUpcomingFragment()?.dataChanged()

    }

    private fun getResolution(resolution:String):String{
        return when (resolution) {
            "0" -> "@s_480"
            "1" -> "@s_960"
            else -> ""
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        pagerAdapter.getFavouritesFragment()?.showLoading()
        pagerAdapter.getMainFragment()?.showLoading()
        pagerAdapter.getUpcomingFragment()?.showLoading()
        val jsonobj = JSONObject()
        val que = Volley.newRequestQueue(this)

        val req = JsonObjectRequest(
            Request.Method.GET,USER_API,jsonobj,
            Response.Listener {
                    response ->
                val projectsFavourites:JSONArray = if(response.has("favorites"))  response["favorites"] as JSONArray else JSONArray()
                //Log.d("favorites", projectsFavourites.toString())
                val projects = response["projects"] as JSONArray
                for (i in 0 until projects.length()) {
                    val project = projects.getJSONObject(i)
                    val projectID = project["id"]
                    var keywords= project["keywords"] as JSONArray
                    val hasAttachments = project["attachments"] != JSONArray()
                    val hasDeadline: Boolean = project["deadline"] != "0000-00-00T00:00:00.000Z"
                    //Log.e("deadline_deadline", project["deadline"].toString())
                    val currentDay=  if (hasDeadline) project["deadline"].toString().subSequence(0,10).toString() else ""
                    val currentTime= if (hasDeadline) project["deadline"].toString().subSequence(11,16).toString() else ""
                    //Log.e("deadline_day and time", currentDay)
                    //Log.e("deadline_day and time", currentTime)
                    var favorites = project["favoriteOf"] as JSONArray
                    var isFavourite = false
                    //Log.d("listoffavourites",  favorites.toString())
                    //Log.d("userID", auth.uid)
                    for(j in 0 until favorites.length()){
                        if(favorites[j].toString() == auth.uid)
                            isFavourite = true
                            break
                    }
                    //Log.d("prova", isFavourite.toString())
                    var aux:MutableList<String> = mutableListOf()
                    for(j in 0 until keywords.length()){
                        aux.add(keywords[j].toString())
                    }

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
                    pagerAdapter.getMainFragment()?.projectAdded(0, projectObject, false)

                    if(hasDeadline) {
                        val formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                        val projectDate = LocalDateTime.parse(
                            projectObject.deadlineDate.toString() + "T" + projectObject.deadlineTime.toString() + ":00.000Z",
                            formatter
                        )
                        //Log.e("deadline date", projectObject.deadlineDate.toString())
                        //Log.e("deadline time", projectObject.deadlineTime.toString())
                        //Log.e("deadline projectDate", projectDate.toString())
                        val currentPlusSeven: LocalDateTime =
                            LocalDateTime.now().plusDays(7)
                        //Log.e("deadline Seven", currentPlusSeven.toString())
                        if (projectDate.isBefore(currentPlusSeven)) {
                            //Log.e("deadline deadlineDate", projectObject.deadlineDate)
                            pagerAdapter.getUpcomingFragment()?.projectAdded(-1, projectObject, true)
                        }
                    }

                    for (i in 0 until projectsFavourites.length()) {
                        if(projectID == (projectsFavourites[i] as String)){
                            pagerAdapter.getFavouritesFragment()?.projectAdded(0, projectObject, false)
                            break
                        }
                    }

                    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    var resolution: String? = prefs.getString("key_upload_quality", "")
                    resolution = getResolution(resolution.toString())
                    //Log.e("resolution", resolution)
                    //Log.e("resolution", "projects/$projectID/icon$resolution.jpg")
                        storageRef.child("projects/$projectID/icon$resolution.jpg")
                        .downloadUrl.addOnSuccessListener {
                            val url = it.toString()

                            projectObject.photoUrl = url

                            pagerAdapter.getFavouritesFragment()?.projectUpdated(projectObject)
                            pagerAdapter.getMainFragment()?.projectUpdated(projectObject)
                            pagerAdapter.getUpcomingFragment()?.projectUpdated(projectObject)
                        }
                }
                /**pagerAdapter.getFavouritesFragment()?.dataChanged()
                pagerAdapter.getMainFragment()?.dataChanged()
                pagerAdapter.getUpcomingFragment()?.dataChanged()**/
            }, Response.ErrorListener {
                println("Error")
                print(it)
            }
        )
        que.add(req)

    }

    fun downloadProject (project : Project?){
        var progressDialog : AlertDialog = MaterialAlertDialogBuilder(this).setView(R.layout.dialog_indeterminate_progress).create()
        progressDialog.show()
        val stringAPI = PDF_API+project?.projectID
        val que = Volley.newRequestQueue(this)
        //Log.e("resolution", stringAPI)
        val req = InputStreamVolleyRequest(
            Request.Method.GET,
            stringAPI.toUri(),
            Response.Listener{
                try {
                    val path = this.externalCacheDir
                    val newFile = File(path, project!!.name +".pdf")
                    val outputStream: FileOutputStream
                    outputStream = FileOutputStream(newFile)
                    outputStream.write(it)
                    outputStream.close()
                    progressDialog.dismiss()
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    val uri: Uri = FileProvider.getUriForFile(this, this.applicationContext.packageName+".fileprovider", newFile)
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(newFile.extension)
                    intent.setDataAndType(uri, mimeType)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    this.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener {
                println("Error")
                print(it.message)
            }
            , null)
        que.add(req)
    }




    fun deleteProject (project : Project?){
        val jsonobj = JSONObject()
        var stringAPI = PROJECT_API+project?.projectID
        val que = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(
            Request.Method.DELETE, stringAPI,jsonobj,
            Response.Listener {
                    response ->
                //println(response["message"].toString())
                println(response.toString())
            }, Response.ErrorListener {
                println("Error")
            }
        )
        que.add(req)


        pagerAdapter.getFavouritesFragment()?.projectRemoved(project)
        pagerAdapter.getMainFragment()?.projectRemoved(project)
        pagerAdapter.getUpcomingFragment()?.projectRemoved(project)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_project_list, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.search)
        (searchItem.actionView as SearchView).apply {
            //setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setSearchableInfo(searchManager.getSearchableInfo(ComponentName(this@ProjectListActivity, SearchProjectResultsActivity::class.java)))
        }
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                /**val menuIterator = menu.iterator()
                while(menuIterator.hasNext()){
                    val menuItem = menuIterator.next()
                    if (menuItem.itemId != R.id.search) menuItem.isVisible = false
                }**/
                findViewById<View>(R.id.cg_searchtags).animate().alpha(1f).withStartAction {
                    findViewById<View>(R.id.cg_searchtags).visibility = View.VISIBLE
                }

                val chipGroup: ChipGroup = findViewById(R.id.cg_searchtags)
                chipGroup.removeAllViews()
                val jsonArray = JSONArray()
                val API = "https://mcc-fall-2019-g10.appspot.com/api/user/${auth.uid}/meta/keywords"
                val que = Volley.newRequestQueue(this@ProjectListActivity)
                val req = JsonArrayRequest(
                    Request.Method.POST,API,jsonArray,
                    Response.Listener {response ->
                        for (i in 0 until response.length()) {
                            val chip = Chip(this@ProjectListActivity)
                            chip.text = response[i] as String
                            chip.setOnClickListener {
                                val query = chip.text.toString()
                                val searchIntent = Intent(this@ProjectListActivity, SearchProjectResultsActivity::class.java)
                                searchIntent.action = Intent.ACTION_SEARCH
                                searchIntent.putExtra(SearchManager.QUERY, query)
                                searchIntent.putExtra(SearchManager.EXTRA_SELECT_QUERY, "chip")
                                startActivity(searchIntent)
                            }
                            chipGroup.addView(chip)
                        }
                    }, Response.ErrorListener { error ->
                        println("Error: $error")
                    }
                )
                que.add(req)

                findViewById<View>(R.id.tabs).visibility = View.GONE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    findViewById<View>(R.id.cg_searchtags).animate().alpha(0f).withEndAction {
                    findViewById<View>(R.id.cg_searchtags).visibility = View.GONE
                    findViewById<View>(R.id.tabs).visibility = View.VISIBLE
                }
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings ->{
                val aboutIntent = Intent(this, SettingsActivity::class.java)
                startActivity(aboutIntent)
            }

            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                finish()
                val logInIntent = Intent(this, LogInActivity::class.java)
                startActivity(logInIntent)
            }

            R.id.userSettings -> {
                val userSettingsIntent = Intent(this, UserSettingsActivity::class.java)
                startActivity(userSettingsIntent)
            }

        }
        return super.onOptionsItemSelected(item)
    }


    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser == null) {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)

        }
    }

}

private fun TabLayout.addOnTabSelectedListener(function: () -> Unit) {

}
