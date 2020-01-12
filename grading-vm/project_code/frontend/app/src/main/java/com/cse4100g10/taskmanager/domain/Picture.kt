package com.cse4100g10.taskmanager.domain

import android.net.Uri

class Picture() : ListItem() {
    var URI: Uri = Uri.EMPTY
    var id: String = ""
    var name: String = ""

    override val type: Int
        get() = TYPE_PICTURE

    constructor(stringUri: String, id: String, name: String) : this() {
        this.URI = Uri.parse(stringUri)
        this.id = id
        this.name = name
    }
}