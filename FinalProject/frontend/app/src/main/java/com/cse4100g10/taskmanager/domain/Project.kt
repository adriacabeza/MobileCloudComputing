package com.cse4100g10.taskmanager.domain

import java.io.Serializable

enum class ProjectType(val displayName: String) {
    PERSONAL("Personal"),
    GROUP("Group");

    companion object{
        fun getFromString(name: String) = valueOf(name.toUpperCase())
    }

}

class Project : Serializable{
    var name:String = ""
    var description: String = ""
    var photoUrl: String? = null
    var projectType:ProjectType = ProjectType.PERSONAL
    var deadlineDate: String? = null
    var projectID: String? = null
    var deadlineTime: String? = null
    var keywords: MutableList<String> = mutableListOf()
    var hasAttachments: Boolean? = null
        set(value) {
            field = value
        }
    var isFavourite: Boolean? = null
    var updated: String? = null
    var adminID: String? = null
    var members: MutableList<String> = mutableListOf()


    constructor(name:String, description: String, photoUrl:String?, projectID: String, hasAttachments: Boolean, updated: String, adminID: String, members: MutableList<String>) {
        this.name = name
        this.description = description
        this.projectID = projectID
        this.photoUrl = photoUrl
        this.hasAttachments = hasAttachments
        this.updated = updated
        this.adminID = adminID
        this.members = members
    }

    constructor(name:String, description: String, photoUrl:String?, projectType:ProjectType, deadlineDate:String, deadlineTime:String, keywords:MutableList<String>, projectID: String, hasAttachments: Boolean, isFavourite:Boolean, updated: String, adminID: String, members: MutableList<String>) {
        this.name = name
        this.description = description
        this.projectID = projectID
        this.photoUrl = photoUrl
        this.projectType = projectType
        this.deadlineDate = deadlineDate
        this.deadlineTime = deadlineTime
        this.keywords = keywords
        this.hasAttachments = hasAttachments
        this.isFavourite = isFavourite
        this.updated = updated
        this.adminID = adminID
        this.members = members
    }

    fun hasDeadline(): Boolean = !deadlineDate.isNullOrEmpty() && !deadlineTime.isNullOrEmpty()
}