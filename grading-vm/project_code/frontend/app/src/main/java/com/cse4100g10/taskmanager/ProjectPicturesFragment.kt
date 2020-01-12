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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cse4100g10.taskmanager.domain.ListItem.Companion.TYPE_HEADER
import com.cse4100g10.taskmanager.ui.main.PictureRecyclerAdapter

class ProjectPicturesFragment : Fragment() {

    private var pActivity: ProjectActivity? = null
    private lateinit var rvPictures: RecyclerView
    private lateinit var vLoading: View
    private lateinit var vEmpty: View
    private var finishedLoading : Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        pActivity = activity as ProjectActivity
        val root = inflater.inflate(R.layout.fragment_project_pictures, container, false)

        vLoading = root.findViewById(R.id.v_loading)
        vEmpty = root.findViewById(R.id.v_empty)
        vEmpty.findViewById<TextView>(R.id.tv_caption).text = "NO PICTURES YET DUH"

        rvPictures = root.findViewById(R.id.projectPicturesRecyclerView)
        rvPictures.itemAnimator = DefaultItemAnimator()
        rvPictures.adapter = PictureRecyclerAdapter(pActivity!!.picturesList, pActivity!!.project, pActivity!!)

        val imagesPerLine = 3

        val onSpanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (rvPictures?.adapter?.getItemViewType(position) == TYPE_HEADER) imagesPerLine else 1
            }
        }

        var layoutManager = GridLayoutManager(activity, imagesPerLine)
        layoutManager.spanSizeLookup = onSpanSizeLookup
        rvPictures?.layoutManager = layoutManager
        updateLayout()
        return root
    }

    fun pictureAdded(position: Int){
        var p = position
        if(position == -1) p = pActivity!!.picturesList.size
        finishedLoading = true
        updateLayout()
        rvPictures.adapter?.notifyItemInserted(p)
    }

    fun dataChanged(){
        finishedLoading = true
        updateLayout()
        rvPictures.adapter?.notifyDataSetChanged()
    }

    fun showLoading(){
        finishedLoading = false
        updateLayout()
    }

    private fun updateLayout(){
        if(finishedLoading) {
            vLoading.visibility = View.GONE
            if (pActivity!!.picturesList.size > 0) {
                vEmpty.visibility = View.GONE
                rvPictures.visibility = View.VISIBLE
            } else {
                vEmpty.visibility = View.VISIBLE
                rvPictures.visibility = View.GONE
            }
        }else{
            rvPictures.visibility = View.GONE
            vEmpty.visibility = View.GONE
            vLoading.visibility = View.VISIBLE
        }
    }

    fun pictureRemoved(position: Int){
        rvPictures.adapter?.notifyItemRemoved(position)
        updateLayout()
    }
}