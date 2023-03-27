package org.robotics.blinkworld.models

import com.google.firebase.database.Exclude

data class User(
    var photo: String?="", var username:String = "",
    val bio: String="", val phone: String="", var uid:String="",
    val followers: Map<String,Boolean> = emptyMap(),
    var state:String ="", val longitude: Double=0.0, val latitude: Double= 0.0,
    val following: Map<String,Boolean> = emptyMap(),val friend: Map<String,Boolean> = emptyMap(),val Time: String ="")  {

}