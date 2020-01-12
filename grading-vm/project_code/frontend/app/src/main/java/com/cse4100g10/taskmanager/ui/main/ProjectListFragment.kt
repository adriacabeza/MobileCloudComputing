package com.cse4100g10.taskmanager.ui.main

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cse4100g10.taskmanager.ProjectListActivity
import com.cse4100g10.taskmanager.R
import com.cse4100g10.taskmanager.RecyclerAdapter
import com.cse4100g10.taskmanager.domain.Project
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * A placeholder fragment containing a simple view.
 */
class ProjectListFragment : Fragment() {
    private var pActivity: ProjectListActivity? = null
    private var projectList: ArrayList<Project> = arrayListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var vLoading: View
    private lateinit var vEmpty: View
    private var finishedLoading : Boolean = false

    override fun onResume() {
        super.onResume()
        projectList.clear()
        recyclerView.adapter!!.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        try {
            pActivity = activity as ProjectListActivity
        }catch (e: Exception){
        }

        val root = inflater.inflate(R.layout.fragment_project_list, container, false)

        vLoading = root.findViewById(R.id.v_loading)
        vEmpty = root.findViewById(R.id.v_empty)
        vEmpty.findViewById<TextView>(R.id.tv_caption).text = "NO PROJECTS HERE :("


        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.itemAnimator = DefaultItemAnimator()
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = RecyclerAdapter(projectList, pActivity)
        updateLayout()
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun projectAdded(position: Int, project: Project, sorting: Boolean){
        finishedLoading = true
        if(!projectList.any{it -> it.projectID == project.projectID.toString()}) {
            if (!sorting) {
                var p = position
                if(position == -1) p = projectList.size
                projectList.add(p, project)
                recyclerView.adapter?.notifyItemInserted(p)
            }
            else{
                val formatter =     DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                val projectDate = LocalDateTime.parse(project.deadlineDate.toString() +"T"+ project.deadlineTime.toString() + ":00.000Z", formatter)
                if(projectList.size == 0){
                    projectList.add(project)
                    recyclerView.adapter?.notifyItemInserted(0)
                }
                else {
                    for (i in 0 until projectList.size) {
                        val aux = LocalDateTime.parse(
                            projectList[i].deadlineDate.toString() + "T" + projectList[i].deadlineTime.toString() + ":00.000Z",
                            formatter
                        )
                        if (aux.isAfter(projectDate)) {
                            ////Log.d("adding", projectDate.toString())
                            projectList.add(i, project)
                            recyclerView.adapter?.notifyItemInserted(i)
                            break
                        }
                        if (i == projectList.size - 1) {
                            projectList.add(projectList.size, project)
                            recyclerView.adapter?.notifyItemInserted(projectList.size)
                        }
                    }
                }
            }
        }
        updateLayout()
    }

    fun projectRemoved(project: Project?){
        if (projectList.contains(project)){
            projectList.indexOf(project).let {
                projectList.removeAt(it)
                recyclerView.adapter?.notifyItemRemoved(it)
                updateLayout()
            }
        }
    }

    fun dataChanged(){
        finishedLoading = true
        updateLayout()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun updateLayout(){
        if(finishedLoading) {
            vLoading.visibility = View.GONE
            if (projectList.size > 0) {
                vEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            } else {
                vEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        }
    }
}