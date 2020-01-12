package com.cse4100g10.taskmanager.domain

import android.net.Uri

class User {
    var name:String? = null
    var photoUrl: Uri? = null
    var uid: String? = null
    var email: String? = null

    constructor(name:String, uid: String, photoUrl:Uri, email:String) {
        this.name = name
        this.uid = uid
        this.email = email
        this.photoUrl = photoUrl
    }


    constructor(name:String, uid: String, email:String) {
        this.name = name
        this.uid = uid
        this.email = email
    }

}
