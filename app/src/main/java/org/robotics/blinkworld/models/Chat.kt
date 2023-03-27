package org.robotics.blinkworld.models

import com.google.firebase.database.ServerValue
import java.util.*

data class Chat(var username: String="", var lastMsg:String="", val id: String="",
                var type:String="", var photo: String?="",var uid:String="",var timeStamp: Long= 0,
                var messageList: List<Message>? = emptyList(),var read: Boolean=false, val text: String="")

