package com.cse4100g10.taskmanager

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.domain.Project
import com.cse4100g10.taskmanager.domain.ProjectType
import com.cse4100g10.taskmanager.utils.MyJsonObjectRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_project_info.view.*
import kotlinx.android.synthetic.main.recycleritem_project.view.iv_projectpic
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/**
 * A placeholder fragment containing a simple view.
 */
class ProjectInfoFragment : Fragment() {

    private lateinit var pActivity: ProjectActivity
    private lateinit var project: Project
    private var usersAdded: MutableList<String> = mutableListOf()
    private var idsAdded: JSONArray = JSONArray()
    private lateinit var cgMembers: ChipGroup
    private lateinit var tvNMembers: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        pActivity = activity as ProjectActivity
        project = pActivity.project
        val root = inflater.inflate(R.layout.fragment_project_info, container, false)

        var tvDescription: TextView = root.tv_project_description
        tvDescription.text = project.description

        //if(project.deadlineDate?.isNotEmpty() && project.deadlineTime?.isNotEmpty()){
        if(project.hasDeadline()){
            var tvDeadline: TextView = root.tv_deadline
            tvDeadline.text = "Due for: ${project.deadlineDate?.replace("-","/")} ${project.deadlineTime}"
            tvDeadline.visibility = View.VISIBLE
        }

        try{
            Picasso.get().load(project.photoUrl).into(root.iv_projectpic, object: com.squareup.picasso.Callback {
                override fun onError(e: Exception?) {}
                override fun onSuccess() {
                    root.iv_placeholder.visibility = View.GONE
                    root.iv_projectpic.visibility = View.VISIBLE
                }
            })
        } catch (e: Exception) {
            //Log.e("YES", "PICASSO ERROR RECYCLER ADAPTER: $e")
        }

        var tvType: TextView = root.tv_project_type
        tvType.text = "${project.projectType.displayName} Project"

        val view = inflater.inflate(R.layout.dialog_add_member, null)
        val userEditText = view.findViewById(R.id.usersEditText) as AutoCompleteTextView

        val adapter = ArrayAdapter(this.context!!, android.R.layout.simple_list_item_1, pActivity.allUsersNames)
        userEditText.setAdapter(adapter)
        userEditText.threshold = 5

        userEditText.setOnClickListener {
            if(!userEditText.enoughToFilter()){
                userEditText.dismissDropDown()
            }
        }
        val chipGroup: ChipGroup = view.findViewById(R.id.chipGroup)


        userEditText.setOnItemClickListener { adapterView, v, i, l ->
            if (!usersAdded.contains(userEditText.text.toString())) {
                usersAdded.add(userEditText.text.toString())
                Log.e("all users IDS", pActivity.allUsersIds.toString())
                Log.e("users added to string", usersAdded.toString())
                idsAdded.put(pActivity.allUsersIds[pActivity.allUsersNames.indexOf(userEditText.text.toString())])
                val chip = Chip(this.context)
                chip.text = userEditText.text
                chip.setCloseIconResource(R.drawable.ic_delete_chip)
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(chipGroup)
                    }
                    chipGroup.removeView(chip)
                    usersAdded.remove(chip.text.toString())
                    idsAdded.remove(usersAdded.indexOf(userEditText.text.toString()))
                }
                chipGroup.addView(chip)
            }
            userEditText.setText("")
        }

        if (project.projectType == ProjectType.GROUP){
            var nMembers: Int = project.members.size
            tvNMembers = root.tv_project_n_members
            tvNMembers.visibility = View.VISIBLE
            tvNMembers.text = " \u2022 $nMembers member(s)"

            var btAddMember: Button = root.bt_add_member
            btAddMember.visibility = View.VISIBLE

            btAddMember.setOnClickListener{
                if(view.parent !=null)  (view.parent as ViewGroup).removeView(view)

                MaterialAlertDialogBuilder(context)
                    .setTitle("Add users")
                    .setPositiveButton("Add"){_,_ ->
                        sendUsers()
                        chipGroup.removeAllViews()
                    }
                    .setView(view)
                    .show()
            }
        }

        val cgKeywords: ChipGroup = root.findViewById(R.id.cg_keywords)
        for (keyword in project.keywords) {
            val chip = Chip(pActivity)
            chip.text = keyword
            //chip.background = pActivity.resources.getColor(R.color.colorSecondary)
            cgKeywords.addView(chip)
        }

        cgMembers = root.findViewById(R.id.cg_members)

        return root
    }

    private fun sendUsers(){
        if(usersAdded.isNotEmpty()) {
            val progressDialog: AlertDialog =
                MaterialAlertDialogBuilder(this.context).setView(R.layout.dialog_indeterminate_progress)
                    .create()
            progressDialog.show()
            val API = "https://mcc-fall-2019-g10.appspot.com/api/project/${project.projectID}/members"

            val que = Volley.newRequestQueue(this.context)
            val req = MyJsonObjectRequest(
                Request.Method.PUT, API, idsAdded,
                Response.Listener { response ->
                    println("RESPONSE: $response")
                    Log.e("ids added", idsAdded.toString())

                    usersAdded.forEachIndexed{index, i ->
                        val chip = Chip(pActivity)
                        chip.text = i
                        chip.setChipBackgroundColorResource(R.color.colorPrimaryLight)
                        chip.setTextColor(resources.getColor(android.R.color.white))
                        cgMembers.addView(chip)

                        val USER_API = "https://mcc-fall-2019-g10.appspot.com/api/user/"
                        val que2 = Volley.newRequestQueue(activity)
                        val req2 = JsonObjectRequest(
                            Request.Method.GET, USER_API + idsAdded[index], JSONObject(),
                            Response.Listener {response ->
                                notification(response["msgToken"] as String)
                            }, Response.ErrorListener {
                                println("MAN ERROR $it")
                            }
                        )
                        que2.add(req2)

                        project.members.add(idsAdded[index] as String)
                    }

                    tvNMembers.text = "${cgMembers.size} member(s)"

                    progressDialog.dismiss()
                    Snackbar.make(view!!, "${usersAdded.size} member(s) added", Snackbar.LENGTH_SHORT).show()
                    for (i in 0 until idsAdded.length())
                        pActivity.projectMembers[idsAdded[i] as String] = usersAdded[i]
                    usersAdded.clear()
                    idsAdded = JSONArray()
                }, Response.ErrorListener { error ->
                    println("Error: $error")
                }
            )
            que.add(req)
        } else{
            Snackbar.make(view!!, "No members added", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun updateMembers() {
        cgMembers.removeAllViews()
        for ((memberId, memberName) in pActivity.projectMembers) {
            val chip = Chip(pActivity)
            chip.text = memberName
            if (memberId == project.adminID)
                chip.setChipBackgroundColorResource(R.color.colorPrimary)
            else
                chip.setChipBackgroundColorResource(R.color.colorPrimaryLight)
            chip.setTextColor(resources.getColor(android.R.color.white))
            cgMembers.addView(chip)
        }
    }

    private fun notification(to: String) {
        /*if (!TextUtils.isEmpty(something)) {

        }*/

        val notification = JSONObject()
        val notificationBody = JSONObject()

        try {
            notificationBody.put("title", "You have been added to project \"${project.name}\"!")
            notificationBody.put("body", "See what is inside it!")
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
}