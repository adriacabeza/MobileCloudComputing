package com.cse4100g10.taskmanager

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.domain.*
import com.cse4100g10.taskmanager.domain.ListItem.Companion.TYPE_HEADER
import com.cse4100g10.taskmanager.ui.main.ProjectPagerAdapter
import com.cse4100g10.taskmanager.utils.LoadPictureDialog
import com.cse4100g10.taskmanager.utils.MyJsonArrayRequest
import com.cse4100g10.taskmanager.utils.MyJsonObjectRequest
import com.cse4100g10.taskmanager.utils.toMutableList
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ProjectActivity : AppCompatActivity() {
    lateinit var storage: FirebaseStorage
    private var loadPictureDialog = LoadPictureDialog(this, LoadPictureDialog.CROP_NONE, "Upload Image")
    private val PROJECT_API:String = "https://mcc-fall-2019-g10.appspot.com/api/project/"
    private val UPLOAD_FILES = 20
    private val CREATE_TASK = 30
    private lateinit var resolution: String
    lateinit var project : Project
    var projectMembers = mutableMapOf<String, String>()
    var allUsersNames: MutableList<String> = mutableListOf()
    var allUsersIds = ArrayList<String>()

    private val imageTypes = arrayListOf<String>(".jpg",".jpeg",".png",".gif")
    private val docTypes = arrayListOf<String>(".docx",".doc",".md",".tex",".txt")
    private val sheetTypes = arrayListOf<String>(".xls",".xlt",".xlsx")
    private val drawingTypes = arrayListOf<String>(".ppt")
    private val audioTypes = arrayListOf<String>(".mp3",".wav",".dct")
    private val videoTypes = arrayListOf<String>(".mp4",".avi",".mov",".mkv")

    val tasksList : MutableList<Task> = mutableListOf()
    val picturesList : MutableList<ListItem> = mutableListOf()
    val filesList : MutableList<ListItem> = mutableListOf()

    private lateinit var pagerAdapter: ProjectPagerAdapter
    lateinit var fab: FloatingActionButton

    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        auth = FirebaseAuth.getInstance()

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        resolution = getResolution(prefs.getString("key_upload_quality", "").toString())
        project = intent.getSerializableExtra(ProjectHolder.PROJECT_KEY) as Project

        var toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.title = project.name
        //actionBar?.title = project.name

        pagerAdapter = ProjectPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.offscreenPageLimit = 4
        viewPager.adapter = pagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                val animX = ObjectAnimator.ofFloat(fab, "scaleX", 0f)
                val animY = ObjectAnimator.ofFloat(fab, "scaleY", 0f)
                val animSetXY = AnimatorSet()
                animSetXY.playTogether(animX, animY)
                animSetXY.duration = 100
                animSetXY.start()

                var newTab = pagerAdapter.TAB_TITLES[tab.position]
                if (newTab != R.string.info && !(newTab == R.string.tasks && (auth.uid != project.adminID))){
                    animSetXY.addListener(object : Animator.AnimatorListener{
                        override fun onAnimationEnd(animation: Animator?) {
                            fab.setImageResource(pagerAdapter.FAB_ICONS[tab.position])
                            fab.visibility = View.VISIBLE
                            val animX = ObjectAnimator.ofFloat(fab, "scaleX", 1.0f)
                            val animY = ObjectAnimator.ofFloat(fab, "scaleY", 1.0f)
                            val animSetXY = AnimatorSet()
                            animSetXY.playTogether(animX, animY)
                            animSetXY.duration = 100
                            animSetXY.start()
                        }
                        override fun onAnimationRepeat(animation: Animator?) {}
                        override fun onAnimationCancel(animation: Animator?) {}
                        override fun onAnimationStart(animation: Animator?) {}

                    })
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        storage = FirebaseStorage.getInstance()

        fab = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            when (pagerAdapter.getCurrentFragmentType()){
                 R.string.tasks -> {
                    val i = Intent(applicationContext, CreateTaskActivity::class.java)
                     i.putExtra("EXTRA_PROJECT", project)
                     startActivityForResult(i, CREATE_TASK)

                }
                R.string.pictures -> {
                    loadPictureDialog.showDialog()

                }
                R.string.files -> {
                    val i = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    startActivityForResult(i, UPLOAD_FILES)
                }
            }
        }
        getAll()
    }

    fun getType(name:String):String{
        if(name.endsWith(".pdf")){
            return "pdf"
        }
        for(i in imageTypes){
            if(name.toLowerCase().endsWith(i)){
                return "image"
            }
        }


        for(i in docTypes){
            if(name.toLowerCase().endsWith(i)){
                return "doc"
            }
        }
        for(i in audioTypes){
            if(name.toLowerCase().endsWith(i)){
                return "audio"
            }
        }

        for(i in drawingTypes){
            if(name.toLowerCase().endsWith(i)){
                return "drawing"
            }
        }

        for(i in sheetTypes){
            if(name.toLowerCase().endsWith(i)){
                return "sheet"
            }
        }

        for(i in videoTypes){
            if(name.endsWith(i)){
                return "video"
            }
        }
        return ""
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun uploadFile(uri: Uri?, isImage: Boolean){
        var progressDialog : AlertDialog = MaterialAlertDialogBuilder(this).setView(R.layout.dialog_indeterminate_progress).create()
        progressDialog.show()
        var storageRef = storage.reference
        var folder = if (isImage) "images" else "files"
        var projectId = project.projectID
        var name =  uri?.let { DocumentFile.fromSingleUri(this, it)?.name }
        var url = storageRef.child("projects/$projectId/$folder/$name")

        var uploadTask = uri?.let { url.putFile(it) }
        var urlTask = uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                progressDialog.dismiss()
                Snackbar.make(findViewById(R.id.cc_root), R.string.file_upload_error, Snackbar.LENGTH_SHORT).show()
            }
            url.downloadUrl
        }?.addOnCompleteListener{ task->
            progressDialog.dismiss()
            if(task.isSuccessful){
                val downloadUri = task.result

                var jsonobj = JSONObject()
                jsonobj.put("url", downloadUri)

                if(isImage) {
                    jsonobj.put("type","image")
                    jsonobj.put("name", name)
                } else {
                    jsonobj.put("type", name?.let { getType(it) })
                    jsonobj.put("name", name)
                }
                var jsonArray: JSONArray= JSONArray()
                jsonArray.put(jsonobj)
                val que = Volley.newRequestQueue(this)
                val req = MyJsonObjectRequest(
                    Request.Method.POST,PROJECT_API+project.projectID+"/attachments",jsonArray,
                    Response.Listener { response ->
                        print("Picture was succesfully uploaded")
                        println(response.toString())
                        //Log.e("response", response.toString())
                    }, Response.ErrorListener {
                        println("Error")
                    }
                )
                que.add(req)

                if(isImage) {
                    Snackbar.make(findViewById(R.id.cc_root), R.string.image_upload_success, Snackbar.LENGTH_SHORT).show()
                    pagerAdapter.getPicturesFragment()?.showLoading()
                }else {
                    Snackbar.make(findViewById(R.id.cc_root), R.string.file_upload_success, Snackbar.LENGTH_SHORT).show()
                    pagerAdapter.getFilesFragment()?.showLoading()
                }
                getAll()
                project.hasAttachments = true

            } else{
                Snackbar.make(findViewById(R.id.cc_root), R.string.file_upload_error, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                LoadPictureDialog.ACTION_GALLERY -> {
                    var uri = loadPictureDialog.handleGalleryResult(data)
                    uploadFile(uri, true)
                }
                LoadPictureDialog.ACTION_CAMERA -> {
                    var uri = loadPictureDialog.handleCameraResult(data)
                    uploadFile(uri, true)
                }
                UPLOAD_FILES -> {
                    if (data != null) {
                        var file = data.getData()
                        uploadFile(file, false)
                    }
                }
                CREATE_TASK -> {
                    getAll()
                    pagerAdapter.getTasksFragment()?.showLoading()
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

    private fun getResolution(resolution:String):String{
        return when (resolution) {
            "0" -> "@s_480"
            "1" -> "@s_960"
            else -> ""
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun getAll() {
        getAllUsers()
        getProjectMembersNames()
        val projectID = project.projectID
        val API = PROJECT_API + projectID
        val jsonobj = JSONObject()
        val que = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(
            Request.Method.GET,API,jsonobj,
            Response.Listener { response ->
                //TASKS
                val tasksResponse:JSONArray = response["tasks"] as JSONArray
                println("Response: $tasksResponse")
                tasksList.clear()
                for (i in 0 until tasksResponse.length()) {
                    val item: JSONObject = tasksResponse.getJSONObject(i)
                    val deadlineDate=  item["deadline"].toString().subSequence(0,10).toString()
                    val deadlineTime= item["deadline"].toString().subSequence(11,16).toString()
                    //if (! tasksList.any { it.name == item["name"].toString()}){
                    tasksList.add(Task(item["id"] as String, item["name"] as String, item["description"] as String, item["status"] == "completed", (item["assigned_to"] as JSONArray).toMutableList(), item["status"] as String, deadlineDate, deadlineTime))
                    //}
                }
                pagerAdapter.getTasksFragment()?.dataChanged()

                //PICTURES AND FILES
                val attachments = response["attachments"] as JSONArray

                var previousFileDate: LocalDateTime = LocalDateTime.now()
                var previousPictureDate: LocalDateTime = LocalDateTime.now()
                var filesIndex = 0
                var picturesIndex = 0

                var filesFragment = pagerAdapter.getFilesFragment()
                var picturesFragment = pagerAdapter.getPicturesFragment()

                for(i in 0 until attachments.length()) {
                    val str = attachments.getJSONObject(i)["created"] as String
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                    val currentItemDate = LocalDateTime.parse(str, formatter)
                    val type= attachments.getJSONObject(i)["type"] as String
                    if (type != "image") {
                        if (!filesList.filter {it.type== ListItem.TYPE_FILE}.any { (it as File_).name == attachments.getJSONObject(i)["name"].toString()}) {
                            /////////////////////////////////////// FIRST DAY /////////////////////////////
                            val today: LocalDateTime = LocalDateTime.now()
                            if (today.dayOfMonth == currentItemDate.dayOfMonth && today.month == currentItemDate.month && today.year == currentItemDate.year) {
                                if (filesList.size > 0 && filesList[0].type == ListItem.TYPE_HEADER && (filesList[0] as Header).name == "Today") {
                                    filesList.add(1, File_(
                                        attachments.getJSONObject(i)["name"].toString(),
                                        type,
                                        attachments.getJSONObject(i)["id"].toString(),
                                        attachments.getJSONObject(i)["url"].toString()
                                    ))

                                    filesFragment?.fileAdded(1)
                                } else {
                                    filesList.add(0, Header("Today"))
                                    filesFragment?.fileAdded(0)
                                    filesIndex++

                                    filesList.add(1, File_(
                                        attachments.getJSONObject(i)["name"].toString(),
                                        type,
                                        attachments.getJSONObject(i)["id"].toString(),
                                        attachments.getJSONObject(i)["url"].toString()
                                    ))
                                    filesFragment?.fileAdded(1)
                                }
                            } //////////////////////////////////////////////////////////////////////////////
                            else {
                                if (currentItemDate.dayOfMonth == previousFileDate.dayOfMonth && currentItemDate.month == previousFileDate.month && currentItemDate.year == previousFileDate.year) {
                                    // SAME DATE
                                    filesList.add(filesIndex, File_(
                                        attachments.getJSONObject(i)["name"].toString(),
                                        type,
                                        attachments.getJSONObject(i)["id"].toString(),
                                        attachments.getJSONObject(i)["url"].toString()
                                    ))
                                    filesFragment?.fileAdded(filesIndex)
                                } else {
                                    // NEW HEADER
                                    val yesterday = LocalDateTime.now().minusDays(1)
                                    if (currentItemDate.dayOfMonth == yesterday.dayOfMonth && currentItemDate.month == yesterday.month && currentItemDate.year == yesterday.year) {
                                        filesList.add(filesIndex, Header("Yesterday"))
                                    } else {
                                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                        filesList.add(filesIndex, Header(currentItemDate.format(formatter)))
                                    }
                                    filesFragment?.fileAdded(filesIndex)
                                    filesIndex++

                                    filesList.add(filesIndex, File_(
                                        attachments.getJSONObject(i)["name"].toString(),
                                        type,
                                        attachments.getJSONObject(i)["id"].toString(),
                                        attachments.getJSONObject(i)["url"].toString()
                                    ))
                                    filesFragment?.fileAdded(filesIndex)
                                }

                            }
                            filesIndex++
                            previousFileDate = currentItemDate
                        }
                    } else {
                        if (!picturesList.filter {it.type==ListItem.TYPE_PICTURE}.any { (it as Picture).id == attachments.getJSONObject(i)["id"].toString()}) {
                            val name = attachments.getJSONObject(i)["name"].toString()
                            //Log.e("nameputa", name)
                            storage.reference.child("projects/${project?.projectID}/images/"+ name.dropLast(4) + resolution + ".jpg" )
                                .downloadUrl.addOnSuccessListener { url ->
                                //Log.e("url_puta", url.toString())
                                    val today: LocalDateTime = LocalDateTime.now()
                                    if (today.dayOfMonth == currentItemDate.dayOfMonth && today.month == currentItemDate.month && today.year == currentItemDate.year) {
                                        if (picturesList.size > 0 && picturesList[0].type == ListItem.TYPE_HEADER && (picturesList[0] as Header).name == "Today") {
                                            picturesList.add(
                                                1,
                                                Picture(
                                                    url.toString(),
                                                    attachments.getJSONObject(i)["id"].toString(),
                                                    attachments.getJSONObject(i)["name"].toString()
                                                )
                                            )
                                            picturesFragment?.pictureAdded(1)
                                        } else {
                                            picturesList.add(0, Header("Today"))
                                            picturesFragment?.pictureAdded(0)
                                            picturesIndex++

                                            picturesList.add(
                                                1,
                                                Picture(
                                                    url.toString(),
                                                    attachments.getJSONObject(i)["id"].toString(),
                                                    attachments.getJSONObject(i)["name"].toString()
                                                )
                                            )
                                            picturesFragment?.pictureAdded(1)
                                        }
                                    } else {
                                        if (currentItemDate.dayOfMonth == previousPictureDate.dayOfMonth && currentItemDate.month == previousPictureDate.month && currentItemDate.year == previousPictureDate.year) {
                                            // SAME DATE
                                            picturesList.add(
                                                picturesIndex,
                                                Picture(
                                                    url.toString(),
                                                    attachments.getJSONObject(i)["id"].toString(),
                                                    attachments.getJSONObject(i)["name"].toString()
                                                )
                                            )
                                            picturesFragment?.pictureAdded(picturesIndex)
                                        } else {
                                            // NEW HEADER
                                            val yesterday = LocalDateTime.now().minusDays(1)
                                            if (currentItemDate.dayOfMonth == yesterday.dayOfMonth && currentItemDate.month == yesterday.month && currentItemDate.year == yesterday.year) {
                                                picturesList.add(picturesIndex, Header("Yesterday"))
                                                picturesFragment?.pictureAdded(picturesIndex)
                                                picturesIndex++
                                            } else {
                                                val formatter =
                                                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                                picturesList.add(
                                                    picturesIndex,
                                                    Header(currentItemDate.format(formatter))
                                                )
                                                picturesFragment?.pictureAdded(picturesIndex)
                                                picturesIndex++
                                            }

                                            picturesList.add(
                                                picturesIndex,
                                                Picture(
                                                    url.toString(),
                                                    attachments.getJSONObject(i)["id"].toString(),
                                                    attachments.getJSONObject(i)["name"].toString()
                                                )
                                            )
                                            picturesFragment?.pictureAdded(picturesIndex)
                                        }
                                    }
                                    picturesIndex++
                                    previousPictureDate = currentItemDate
                                }.addOnFailureListener {
                               //Log.e("Error Loading URL", it.message.toString())
                            }

                        }
                    }
                }
                picturesFragment?.dataChanged()
                filesFragment?.dataChanged()

            }, Response.ErrorListener { error ->
                //Log.e("Error on response", error.toString())
            }
        )
        que.add(req)
    }

    private fun getProjectMembersNames(){
        for (i in 0 until project.members.size) {
            val memberId = project.members[i]
            val USER_API = "https://mcc-fall-2019-g10.appspot.com/api/user/$memberId"

            val que = Volley.newRequestQueue(this)
            val req = JsonObjectRequest(
                Request.Method.GET, USER_API, JSONObject(),
                Response.Listener { response ->
                    projectMembers[memberId] = response["name"] as String
                    pagerAdapter.getInfoFragment()?.updateMembers()
                }, Response.ErrorListener { error ->
                    println("Error in getting members names: $error")
                }
            )
            que.add(req)
        }
    }

    fun getAllUsers(){
        val API = "https://mcc-fall-2019-g10.appspot.com/api/user/all"
        val que = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(
            Request.Method.GET, API, JSONObject(),
            Response.Listener { response ->
                parseUsers(response["all"] as JSONArray)
            }, Response.ErrorListener { error ->
                println("Error in retrieving users: $error")
            }
        )
        que.add(req)

    }

    fun parseUsers(users: JSONArray){
        for (i in 0 until users.length()) {
            val item:JSONObject = users.getJSONObject(i)
            //Log.e("members", members.toString())
            //Log.e("members_name", item["name"].toString())
            if(!allUsersNames.contains(item["name"])) {
                allUsersNames.add(item["name"].toString())
                allUsersIds.add(item["id"].toString())
            }
        }
    }

    fun deleteFile (file : File_?){
        val jsonobj = JSONObject()
        var stringAPI = PROJECT_API+project?.projectID+"/attachments/"+ file!!.id
        val que = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(
            Request.Method.DELETE, stringAPI,jsonobj,
            Response.Listener {
                    response ->
                println(response.toString())
            }, Response.ErrorListener {
                println("Error")
            }
        )
        que.add(req)

        updateAttachments(project)

        // Create a storage reference from our app
        val storageRef = storage.reference

        // Create a reference to the file to delete
        val desertRef = storageRef.child("projects/${project?.projectID}/files/${file!!.name}")

        // Delete the file
        desertRef.delete().addOnSuccessListener {
            // File deleted successfully
            Snackbar.make(findViewById(R.id.cc_root), "File deleted sucessfully", Snackbar.LENGTH_SHORT).show()
        }.addOnFailureListener {
            // Uh-oh, an error occurred!
            Snackbar.make(findViewById(R.id.cc_root), "Something went wrong", Snackbar.LENGTH_SHORT).show()
        }

        filesList?.indexOf(file)?.let {
            filesList?.removeAt(it)
            pagerAdapter.getFilesFragment()?.fileRemoved(it)
        }

        // See if there is a Header with no attachments
        // removeHeaderWithNoAttachments(filesList, (pagerAdapter.getFilesFragment()) as Fragment)
        var previousIsHeader = false
        for (i in 1 until filesList.size) {
            if (filesList[i].type == TYPE_HEADER) {
                previousIsHeader = if (previousIsHeader) {
                    filesList.removeAt(i - 1)
                    pagerAdapter.getFilesFragment()?.fileRemoved(i - 1)
                    true
                } else {
                    false
                }
            }
        }

        if (filesList.size == 1) {
            filesList.removeAt(0)
            pagerAdapter.getFilesFragment()?.fileRemoved(0)
        }
    }

    fun deletePicture (picture : Picture?){
        val jsonobj = JSONObject()
        var stringAPI = PROJECT_API+project?.projectID+"/attachments/"+picture!!.id
        val que = Volley.newRequestQueue(this)
        var req = JsonObjectRequest(
            Request.Method.DELETE, stringAPI,jsonobj,
            Response.Listener {
                    response ->
                println(response.toString())
            }, Response.ErrorListener {
                println("Error")
            }
        )
        que.add(req)

        updateAttachments(project)

        // Create a storage reference from our app
        val storageRef = storage.reference

        // Create a reference to the file to delete
        val desertRef = storageRef.child("projects/${project?.projectID}/images/${picture!!.name}")

        // Delete the file
        desertRef.delete().addOnSuccessListener {
            // File deleted successfully
            Snackbar.make(findViewById(R.id.cc_root), "Picture deleted sucessfully", Snackbar.LENGTH_SHORT).show()
        }.addOnFailureListener {
            // Uh-oh, an error occurred!
            Snackbar.make(findViewById(R.id.cc_root), "Something went wrong", Snackbar.LENGTH_SHORT).show()
        }

        picturesList?.indexOf(picture)?.let {
            picturesList?.removeAt(it)
            pagerAdapter.getPicturesFragment()?.pictureRemoved(it)
        }

        // See if there is a Header with no attachments
        // removeHeaderWithNoAttachments(picturesList, (pagerAdapter.getPicturesFragment()) as Fragment)
        var previousIsHeader = false
        for (i in 1 until picturesList.size) {
            if (picturesList[i].type == TYPE_HEADER) {
                previousIsHeader = if (previousIsHeader) {
                    picturesList.removeAt(i - 1)
                    pagerAdapter.getPicturesFragment()?.pictureRemoved(i - 1)
                    true
                } else {
                    false
                }
            }
        }

        if (picturesList.size == 1) {
            picturesList.removeAt(0)
            pagerAdapter.getPicturesFragment()?.pictureRemoved(0)
        }
    }

    private fun updateAttachments(project: Project) {
        val projectAPI = "https://mcc-fall-2019-g10.appspot.com/api/project/${project.projectID}"
        val jsonobj = JSONObject()
        val que = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(
            Request.Method.GET, projectAPI,jsonobj,
            Response.Listener {
                    response ->
                project.hasAttachments = response["attachments"] == JSONArray()
            }, Response.ErrorListener {
                println("Error2 updateAttachments => ProjectActivity.kt")
            }
        )
        que.add(req)
    }

    /*private fun removeHeaderWithNoAttachments(list: MutableList<ListItem>, fragment: Fragment) {
        var previousIsHeader = false
        for (i in 1 until list.size) {
            if (list[i].type == TYPE_HEADER) {
                previousIsHeader = if (previousIsHeader) {
                    list.removeAt(i - 1)
                    pagerAdapter.getPicturesFragment()?.pictureRemoved(i - 1)
                    true
                } else {
                    false
                }
            }
        }
    }*/

    fun deleteTask(task: Task) {
        val projectAPI = "https://mcc-fall-2019-g10.appspot.com/api/project/${project.projectID}/tasks/${task.taskID}"

        val que = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(
            Request.Method.DELETE, projectAPI,JSONObject(),
            Response.Listener {
                    response ->
                    // response == projectID
                    tasksList?.indexOf(task)?.let {
                        tasksList?.removeAt(it)
                        pagerAdapter.getTasksFragment()?.taskRemoved(it)
                    }
            }, Response.ErrorListener {
                println("Error2 updateAttachments => ProjectActivity.kt")
            }
        )
        que.add(req)
    }
}