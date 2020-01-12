package com.cse4100g10.taskmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cse4100g10.taskmanager.ui.main.TaskRecyclerAdapter


/**
 * A placeholder fragment containing a simple view.
 */
class ProjectTasksFragment : Fragment() {
    private lateinit var pActivity: ProjectActivity
    private lateinit var rvTasks: RecyclerView
    private lateinit var vLoading: View
    private lateinit var vEmpty: View
    private var finishedLoading : Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        pActivity = activity as ProjectActivity
        val root = inflater.inflate(R.layout.fragment_project_tasks, container, false)

        rvTasks = root.findViewById(R.id.rv_tasks)
        vLoading = root.findViewById(R.id.v_loading)
        vEmpty = root.findViewById(R.id.v_empty)
        vEmpty.findViewById<TextView>(R.id.tv_caption).text = "NO TASKS YET DUH"

        rvTasks.itemAnimator = DefaultItemAnimator()
        var layoutManager = LinearLayoutManager(activity)
        rvTasks.layoutManager = layoutManager

        //val dividerItemDecoration = DividerItemDecoration(rvTasks?.context, layoutManager.orientation)
        //rvTasks.addItemDecoration(dividerItemDecoration)
        rvTasks?.adapter = TaskRecyclerAdapter(pActivity.tasksList, pActivity, pActivity.project)
        updateLayout()
        return root
    }

    fun taskAdded(position: Int){
        var p = position
        if(position == -1) p = pActivity!!.filesList.size
        finishedLoading = true
        updateLayout()
        rvTasks.adapter?.notifyItemInserted(p)
    }

    fun taskRemoved(position: Int){
        rvTasks.adapter?.notifyItemRemoved(position)
        updateLayout()
    }

    fun dataChanged(){
        finishedLoading = true
        updateLayout()
        rvTasks.adapter?.notifyDataSetChanged()
    }

    fun showLoading(){
        finishedLoading = false
        updateLayout()
    }

    private fun updateLayout(){
        if(finishedLoading) {
            vLoading.visibility = View.GONE
            if (pActivity!!.tasksList.size > 0) {
                vEmpty.visibility = View.GONE
                rvTasks.visibility = View.VISIBLE
            } else {
                vEmpty.visibility = View.VISIBLE
                rvTasks.visibility = View.GONE
            }
        }else{
            rvTasks.visibility = View.GONE
            vEmpty.visibility = View.GONE
            vLoading.visibility = View.VISIBLE
        }
    }
}