package com.example.firebasekotlin

import com.google.firebase.database.Exclude

data class Upload(var mName: String, var mImageUrl: String) {


    @Exclude
    var mKey: String = "Default String"

        @Exclude
        set(value) {
            field = value
        }
        @Exclude
        get() = field


    constructor() : this("", "")

    init {
        if (mName.trim() == "") {

            mName = "No Name"
        }
    }
}

