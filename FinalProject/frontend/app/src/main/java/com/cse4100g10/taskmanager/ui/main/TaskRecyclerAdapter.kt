package com.cse4100g10.taskmanager.ui.main

import android.app.Activity
import android.graphics.Paint
import android.os.Build
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.ProjectActivity
import com.cse4100g10.taskmanager.R
import com.cse4100g10.taskmanager.domain.Project
import com.cse4100g10.taskmanager.domain.Task
import com.cse4100g10.taskmanager.utils.MyJsonObjectRequest
import com.cse4100g10.taskmanager.utils.inflate
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.recycleritem_task.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class TaskRecyclerAdapter(private var tasks: MutableList<Task>, private var activity: ProjectActivity, private var project: Project) : RecyclerView.Adapter<TaskHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val inflatedView = parent.inflate(R.layout.recycleritem_task, false)
        return TaskHolder(inflatedView, activity, project)
    }

    override fun getItemCount() =  tasks.size


    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val task = tasks[position]
        holder.bindTask(task)
    }
}

class TaskHolder(val view: View, activity: ProjectActivity, project: Project) : RecyclerView.ViewHolder(view) {
    private lateinit var task: Task
    val activity = activity
    val project = activity.project
    var membersToAssign = mutableListOf<String>()

    init {
        view.setOnClickListener {
            //view.cb_completed.toggle()
            //MaterialAlertDialogBuilder(activity).setTitle(task.name).setMessage(task.description).create().show()
            val view = activity.layoutInflater.inflate(R.layout.dialog_task_info, null)
            if(view.parent !=null)  (view.parent as ViewGroup).removeView(view)
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val tvStatus: TextView = view.findViewById(R.id.tv_status)
            val tvDeadline: TextView = view.findViewById(R.id.tv_deadline)
            val tvDescr: TextView = view.findViewById(R.id.tv_description)
            tvName.text = task.name
            tvStatus.text = task.status
            tvDeadline.text = "Due for: ${task.deadlineDate.replace("-","/")} ${task.deadlineTime}"
            tvDescr.text = task.description

            if(task.assignedTo.size > 0){
                for (memberId in task.assignedTo) {
                    val memberName = activity.projectMembers[memberId]
                    view.findViewById<LinearLayout>(R.id.ll_users).visibility = View.VISIBLE
                    val cgUsers: ChipGroup = view.findViewById(R.id.cg_assigned_users)
                    val chip = Chip(activity)
                    chip.text = memberName
                    chip.setChipBackgroundColorResource(R.color.colorPrimaryLight)
                    chip.setTextColor(activity.resources.getColor(android.R.color.white))
                    cgUsers.addView(chip)
                }
            }

            MaterialAlertDialogBuilder(activity)
                .setView(view)
                .setPositiveButton("Assign Members") {_, _ ->
                    showAssignMemberDialog()
                }
                .setNegativeButton("Delete Task") {_, _ ->
                    activity.deleteTask(task)
                }
                .show()
        }

        view.cb_completed.setOnCheckedChangeListener { _, isChecked: Boolean ->
            task.completed = isChecked
            var status = ""
            if (isChecked) status = "completed"
            else {
                if (project.projectType.displayName == "Personal" || task.assignedTo.size >= 1) status = "ongoing"
                else status = "pending"
            }
            updateTaskStatus(status)
            updateTaskView()
        }
    }

    fun bindTask(task : Task) {
        this.task = task
        updateTaskView()
    }

    private fun updateTaskView(){
        view.tv_taskname.text = task.name
        view.cb_completed.isChecked = task.completed
        //view.tv_taskname.isEnabled = !task!!.completed
        view.tv_taskname.alpha =    if (task.completed)
                                        0.6f
                                    else
                                        1f
        view.tv_taskname.paintFlags =   if (task.completed)
                                            view.tv_taskname.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                        else
                                            view.tv_taskname.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }

    private fun showAssignMemberDialog(){
        var availableMembers = activity.projectMembers.filter { !task.assignedTo.contains(it.key) }.values.toList()
        if(availableMembers.isNotEmpty()){
            val view = activity.layoutInflater.inflate(R.layout.dialog_add_member, null)
            val userEditText = view.findViewById(R.id.usersEditText) as AutoCompleteTextView

            val adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, availableMembers)
            userEditText.setAdapter(adapter)
            userEditText.threshold = 1

            userEditText.setOnClickListener {
                if(!userEditText.enoughToFilter()){
                    userEditText.dismissDropDown()
                }
            }
            val chipGroup: ChipGroup = view.findViewById(R.id.chipGroup)

            userEditText.setOnItemClickListener { adapterView, v, i, l ->
                if (!membersToAssign.contains(userEditText.text.toString())) {
                    membersToAssign.add(userEditText.text.toString())
                    val chip = Chip(activity)
                    chip.text = userEditText.text
                    chip.setCloseIconResource(R.drawable.ic_delete_chip)
                    chip.isCloseIconVisible = true
                    chip.setOnCloseIconClickListener {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            TransitionManager.beginDelayedTransition(chipGroup)
                        }
                        chipGroup.removeView(chip)
                        membersToAssign.remove(chip.text.toString())
                    }
                    chipGroup.addView(chip)
                }
                userEditText.setText("")
            }

            MaterialAlertDialogBuilder(activity)
                .setTitle("Assign members")
                .setPositiveButton("Add"){_,_ ->
                    sendMembersToAssign()
                }
                .setView(view)
                .show()
        }else{
            Snackbar.make(view, "No more available users in the project", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun sendMembersToAssign(){
        var count = membersToAssign.size
        val progressDialog: AlertDialog = MaterialAlertDialogBuilder(activity).setView(R.layout.dialog_indeterminate_progress).create()
        if (count > 0) progressDialog.show()
        for ((memberId, memberName) in activity.projectMembers.filter { membersToAssign.contains(it.value) }){
            val API = "https://mcc-fall-2019-g10.appspot.com/api/project/${activity.project.projectID}/tasks/${task.taskID}/assigned_to/$memberId"

            val USER_API = "https://mcc-fall-2019-g10.appspot.com/api/user/"
            val que3 = Volley.newRequestQueue(activity)
            val req3 = JsonObjectRequest(
                Request.Method.GET, USER_API + memberId, JSONObject(),
                Response.Listener {response ->
                    notification(response["msgToken"] as String)
                }, Response.ErrorListener {
                    println("MAN ERROR $it")
                }
            )
            que3.add(req3)

            task.assignedTo.add(memberId)

            val que = Volley.newRequestQueue(activity)
            val req = MyJsonObjectRequest(
                Request.Method.PUT, API, JSONArray(),
                Response.Listener { response ->
                    if (--count == 0){
                        updateTaskStatus("ongoing")
                        progressDialog.dismiss()
                        Snackbar.make(view!!, "${membersToAssign.size} new member(s) assigned to task '${task.name}'", Snackbar.LENGTH_SHORT).show()
                        membersToAssign.clear()
                        activity.getAll()
                    }
                    println("RESPONSE: $response")
                }, Response.ErrorListener { error ->
                    println("Error: $error")
                }
            )
            que.add(req)
        }
    }

    private fun notification(to: String) {
        /*if (!TextUtils.isEmpty(something)) {

        }*/

        val notification = JSONObject()
        val notificationBody = JSONObject()

        try {
            notificationBody.put("title", "You have been added to task \"${task.name}\" in project \"${project.name}\"!")
            notificationBody.put("body", "See what things you have to do!")
            //notificationBody.put("registration_ids", jsonarray)
            //Log.e("TAG", "TO => $to, nBody => $notificationBody")
            notification.put("to", to)
            notification.put("notification", notificationBody)
            //notification.put("from", "165683726790")
            //Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: ${e.stackTrace}")
            Log.e("TAG", "onCreate: ${e.message}")
        }

        sendNotification(notification)
    }

    private fun sendNotification(notification: JSONObject) {
        val FCM_API = "https://fcm.googleapis.com/fcm/send"
        val serverKey = "key=AAAAJpOFCcY:APA91bFdD_sG6WEUdEtjDWOHdOD_zxcvimyYQbuCkXU2L71lTd4gI2U09WNPecmL64WBKfW7Katu58p62lYbpJGT0bM14XPlLkIkE_zeCVJoQtD04n2islSYJNMDJ2FK9scrviHjP64Y"
        val contentType = "application/json"

        //Log.e("TAG", "TOOOOO => " + (notification["to"] as String))

        val requestQueue: RequestQueue by lazy {
            Volley.newRequestQueue(activity?.applicationContext)
        }

        Log.e("TAG", "Send notification")
        Log.e("TAG", notification.toString())

        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> {response ->
                Log.e("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(activity, "RequestError", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onResponseError: Didn't work")
                Log.i("TAG", it.message.toString())
                Log.i("TAG", it.stackTrace.toString())
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                //super.getHeaders()

                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType

                return params
            }
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun updateTaskStatus(status: String){
        task.status = status

        val TASK_API = "https://mcc-fall-2019-g10.appspot.com/api/project/${project.projectID}/tasks/${task.taskID}"

        val jsonobj = JSONObject()
        jsonobj.put("status", status)

        val que = Volley.newRequestQueue(activity)
        val req = JsonObjectRequest(
            Request.Method.PUT,TASK_API,jsonobj,
            Response.Listener {
                    response ->
                println("Response successfully done TaskRecyclerAdapter task status changing => $response")
            }, Response.ErrorListener {
                println("Error TaskRecyclerAdapter task status changing => $it")
            }
        )
        que.add(req)
    }
}