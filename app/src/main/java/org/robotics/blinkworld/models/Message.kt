package org.robotics.blinkworld.models

import java.util.*

data class Message(val id: String = "", val text: String="", val type: String="", val uid: String="",
                   val userPhoto:String?="", var read: Boolean = false, var postId: String = "",
                   var imagePosts: String = "", var authorPost: String = "",var timeStamp: Long = 0,val photo: String?="",
                   val postAuthorUid: String?="",val typeImage:Int ?= 0)
{


    override fun equals(other: Any?): Boolean {
        return (other as Message).id == id

    }

    fun fileStemp(): Long = timeStamp as Long
}