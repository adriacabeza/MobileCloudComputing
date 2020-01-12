package com.cse4100g10.taskmanager

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
import com.cse4100g10.taskmanager.ui.main.FileRecyclerAdapter


/**
 * A placeholder fragment containing a simple view.
 */
class ProjectFilesFragment : Fragment() {

    private var pActivity: ProjectActivity? = null
    private lateinit var rvFiles: RecyclerView
    private lateinit var vLoading: View
    private lateinit var vEmpty: View
    private var finishedLoading : Boolean = false


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        pActivity = activity as ProjectActivity
        val root = inflater.inflate(R.layout.fragment_project_files, container, false)

        vLoading = root.findViewById(R.id.v_loading)
        vEmpty = root.findViewById(R.id.v_empty)
        vEmpty.findViewById<TextView>(R.id.tv_caption).text = "NO FILES YET DUH"

        rvFiles = root.findViewById(R.id.projectFilesRecyclerView)
        rvFiles.itemAnimator = DefaultItemAnimator()
        var layoutManager = LinearLayoutManager(activity)
        rvFiles.layoutManager = layoutManager
        rvFiles.adapter = FileRecyclerAdapter(pActivity!!.filesList, pActivity!!.project, pActivity!!)
        updateLayout()
        return root
    }

    fun fileAdded(position: Int){
        var p = position
        if(position == -1) p = pActivity!!.filesList.size
        finishedLoading = true
        updateLayout()
        rvFiles.adapter?.notifyItemInserted(p)
    }

    fun dataChanged(){
        finishedLoading = true
        updateLayout()
        rvFiles.adapter?.notifyDataSetChanged()
    }

    fun showLoading(){
        finishedLoading = false
        updateLayout()
    }

    private fun updateLayout(){
        if(finishedLoading) {
            vLoading.visibility = View.GONE
            if (pActivity!!.filesList.size > 0) {
                vEmpty.visibility = View.GONE
                rvFiles.visibility = View.VISIBLE
            } else {
                vEmpty.visibility = View.VISIBLE
                rvFiles.visibility = View.GONE
            }
        }else{
            rvFiles.visibility = View.GONE
            vEmpty.visibility = View.GONE
            vLoading.visibility = View.VISIBLE
        }
    }

    fun fileRemoved(position: Int){
        rvFiles.adapter?.notifyItemRemoved(position)
        updateLayout()
    }
}