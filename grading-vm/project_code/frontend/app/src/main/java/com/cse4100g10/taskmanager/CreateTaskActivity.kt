package com.cse4100g10.taskmanager

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.transition.TransitionManager
import android.view.View.GONE
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.domain.Project
import com.cse4100g10.taskmanager.domain.ProjectType
import com.cse4100g10.taskmanager.utils.MyJsonObjectRequest
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class CreateTaskActivity : AppCompatActivity() {

    lateinit var taskName: EditText
    lateinit var descriptionEditText: EditText
    lateinit var timeEditText: EditText
    lateinit var dateEditText: EditText
    lateinit var assignMem: AutoCompleteTextView
    //lateinit var assignedList: TextView
    lateinit var upload: Button
    lateinit var save: Button
    lateinit var status: String
    lateinit var image: Uri

    lateinit var project: Project


    private val GALLERY_REQUEST_CODE = 1
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101
    private var isImage = false

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val members: MutableList<String> = mutableListOf()
    private val membersInformation: MutableList<MutableList<String>> = mutableListOf()
    private val membersAdded: MutableList<String> = mutableListOf()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        taskName = findViewById(R.id.nameEditText)
        val nameLayout:TextInputLayout = findViewById(R.id.nameLayout)
        nameLayout.helperText = "*Required"

        descriptionEditText = findViewById(R.id.descriptionEditText)
        val descriptionLayout:TextInputLayout = findViewById(R.id.descriptionLayout)
        descriptionLayout.helperText = "*Required"

        timeEditText = findViewById(R.id.TimeEditText)
        val timeLayout:TextInputLayout = findViewById(R.id.timeLayout)
        timeLayout.helperText = "*Required"
        dateEditText = findViewById(R.id.DateEditText)
        val dateLayout:TextInputLayout = findViewById(R.id.dateLayout)
        dateLayout.helperText = "*Required"
        //assignedList = findViewById(R.id.assignedListText)
        assignMem = findViewById(R.id.assignedEditText)
        upload = findViewById(R.id.imageDesButton)
        save = findViewById(R.id.bt_save)
        status = "pending"
        project = intent.getSerializableExtra("EXTRA_PROJECT") as Project

        val adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, members)
        assignMem.setAdapter(adapter)
        assignMem.threshold = 5
        //assignedList.text = members.toString()


        val assignedTextView: MaterialTextView = findViewById(R.id.assignedTextView)
        val assignedLayout: TextInputLayout = findViewById(R.id.assignedLayout)

        if (project.projectType == ProjectType.PERSONAL || project.adminID != auth.uid) {
            assignedTextView.visibility = GONE
            assignedLayout.visibility = GONE
            //assignedList.visibility = GONE
        }


        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        timeEditText.setOnClickListener {
            showTimePickerDialog()
        }

        upload.setOnClickListener {
            setGalleryPermissions()

        }
        assignMem.setOnClickListener {
            if(!assignMem.enoughToFilter()){
                assignMem.dismissDropDown()
            }
        }
        assignMem.setOnItemClickListener { adapterView, view, i, l ->
            if (!membersAdded.contains(assignMem.text.toString())) {
                membersAdded.add(assignMem.text.toString())

                val chipGroup: ChipGroup = findViewById(R.id.chipGroup)
                val chip = Chip(this)
                chip.text = assignMem.text
                chip.setCloseIconResource(R.drawable.ic_delete_chip)
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(chipGroup)
                    }
                    chipGroup.removeView(chip)
                    membersAdded.remove(chip.text.toString())
                }
                chipGroup.addView(chip)
            }
            assignMem.setText("")
        }

        save.setOnClickListener {
            val API =
                "https://mcc-fall-2019-g10.appspot.com/api/project/${project.projectID}/tasks"


            if (project.projectType.displayName == "Personal") membersAdded.add(auth.uid.toString())
            println("HEREEEEEEEEEEEEEEE => $membersAdded")
            if (membersAdded.size == 1 || project.projectType.displayName == "Personal") {
                status = "ongoing"
            }
            var requiredFulfilled: Boolean = true
            val name = taskName.text.toString()
            val description = descriptionEditText.text.toString()
            val deadlineDate = dateEditText.text.toString()
            val deadlineTime = timeEditText.text.toString()
            val membersAddedInTask: String = membersAdded.toString()
            if (name == "") {
                val nameLayout: TextInputLayout = findViewById(R.id.nameLayout)
                nameLayout.error = "This field is required."
                requiredFulfilled = false
            } else {
                val nameLayout: TextInputLayout = findViewById(R.id.nameLayout)
                nameLayout.isErrorEnabled = false
            }

            if (description == "") {
                val descriptionLayout: TextInputLayout = findViewById(R.id.descriptionLayout)
                descriptionLayout.error = "This field is required."
                requiredFulfilled = false
            } else {
                val descriptionLayout: TextInputLayout = findViewById(R.id.descriptionLayout)
                descriptionLayout.isErrorEnabled = false
            }
            if (deadlineDate == "" ) {
                val dateLayout: TextInputLayout = findViewById(R.id.dateLayout)
                dateLayout.error = "This field is required."
                requiredFulfilled = false
            } else {
                val dateLayout: TextInputLayout = findViewById(R.id.dateLayout)
                dateLayout.isErrorEnabled = false
            }
            if (deadlineTime == "" ) {
                val timeLayout: TextInputLayout = findViewById(R.id.timeLayout)
                timeLayout.error = "This field is required."
                requiredFulfilled = false
            } else {
                val timeLayout: TextInputLayout = findViewById(R.id.timeLayout)
                timeLayout.isErrorEnabled = false
            }
            if (requiredFulfilled) {
                val progressDialog: AlertDialog =
                    MaterialAlertDialogBuilder(this).setView(R.layout.dialog_indeterminate_progress)
                        .create()
                progressDialog.show()
                val user = auth.currentUser
                if (user != null) {
                    // ADD TASK
                    val jsonobj = JSONObject()
                    jsonobj.put("description", description)
                    jsonobj.put("status", status)
                    jsonobj.put("name", name)

                    if (deadlineDate != "" && deadlineTime != "") {
                        val deadline = deadlineDate + "T" + deadlineTime + ":00.000Z"
                        jsonobj.put("deadline", deadline)
                    } else {
                        val deadline = "0000-00-00" + "T" + "00:00" + ":00.000Z"
                        jsonobj.put("deadline", deadline)
                    }

                    val jsonarray = JSONArray()
                    jsonarray.put(jsonobj)

                    println(jsonarray.toString())

                    val que = Volley.newRequestQueue(this)
                    val req = MyJsonObjectRequest(
                        Request.Method.POST, API, jsonarray,
                        Response.Listener { response ->
                            progressDialog.dismiss()
                            //Log.e("TASK", "YAAAAY")
                            println("RESPONSEEEE: $response")
                            val taskID: String = (response["tidList"] as JSONArray)[0] as String

                            // ADD MEMBERS
                            for (i in 0 until membersInformation.size) {
                                if (membersAddedInTask.contains(membersInformation[i][0]) || membersAddedInTask.contains(membersInformation[i][1])) {
                                    val USER_MEMBER_API =
                                        "https://mcc-fall-2019-g10.appspot.com/api/project/${project.projectID}/tasks/$taskID/assigned_to/${membersInformation[i][0]}"

                                    val que2 = Volley.newRequestQueue(this)
                                    val req2 = JsonObjectRequest(
                                        Request.Method.PUT, USER_MEMBER_API, JSONObject(),
                                        Response.Listener { response ->
                                            println("ADDED!")
                                        }, Response.ErrorListener { error ->
                                            println("Error: $error")
                                        }
                                    )
                                    que2.add(req2)
                                }
                            }

                            this.finish()
                        }, Response.ErrorListener { error ->
                            println("Error: $error")
                        }
                    )
                    que.add(req)
                }

            }
        }

        loadMembers()
    }

    private fun loadMembers() {
        for (i in 0 until project.members.size) {
            val USER_API = "https://mcc-fall-2019-g10.appspot.com/api/user/${project.members[i]}"

            val que = Volley.newRequestQueue(this)
            val req = JsonObjectRequest(
                Request.Method.GET, USER_API, JSONObject(),
                Response.Listener { response ->
                    membersInformation.add(mutableListOf(project.members[i], response["name"] as String))
                    members.add(response["name"] as String)
                }, Response.ErrorListener { error ->
                    println("Error in some member CreateTaskActivity: $error")
                }
            )
            que.add(req)
        }
    }

    private fun setGalleryPermissions() {
        if (ContextCompat.checkSelfPermission(this@CreateTaskActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?

            ActivityCompat.requestPermissions(this@CreateTaskActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

        } else {
            // Permission has already been granted
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryIntent.type = "image/*"
                    startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(applicationContext, "Error in the permissions to access the gallery!", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }
            else -> {}
        }
    }

    //handle result of picked image
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    if (data != null) {
                        image = data.data!!
                    }
                    isImage = true
                    detectImage()
                }
                else -> {}
            }
        }
    }


    private fun detectImage(){
        if (isImage) {

            val imageFirebase: FirebaseVisionImage = FirebaseVisionImage.fromFilePath(this, image)

            val detector = FirebaseVision.getInstance()
                .onDeviceTextRecognizer

            detector.processImage(imageFirebase)
                .addOnSuccessListener { firebaseVisionText ->
                    // Task completed successfully
                    descriptionEditText.setText(firebaseVisionText.text)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    Toast.makeText(
                        applicationContext,
                        "Error in detecting the text in the image!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateEditText: EditText = findViewById(R.id.DateEditText)

        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val aux = if(dayOfMonth.toString().length < 2) "0$dayOfMonth" else dayOfMonth.toString()
                dateEditText.setText(year.toString() + "-" + (monthOfYear + 1) + "-" + aux)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun showTimePickerDialog() {
        val cal = Calendar.getInstance()
        val timeEditText:EditText = findViewById(R.id.TimeEditText)

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            timeEditText.setText(SimpleDateFormat("HH:mm").format(cal.time))
        }
        TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }
    private fun getMembersProject():Array<String>{
        val API = "https://mcc-fall-2019-g10.appspot.com/api/project/${project.projectID}"
        var list:Array<String> = emptyArray<String>()
            val jsonobj = JSONObject()
            val que = Volley.newRequestQueue(this)

            val req = JsonObjectRequest(
                Request.Method.GET,API,jsonobj,
                Response.Listener {
                        response ->
                    val projectsMembers:JSONArray = if(response.has("members"))  response["members"] as JSONArray else JSONArray()
                    list  = Array(projectsMembers.length()) {
                        projectsMembers.getString(it)
                    }

                }, Response.ErrorListener {
                    println("Error")
                    print(it)
                }
            )
            que.add(req)
            return list
    }
}

