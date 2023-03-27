package org.robotics.blinkworld.Utils

import org.robotics.blinkworld.models.User


private lateinit var mUser: User
enum class StateUser(val state:String) {


    ONLINE("online"),
    OFFLINE("offline");
    companion object{

        fun updateState(appStates: StateUser){

            database.child(NODE_USERS).child(currentUid()!!).addListenerForSingleValueEvent(
                Utils.ValueEventListenerAdapter {
                    mUser = it.getValue(User::class.java)!!


                })



            database.child(NODE_USERS).child(currentUid()!!).child("state")
                .setValue(appStates.state ).addOnSuccessListener {
                    mUser.state = appStates.state }

        }
    }
}


