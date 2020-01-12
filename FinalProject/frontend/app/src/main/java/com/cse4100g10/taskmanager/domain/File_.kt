package com.cse4100g10.taskmanager.domain

class File_() : ListItem() {
    var name: String = ""
    var documentType: String = ""
    var id: String = ""
    var url: String = ""

    override val type: Int
        get() = TYPE_FILE

    constructor(name: String, documentType: String, id: String, url: String) : this() {
        this.name = name
        this.documentType = documentType
        this.id = id
        this.url = url
    }
}