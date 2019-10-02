package com.adriacabeza.exercise03

import android.app.Activity
import android.content.Context
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class ListViewAdapter(private val activity: Activity, photoList: ArrayList<PhotoList>) : BaseAdapter() {

    private var photoList = ArrayList<PhotoList>()

    init {
        this.photoList = photoList
    }

    override fun getCount(): Int {
        return photoList.size
    }

    override fun getItem(i: Int): PhotoList {
        return photoList[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }


    override fun getView(i: Int, convertView: View?, viewGroup: ViewGroup): View {
        var vi: View? = convertView
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        vi = inflater.inflate(R.layout.list_item, viewGroup,false)
        val image = vi.findViewById(R.id.showImage) as ImageView
        Picasso.get().load(photoList[i].url).centerCrop().resize(240,240).into(image)
        val author = vi.findViewById(R.id.showAuthor) as TextView
        author.text = photoList[i].author



        return vi
    }
}