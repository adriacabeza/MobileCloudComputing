package com.cse4100g10.taskmanager.domain

class Header(var name: String) : ListItem() {
    override val type: Int
        get() = TYPE_HEADER
}