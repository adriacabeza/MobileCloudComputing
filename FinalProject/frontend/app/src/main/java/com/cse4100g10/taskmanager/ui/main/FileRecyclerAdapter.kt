package com.cse4100g10.taskmanager.ui.main

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.cse4100g10.taskmanager.ProjectActivity
import com.cse4100g10.taskmanager.R
import com.cse4100g10.taskmanager.domain.File_
import com.cse4100g10.taskmanager.domain.Header
import com.cse4100g10.taskmanager.domain.ListItem
import com.cse4100g10.taskmanager.domain.Project
import com.cse4100g10.taskmanager.utils.InputStreamVolleyRequest
import com.cse4100g10.taskmanager.utils.inflate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.recycleritem_file.view.*
import kotlinx.android.synthetic.main.recycleritem_header.view.*
import java.io.File
import java.io.FileOutputStream


class FileRecyclerAdapter(private var items: MutableList<ListItem>, private var project: Project, private var projectActivity: ProjectActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ListItem.TYPE_HEADER) {
            val itemView = parent.inflate(R.layout.recycleritem_header, false)
            HeaderHolder(itemView)
        } else {
            val itemView = parent.inflate(R.layout.recycleritem_file, false)
            FileHolder(itemView, project, projectActivity)
        }
    }

    override fun getItemCount() =  items.size


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = items[position].type
        if (type == ListItem.TYPE_HEADER) {
            val header = items[position] as Header
            (holder as HeaderHolder).bindTask(header)
            // your logic here
        } else {
            val file = items[position] as File_
            (holder as FileHolder).bindTask(file)
            // your logic here


        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }
}

class FileHolder(val view : View, val project: Project, val activity: ProjectActivity) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
    private var PROJECT_API:String = "https://mcc-fall-2019-g10.appspot.com/api/project/"
    private var file: File_? = null

    override fun onClick(p0: View?) {
            val uri : Uri = Uri.parse(file!!.url)
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
                        val outputStream: FileOutputStream
                        outputStream = FileOutputStream(newFile)
                        outputStream.write(it)
                        outputStream.close()
                        progressDialog.dismiss()
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        var uri: Uri = FileProvider.getUriForFile(context, context.applicationContext.packageName+".fileprovider", newFile)
                        //val uri: Uri = getTempProviderFile(context, newFile.path)
                        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(newFile.extension)
                        intent.setDataAndType(uri, mimeType
                        )
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


    fun bindTask(file : File_) {
        this.file = file
        updateTaskView()
    }

    private fun updateTaskView(){
        view.tv_documentName.text = file!!.name
        when (file!!.documentType) {
            "pdf" -> view.iv_documentType.setBackgroundResource(R.drawable.ic_document_type_pdf)
            "doc" -> view.iv_documentType.setBackgroundResource(R.drawable.ic_document_type_doc)
            "sheet" -> view.iv_documentType.setBackgroundResource(R.drawable.ic_document_type_sheet)
            "drawing" -> view.iv_documentType.setBackgroundResource(R.drawable.ic_document_type_drawing)
            "audio" -> view.iv_documentType.setBackgroundResource(R.drawable.ic_document_type_audio)
            "video" -> view.iv_documentType.setBackgroundResource(R.drawable.ic_document_type_video)
            else -> view.iv_documentType.setBackgroundResource(R.drawable.ic_document_type_not_found)
        }
    }



    init {
        view.setOnLongClickListener(this)
        view.setOnClickListener(this)
    }

    override fun onLongClick(p0: View?): Boolean {
        MaterialAlertDialogBuilder(itemView.context)
            .setMessage("Are you sure that you want to delete this file?")
            .setPositiveButton("Delete") { _, _ ->
                activity.deleteFile(file)
            }
            .show()
        return true
    }


}

class HeaderHolder(val view : View) : RecyclerView.ViewHolder(view) {
    private var header: Header? = null

    fun bindTask(header : Header) {
        this.header = header
        updateTaskView()
    }

    private fun updateTaskView(){
        view.headerTextView.text = header!!.name
    }
}