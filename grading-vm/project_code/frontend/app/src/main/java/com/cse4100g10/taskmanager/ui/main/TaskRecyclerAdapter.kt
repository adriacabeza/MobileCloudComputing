package com.cse4100g10.taskmanager.ui.main

import android.app.Activity
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.R
import com.cse4100g10.taskmanager.domain.Project
import com.cse4100g10.taskmanager.domain.Task
import com.cse4100g10.taskmanager.utils.inflate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.recycleritem_task.view.*
import org.json.JSONObject


class TaskRecyclerAdapter(private var tasks: MutableList<Task>, private var activity: Activity, private var project: Project) : RecyclerView.Adapter<TaskHolder>()  {
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

class TaskHolder(val view: View, activity: Activity, project: Project) : RecyclerView.ViewHolder(view) {
    private lateinit var task: Task

    init {
        view.setOnClickListener {
            //view.cb_completed.toggle()
            MaterialAlertDialogBuilder(activity).setTitle(task.name).setMessage(task.description).create().show()
        }

        view.cb_completed.setOnCheckedChangeListener { _, isChecked: Boolean ->
            val TASK_API = "https://mcc-fall-2019-g10.appspot.com/api/project/${project.projectID}/tasks/${task.taskID}"

            task.completed = isChecked

            val jsonobj = JSONObject()
            if (isChecked) jsonobj.put("status", "completed")
            else {
                if (project.projectType.displayName == "Personal" || task.assignedTo.length() == 1) jsonobj.put("status", "ongoing")
                else jsonobj.put("status", "pending")
            }

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
}