package com.cse4100g10.taskmanager.domain

class Task(val taskID: String, val name: String, val description: String, var completed:Boolean, var assignedTo: MutableList<String>, var status: String, var deadlineDate: String, var deadlineTime: String) {
    fun toggleCompleted(){
        this.completed = !this.completed
    }
}