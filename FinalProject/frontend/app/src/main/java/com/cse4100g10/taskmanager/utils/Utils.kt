package com.cse4100g10.taskmanager.utils

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.FileProvider
import org.json.JSONArray
import java.io.File

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun getTempFile(context: Context): Uri {
    return Uri.fromFile(File(context.externalCacheDir, getTempFileName("jpg")))
}

fun getTempProviderFile(context: Context, filename: String): Uri {
    val path = context.externalCacheDir
    //val newFile = File(path, getTempFileName(extension))
    val newFile = File(path, filename)
    return FileProvider.getUriForFile(context, context.applicationContext.packageName+".fileprovider", newFile)
}



fun getTempFileName(extension : String) : String{
    return System.currentTimeMillis().toString() + "." + extension
}

fun JSONArray.toMutableList(): MutableList<String> = MutableList(length(), this::get) as MutableList<String>
