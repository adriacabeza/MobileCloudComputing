package com.cse4100g10.taskmanager.ui.main

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.ProjectActivity
import com.cse4100g10.taskmanager.R
import com.cse4100g10.taskmanager.domain.Header
import com.cse4100g10.taskmanager.domain.ListItem
import com.cse4100g10.taskmanager.domain.Picture
import com.cse4100g10.taskmanager.domain.Project
import com.cse4100g10.taskmanager.utils.InputStreamVolleyRequest
import com.cse4100g10.taskmanager.utils.inflate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recycleritem_picture.view.*
import java.io.File
import java.io.FileOutputStream

class PictureRecyclerAdapter(private var items: MutableList<ListItem>, private var project: Project, private var projectActivity: ProjectActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ListItem.TYPE_HEADER) {
            val itemView = parent.inflate(R.layout.recycleritem_header, false)
            HeaderHolder(itemView)
        } else {
            val itemView = parent.inflate(R.layout.recycleritem_picture, false)
            PictureHolder(itemView, project, projectActivity)
        }
    }

    override fun getItemCount() =  items.size


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = items[position].type
        if (type == ListItem.TYPE_HEADER) {
            val header = items[position] as Header
            (holder as HeaderHolder).bindTask(header)
        } else {
            val picture = items[position] as Picture
            (holder as PictureHolder).bindTask(picture)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }
}
/*
private fun saveImage(context: Context, finalBitmap: Bitmap?) : Uri{
    var uriOut : Uri = getTempProviderFile(context)
    var file : File = File(uriOut.toString())
    try {
        val out = FileOutputStream(file)
        finalBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
        return uriOut
    } catch (e: Exception) {
        //Log.e("ehhh","lll")
        for (x in e.stackTrace) //Log.e("ehhh",x.toString())

        e.printStackTrace()
    }
    return Uri.EMPTY
}*/

class PictureHolder(val view : View, val project: Project, val activity: ProjectActivity) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
    private var PROJECT_API:String = "https://mcc-fall-2019-g10.appspot.com/api/project/"
    private lateinit var picture: Picture
    private var fullLocalImageUri: Uri = Uri.EMPTY


    init {
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)
    }

    fun bindTask(picture : Picture) {
        this.picture = picture
        updateTaskView()
    }



    override fun onClick(v: View) {
        val uri : Uri = picture.URI
        val context = itemView.context
        var progressDialog : AlertDialog = MaterialAlertDialogBuilder(context).setView(R.layout.dialog_indeterminate_progress).create()
        progressDialog.show()
        val que = Volley.newRequestQueue(context.applicationContext)
        val req = InputStreamVolleyRequest(
            Request.Method.GET,
            uri,
            Response.Listener{
                try {
                    val path = context.externalCacheDir
                    val newFile = File(path, File(uri.path).name)
                    var outputStream: FileOutputStream
                    outputStream = FileOutputStream(newFile)
                    outputStream.write(it)
                    outputStream.close()
                    progressDialog.dismiss()
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    var uri: Uri = FileProvider.getUriForFile(context, context.applicationContext.packageName+".fileprovider", newFile)
                    //var uri: Uri = getTempProviderFile(context, newFile.path)
                    intent.setDataAndType(uri, "image/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener {
                println("Error")
                print(it)
            }
        , null)
        que.add(req)
    }

    override fun onLongClick(p0: View?): Boolean {
        MaterialAlertDialogBuilder(itemView.context)
            .setMessage("Are you sure that you want to delete this picture?")
            .setPositiveButton("Delete") { _, _ ->
                activity.deletePicture(picture)
            }
            .show()
        return true
    }




    private fun updateTaskView(){
        //Log.e("puta", picture!!.URI.toString())
        Picasso.get()
            .load(picture!!.URI)
            .resize(256, 256)
            .centerCrop()
            .into(view.iv_picture)
    }
}