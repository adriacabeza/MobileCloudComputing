package com.cse4100g10.taskmanager.domain

import org.json.JSONArray

class Task(val taskID: String, val name: String, val description: String, var completed:Boolean, var assignedTo: JSONArray) {
    fun toggleCompleted(){
        this.completed = !this.completed
    }

}