package com.cse4100g10.taskmanager

import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.size
import androidx.fragment.app.Fragment
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
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_project_info.view.*
import kotlinx.android.synthetic.main.recycleritem_project.view.iv_projectpic
import org.json.JSONArray
import org.json.JSONObject


/**
 * A placeholder fragment containing a simple view.
 */
class ProjectInfoFragment : Fragment() {

    private lateinit var pActivity: ProjectActivity
    private lateinit var project: Project
    private var listUsers: MutableList<String> = mutableListOf()
    private var usersAdded: MutableList<String> = mutableListOf()
    private var idsAdded: JSONArray = JSONArray()
    private var listIDs = ArrayList<String>()
    private lateinit var cgMembers: ChipGroup
    private lateinit var tvNMembers: TextView
    private var members = ArrayList<String>()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        pActivity = activity as ProjectActivity
        project = pActivity.project
        val root = inflater.inflate(R.layout.fragment_project_info, container, false)
        var tvDescription: TextView = root.tv_project_description
        tvDescription.text = project.description
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

        val view = inflater.inflate(R.layout.custom_dialog, null)
        val userEditText = view.findViewById(R.id.usersEditText) as AutoCompleteTextView

        val adapter = ArrayAdapter(
            this.context!!,
            android.R.layout.simple_list_item_1, listUsers)
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
                idsAdded.put(listIDs[usersAdded.indexOf(userEditText.text.toString())])
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
            tvNMembers.text = "$nMembers member(s)"

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
        for (i in 0 until project.members.size) {
            val USER_API = "https://mcc-fall-2019-g10.appspot.com/api/user/${project.members[i]}"

            val que = Volley.newRequestQueue(this.context)
            val req = JsonObjectRequest(
                Request.Method.GET, USER_API, JSONObject(),
                Response.Listener { response ->
                    members.add(response["name"] as String)
                    val chip = Chip(pActivity)
                    chip.text = response["name"] as String
                    chip.setChipBackgroundColorResource(R.color.colorPrimaryLight)
                    chip.setTextColor(resources.getColor(android.R.color.white))
                    cgMembers.addView(chip)
                    if(i == project.members.size -1){
                        getAllUsers(members)
                    }
                }, Response.ErrorListener { error ->
                    println("Error in some member CreateTaskActivity: $error")
                }
            )
            que.add(req)
        }


        return root
    }



    fun parseList(users: JSONArray, members: ArrayList<String>){
        for (i in 0 until users.length()) {
            val item:JSONObject = users.getJSONObject(i)
            //Log.e("members", members.toString())
            //Log.e("members_name", item["name"].toString())
            if(!members.contains(item["name"])) {
                listUsers.add(item["name"].toString())
                listIDs.add(item["id"].toString())
            }
        }
    }

    fun sendUsers(){
        if(usersAdded.isNotEmpty()) {
            val progressDialog: AlertDialog =
                MaterialAlertDialogBuilder(this.context).setView(R.layout.dialog_indeterminate_progress)
                    .create()
            progressDialog.show()
            val API =
                "https://mcc-fall-2019-g10.appspot.com/api/project/${project.projectID}/members"

            val que = Volley.newRequestQueue(this.context)
            val req = MyJsonObjectRequest(
                Request.Method.PUT, API, idsAdded,
                Response.Listener { response ->
                    progressDialog.dismiss()
                    println("RESPONSE: $response")
                    for (i in usersAdded) {
                        val chip = Chip(pActivity)
                        chip.text = i
                        chip.setChipBackgroundColorResource(R.color.colorPrimaryLight)
                        chip.setTextColor(resources.getColor(android.R.color.white))
                        cgMembers.addView(chip)
                    }
                    tvNMembers.text = "${cgMembers.size} member(s)"

                    usersAdded.clear()
                    idsAdded = JSONArray()
                }, Response.ErrorListener { error ->
                    println("Error: $error")
                }
            )
            que.add(req)
        } else{
            Snackbar.make(view!!, "Please add a user", Snackbar.LENGTH_SHORT).show()
        }
    }


    fun getAllUsers(members:ArrayList<String>){
        val API = "https://mcc-fall-2019-g10.appspot.com/api/user/all"
        val que = Volley.newRequestQueue(this.context)
        val req = JsonObjectRequest(
            Request.Method.GET, API, JSONObject(),
            Response.Listener { response ->
                parseList(response["all"] as JSONArray, members)
            }, Response.ErrorListener { error ->
                println("Error in retrieving users: $error")
            }
        )
        que.add(req)

    }

}