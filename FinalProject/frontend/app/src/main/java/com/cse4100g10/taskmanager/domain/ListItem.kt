package com.cse4100g10.taskmanager.domain

abstract class ListItem {

    abstract val type: Int

    companion object {

        val TYPE_HEADER = 0
        val TYPE_FILE = 1
        val TYPE_PICTURE = 2
    }
}