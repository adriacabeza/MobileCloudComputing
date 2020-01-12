package com.cse4100g10.taskmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.domain.Project
import com.cse4100g10.taskmanager.utils.inflate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recycleritem_project.view.*
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class RecyclerAdapter(private var projects: ArrayList<Project>, private var activity: ProjectListActivity?) : RecyclerView.Adapter<ProjectHolder>() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectHolder {
        val inflatedView = parent.inflate(R.layout.recycleritem_project, false)
        return ProjectHolder(inflatedView, this, activity)
    }

    override fun getItemCount() =  projects.size


    fun removeItem(position: Int) {
        projects.removeAt(position)
        notifyItemRemoved(position)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ProjectHolder, position: Int) {
        val itemPhoto = projects[position]
        holder.bindProject(itemPhoto, projects)
    }

}

@RequiresApi(Build.VERSION_CODES.O)
class ProjectHolder(v: View, adapter: RecyclerAdapter, activity: ProjectListActivity?) : RecyclerView.ViewHolder(v), View.OnClickListener {
    private var view: View = v
    private var project: Project? = null
    private var projects: ArrayList<Project>? = null
    private lateinit var auth: FirebaseAuth
    private var selected: Boolean? = false
    private val API:String = "https://mcc-fall-2019-g10.appspot.com/api/user/"
    var popup: PopupMenu? = null
    init {
        auth = FirebaseAuth.getInstance()
        v.setOnClickListener(this)
        view.bt_star.setOnClickListener { view ->
            //auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val jsonobj = JSONObject()
            val que = Volley.newRequestQueue(v.context)
            if (!selected!!) {
                //send request to put it as favourite
                //Log.d("sending to",API+user!!.uid+"/favorites/"+ project!!.projectID)
                view.bt_star.setIconResource(R.drawable.ic_star_24dp)
                val req = JsonObjectRequest(
                    Request.Method.PUT,API+user!!.uid+"/favorites/"+ project!!.projectID,jsonobj,
                    Response.Listener {
                            response ->
                        println(response.toString())
                    }, Response.ErrorListener {
                        println("Error")
                    }
                )
                que.add(req)

                activity!!.addProjectFavourites(project!!)

                selected = true
            } else {
                //Log.d("sending to",API+user!!.uid+"/favorites/"+(project!!.projectID as String))
                //send request to delete it as favourite
                view.bt_star.setIconResource(R.drawable.ic_star_border_24dp)
                val req = JsonObjectRequest(
                    Request.Method.DELETE,API+user!!.uid+"/favorites/"+(project!!.projectID as String),jsonobj,
                    Response.Listener {
                            response ->
                        println(response.toString())
                    }, Response.ErrorListener {
                        println("Error")
                    }
                )
                que.add(req)
                activity!!.removeProjectFavourites(project!!)
                selected = false
            }
        }
        if(activity != null) {
            val btOverflow : View = view.bt_overflow as View
            btOverflow.visibility = View.VISIBLE
            popup = PopupMenu(itemView.context, btOverflow)
            popup?.inflate(R.menu.menu_project_card)

            popup?.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.delete_project -> {
                        MaterialAlertDialogBuilder(itemView.context)
                            .setMessage("Are you sure that you want to delete this project?")
                            .setPositiveButton("Delete") { _, _ ->
                                activity.deleteProject(project)
                            }
                            .show()
                    }
                    R.id.download_report -> {
                        MaterialAlertDialogBuilder(itemView.context)
                            .setMessage("Do you want to download a report of the project?")
                            .setPositiveButton("Yes") { _, _ ->
                                activity.downloadProject(project)
                            }
                            .show()

                    }
                }
                return@setOnMenuItemClickListener true
            }
            v.bt_overflow.setOnClickListener { btn -> popup?.show() }
        }
    }

    override fun onClick(v: View) {
        val context = itemView.context
        val showProjectIntent = Intent(context, ProjectActivity::class.java)
        showProjectIntent.putExtra(PROJECT_KEY, project)
        context.startActivity(showProjectIntent)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    fun bindProject(project: Project, projects:ArrayList<Project>) {
        this.project = project

        popup?.menu?.findItem(R.id.delete_project)?.isVisible = auth.uid == project?.adminID


        this.selected = project.isFavourite
        //Log.d("project",project.name)
        //Log.d("project_selected",project.isFavourite.toString())
        if(this.selected!!) {
            view.bt_star.setIconResource(R.drawable.ic_star_24dp)
        }
        else{
            view.bt_star.setIconResource(R.drawable.ic_star_border_24dp)
        }
        this.projects = projects
        view.itemTitle.text = project.name
        // view.itemDescription.text = project.description

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
        val currentItemDate = LocalDateTime.parse(project.updated, formatter)
        val today: LocalDateTime = LocalDateTime.now()
        val yesterday: LocalDateTime = LocalDateTime.now().minusDays(1)


        val str = if (today.dayOfMonth == currentItemDate.dayOfMonth && today.month == currentItemDate.month && today.year == currentItemDate.year) {
            "Today"
        } else if (yesterday.dayOfMonth == currentItemDate.dayOfMonth && yesterday.month == currentItemDate.month && yesterday.year == currentItemDate.year) {
            "Yesterday"
        } else {
            val day = if (currentItemDate.dayOfMonth < 10) {
                "0" + currentItemDate.dayOfMonth.toString()
            } else {
                currentItemDate.dayOfMonth.toString()
            }

            var month = currentItemDate.month.toString().toLowerCase()
            month = month[0].toUpperCase() + month.drop(1)

            "$month $day"
        }

        view.itemLastModification.text = "Last modified: $str"

        /*if (project.photoUrl == null) {
            //Log.e("YES", "NULL => ${project.name} => ${project.photoUrl}")
        } else {
            //Log.e("YES", "NOPE => ${project.name} => ${project.photoUrl}")
        }*/

        /*if(!project.photoUrl.isNullOrEmpty()) {
            Picasso.get().load(project.photoUrl).into(view.iv_projectpic)
        }*/

        try{
            Picasso.get().load(project.photoUrl).into(view.iv_projectpic)
        } catch (e: Exception) {
            //Log.e("YES", "PICASSO ERROR RECYCLER ADAPTER: $e")
        }

        if (project.hasAttachments!!) {
            val bt_hasmedia : View = view.bt_hasmedia as View
            bt_hasmedia.visibility = View.VISIBLE
        } else {
            val bt_hasmedia : View = view.bt_hasmedia as View
            bt_hasmedia.visibility = View.GONE
        }
    }


    companion object {
        val PROJECT_KEY = "PROJECT"
    }
}
