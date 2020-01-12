package com.cse4100g10.taskmanager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.transition.TransitionManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.getSpans
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.domain.ProjectType
import com.cse4100g10.taskmanager.utils.LoadPictureDialog
import com.cse4100g10.taskmanager.utils.VerticalImageSpan
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.yalantis.ucrop.UCrop
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CreateProjectActivity : AppCompatActivity() {
    private var keywords: MutableList<String> = mutableListOf()

    private var loadPictureDialog = LoadPictureDialog(this@CreateProjectActivity, LoadPictureDialog.CROP_SQUARE, "Select Project Icon")

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private val API:String = "https://mcc-fall-2019-g10.appspot.com/api/project"

    private var URIimage: Uri? = Uri.EMPTY


    @SuppressLint("SetTextI18n", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_project)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_clear_24dp)

        // Drop down menu
        val projectTypes = ProjectType.values().map { it.displayName }
        val adapter = ArrayAdapter(this, R.layout.dropdown_item_simple, projectTypes)
        val editTextFilledExposedDropdown: AutoCompleteTextView = findViewById(R.id.filled_exposed_dropdown)
        editTextFilledExposedDropdown.setAdapter(adapter)

        val timeEditText: EditText = findViewById(R.id.TimeEditText)
        val dateEditText: EditText = findViewById(R.id.DateEditText)
        val keywordsEditText: EditText = findViewById(R.id.et_keywords)
        val keywordsTil: TextInputLayout = findViewById(R.id.til_keywords)
        val mcvProfilepic:MaterialCardView = findViewById(R.id.mcv_profilepic)

        val nameLayout:TextInputLayout = findViewById(R.id.nameLayout)
        nameLayout.helperText = "*Required"

        val descriptionLayout:TextInputLayout = findViewById(R.id.descriptionLayout)
        descriptionLayout.helperText = "*Required"

        val dropDownTextInputLayout:TextInputLayout = findViewById(R.id.DropDownTextInputLayout)
        dropDownTextInputLayout.helperText = "*Required"

        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        timeEditText.setOnClickListener {
            showTimePickerDialog()
        }

        mcvProfilepic.setOnClickListener {
            loadPictureDialog.showDialog()
        }

        /**keywordsEditText.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                //if(after == 0 && charSequence.length>1){
                if(after < count){
                    //TODO: dafuq is this
                    /**keywordsEditText.setText("")
                    keywords.removeAt(keywords.size-1)
                    for(keyword in keywords){
                        val chip = ChipDrawable.createFromAttributes(this@CreateProjectActivity,null,0, R.style.Widget_MaterialComponents_Chip_Entry)
                        chip.text = keyword
                        chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)
                        var span = VerticalImageSpan(chip)
                        var editable = keywordsEditText.editableText
                        editable.setSpan(span, spannedLength, editable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        spannedLength = editable.length
                    }
                    //keywordsEditText.setText(keywordsEditText.text.toString().substring(0, keywordsEditText.length()-1))
                    keywordsEditText.setSelection(keywordsEditText.text.length)**/
                    /**
                     * //Log.e("lllll2", "${start+after-count} | ${start+after}")
                    var spans = keywordsEditText.editableText.getSpans<VerticalImageSpan>(start+count-after)
                    //var spans = keywordsEditText.editableText.getSpans<VerticalImageSpan>()
                    for(span in spans){
                        //if(keywordsEditText.editableText.getSpanStart())
                        keywordsEditText.editableText.removeSpan(span)
                    }**/
                }
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                //Log.e("llllll", "$charSequence - $start - $before - $count")
                keywordsTil.isErrorEnabled = false
                var editable = keywordsEditText.editableText
                var spans = editable.getSpans(0, editable.toString().length, VerticalImageSpan::class.java)
                for (s in spans)
                    s.refresh()
                if (count > before){
                    if(charSequence.subSequence(start, start+count).contains(" ")){
                        val list = charSequence.toString().split(" ")
                        val keyword = list[list.size-2]
                        if(!keywords.contains(keyword)) {
                            if( keywords.size >= 3){
                                keywordsTil.error = "Max. 3 keywords allowed"
                            }else{
                                Log.e("hhh", "added $keyword")
                                keywords.add(keyword)
                                val chip = ChipDrawable.createFromAttributes(this@CreateProjectActivity,null,0, R.style.Widget_MaterialComponents_Chip_Action)
                                chip.text = keyword
                                chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)
                                //chip.setPadding(100, 0, 0, 0)
                                //chip.isCloseIconVisible = false
                                var span = VerticalImageSpan(chip, keyword)
                                var spannedLength = editable.toString().substring(0, editable.toString().length-1).lastIndexOf(" ")
                                if (spannedLength==-1) spannedLength = 0
                                editable.setSpan(span, spannedLength, editable.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                        //spannedLength = editable.length
                    }
                }
                else{
                    var spans = keywordsEditText.editableText.getSpans<VerticalImageSpan>(start+count-1, start+count)
                    for(span in spans){
                        //if(keywordsEditText.editableText.getSpanStart())
                        keywordsEditText.editableText.removeSpan(span)
                        keywords.remove(span.toString())
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {
                /**if (editable.isNotEmpty() && editable.last()==' '){
                    val chip = ChipDrawable.createFromAttributes(this@CreateProjectActivity,null,0, R.style.Widget_MaterialComponents_Chip_Entry)
                    val list = editable.toString().split(" ")
                    keywords.add(list[list.size-2])
                    chip.text = keywords.last()
                    chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)
                    var span = VerticalImageSpan(chip)
                    editable.setSpan(span, spannedLength, editable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannedLength = editable.length
                }**/
            }
        })**/

        keywordsEditText.setOnEditorActionListener { v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEND) {
                // Should the following two lines be here or inside the if statement?
                val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                if( keywords.size >= 3){
                    keywordsTil.error = "Max. 3 keywords allowed"
                }else{
                    val chipGroup: ChipGroup = findViewById(R.id.chipGroup)
                    val chip = Chip(this)
                    //val chipDrawable = ChipDrawable.createFromAttributes(this,null,0, R.style.Widget_MaterialComponents_Chip_Choice)
                    //chip.setChipDrawable(chipDrawable)
                    chip.text = keywordsEditText.text.trim()
                    chip.setCloseIconResource(R.drawable.ic_delete_chip)
                    chip.isCloseIconVisible = true
                    chip.setOnCloseIconClickListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            TransitionManager.beginDelayedTransition(chipGroup)
                        }
                        keywords.remove(chip.text.toString())
                        chipGroup.removeView(chip)
                    }
                    keywords.add(chip.text.toString())
                    chipGroup.addView(chip)
                    keywordsEditText.setText("")
                }
            }
            false
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
                val aux = if(dayOfMonth.toString().length < 2) "0"+dayOfMonth.toString() else dayOfMonth.toString()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val ivPlaceholder:ImageView = findViewById(R.id.iv_placeholder)
        val ivProjectpic:ImageView = findViewById(R.id.iv_projectpic)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                LoadPictureDialog.ACTION_GALLERY -> loadPictureDialog.handleGalleryResult(data)
                LoadPictureDialog.ACTION_CAMERA -> loadPictureDialog.handleCameraResult(data)
                UCrop.REQUEST_CROP -> {
                    URIimage = loadPictureDialog.handleCropResult(data)
                    ivPlaceholder.visibility = View.GONE
                    ivProjectpic.visibility = View.VISIBLE
                    ivProjectpic.setImageURI(URIimage)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_project, menu)
        return true
    }

    private fun uploadFile(uri: Uri?, projectId: String){
        val progressDialog : AlertDialog = MaterialAlertDialogBuilder(this).setView(R.layout.dialog_indeterminate_progress).create()
        progressDialog.show()
        val storageRef = storage.reference
        val uploadTask = uri?.let { storageRef.child("projects/$projectId/icon.jpg").putFile(it) }
        uploadTask?.addOnFailureListener {
            progressDialog.dismiss()
            //Snackbar.make(findViewById(R.id.cc_root), R.string.file_upload_error, Snackbar.LENGTH_SHORT).show()
            this.finish()
        }?.addOnSuccessListener {
            progressDialog.dismiss()
            //Snackbar.make(findViewById(R.id.cc_root), R.string.file_upload_success, Snackbar.LENGTH_SHORT).show()
            this.finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                var requiredFullfiled: Boolean = true

                val nameEditText:EditText = findViewById(R.id.NameEditText)
                val descriptionEditText:EditText = findViewById(R.id.DescriptionEditText)
                val projectTypeTextView:AutoCompleteTextView = findViewById(R.id.filled_exposed_dropdown)
                val dateEditText: EditText = findViewById(R.id.DateEditText)
                val timeEditText: EditText = findViewById(R.id.TimeEditText)

                val name = nameEditText.text.toString()
                val description = descriptionEditText.text.toString()
                val projectType = projectTypeTextView.text.toString()
                val deadlineDate = dateEditText.text.toString()
                val deadlineTime = timeEditText.text.toString()
                val keywordsJ = JSONArray(keywords)
                // Chip texts
                val today: LocalDateTime = LocalDateTime.now()
                val formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")



                if (name == "") {
                    val nameLayout: TextInputLayout = findViewById(R.id.nameLayout)
                    nameLayout.error = "This field is required."
                    requiredFullfiled = false
                } else {
                    val nameLayout: TextInputLayout = findViewById(R.id.nameLayout)
                    nameLayout.isErrorEnabled = false
                }

                if (description == "") {
                    val descriptionLayout: TextInputLayout = findViewById(R.id.descriptionLayout)
                    descriptionLayout.error = "This field is required."
                    requiredFullfiled = false
                } else {
                    val descriptionLayout: TextInputLayout = findViewById(R.id.descriptionLayout)
                    descriptionLayout.isErrorEnabled = false
                }
                if(deadlineDate !="" && deadlineTime !=""){
                    val projectDate = LocalDateTime.parse(
                        deadlineDate + "T" + deadlineTime + ":00.000Z",
                        formatter
                    )
                    if(projectDate.isBefore(today) && projectDate.dayOfMonth != today.dayOfMonth){
                        val dateLayout: TextInputLayout = findViewById(R.id.dateLayout)
                        dateLayout.error = "Choose a correct date."
                        requiredFullfiled = false
                    }else{
                        val dateLayout: TextInputLayout = findViewById(R.id.dateLayout)
                        dateLayout.isErrorEnabled = false
                    }
                    if(projectDate.dayOfMonth == today.dayOfMonth){
                        if(projectDate.hour < today.hour) {
                            val timeLayout: TextInputLayout = findViewById(R.id.timeLayout)
                            timeLayout.error = "Choose a correct hour."
                            requiredFullfiled = false
                        }else if(projectDate.hour == today.hour){
                            if(projectDate.minute < today.minute){
                                val timeLayout: TextInputLayout = findViewById(R.id.timeLayout)
                                timeLayout.error = "Choose a correct hour."
                                requiredFullfiled = false
                            }else{
                                val timeLayout: TextInputLayout = findViewById(R.id.timeLayout)
                                timeLayout.isErrorEnabled = false
                            }
                        }else{
                            val timeLayout: TextInputLayout = findViewById(R.id.timeLayout)
                            timeLayout.isErrorEnabled = false
                        }
                    }else{
                        val timeLayout: TextInputLayout = findViewById(R.id.timeLayout)
                        timeLayout.isErrorEnabled = false
                    }
                }
                if (projectType == "") {
                    val dropDownTextInputLayout: TextInputLayout = findViewById(R.id.DropDownTextInputLayout)
                    dropDownTextInputLayout.error = "This field is required."
                    requiredFullfiled = false
                } else {
                    val dropDownTextInputLayout: TextInputLayout = findViewById(R.id.DropDownTextInputLayout)
                    dropDownTextInputLayout.isErrorEnabled = false
                }

                if (projectType != "" && description != "" && name != "") requiredFullfiled = true

                //var project = Project(name, description, photoUrl.toString(), projectType, deadlineDate, deadlineTime, keywords)

                if(requiredFullfiled){
                    val progressDialog : AlertDialog = MaterialAlertDialogBuilder(this).setView(R.layout.dialog_indeterminate_progress).create()
                    progressDialog.show()

                    val user = auth.currentUser
                    if(user != null){
                        val jsonobj = JSONObject()
                        jsonobj.put("admin", user.uid)
                        jsonobj.put("name", name)
                        jsonobj.put("description", description)
                        jsonobj.put("type", projectType)

                        if (deadlineDate != "" && deadlineTime != "") {
                            val deadline = deadlineDate + "T" + deadlineTime + ":00.000Z"
                            jsonobj.put("deadline", deadline)
                        } else {
                            val deadline = "0000-00-00" + "T" + "00:00" + ":00.000Z"
                            jsonobj.put("deadline", deadline)
                        }

                        jsonobj.put("keywords", keywordsJ)

                        val que = Volley.newRequestQueue(this)
                        val req = JsonObjectRequest(
                            Request.Method.POST,API,jsonobj,
                            Response.Listener {response ->
                                progressDialog.dismiss()
                                uploadFile(this.URIimage, response["id"] as String)
                            }, Response.ErrorListener { error ->
                                println("Error: $error")
                            }
                        )
                        que.add(req)
                    }
                }
            }
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}