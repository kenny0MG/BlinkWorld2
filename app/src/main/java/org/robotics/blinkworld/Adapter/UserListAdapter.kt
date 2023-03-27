package org.robotics.blinkworld.Adapter

import android.graphics.drawable.AnimationDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.friend_item.view.*
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.NODE_USERS
import org.robotics.blinkworld.Utils.currentUid
import org.robotics.blinkworld.Utils.database
import org.robotics.blinkworld.Utils.loadUserPhoto
import org.robotics.blinkworld.models.User


class UserListAdapter(private val listener: Listener) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {
        private var dataSet =  listOf<User>()!!
        private var mPositions = mapOf<String,Int>()!!
        private var mFollows = mapOf<String, Boolean>()!!



    interface Listener {
        fun follow(uid:String)
        fun unfollow(uid:String)
        fun click(uid:String)
    }

        fun updateDataSet(users: List<User>,follows:Map<String,Boolean>) {

            val diffResult = DiffUtil.calculateDiff(DiffCallback(dataSet, users))
            dataSet = users
            mPositions = users.withIndex().map { (idx, user) -> user.uid to idx }.toMap()
            mFollows = follows
            diffResult.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): UserListAdapter.UserViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.friend_item, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserListAdapter.UserViewHolder, position: Int) {
            with(holder) {
                photo_friend!!.loadUserPhoto(dataSet[position].photo)
                username_friend.text = dataSet[position].username

                //подписка - отписка


                if (dataSet[position].uid != currentUid()!!) {
                   database.child(NODE_USERS).child(
                        currentUid()!!).child("friends")
                        .child(dataSet[position].uid).get()
                        .addOnSuccessListener {
                            if(it.exists()){
                                add_button.visibility = View.GONE
                                your_friend.visibility = View.VISIBLE
                                your_friend.text = "Your friend"
                                its_you.visibility = View.GONE
                            }else{
                                org.robotics.blinkworld.Utils.database.child(org.robotics.blinkworld.Utils.NODE_USERS).child(
                                    currentUid()!!).child("following")
                                    .child(dataSet[position].uid).get()
                                    .addOnSuccessListener {
                                        if (it.exists()) {
                                            add_button.visibility = android.view.View.GONE
                                            your_friend.visibility = android.view.View.VISIBLE
                                            your_friend.text = "You follow"
                                            its_you.visibility = android.view.View.GONE


                                        } else {
                                            add_button.visibility = android.view.View.VISIBLE
                                            your_friend.visibility = android.view.View.GONE
                                            its_you.visibility = android.view.View.GONE


                                        }
                                    }
                            }


                        }

                } else {
                    its_you.visibility = android.view.View.VISIBLE
                    add_button.visibility = android.view.View.GONE
                    your_friend.visibility = android.view.View.GONE
                }


                //val followers = mFollows[dataSet[position].uid] ?: false
                add_button.setOnClickListener { listener.follow(dataSet[position].uid) }



                itemView.setOnClickListener {
                    listener.click(dataSet[position].uid)
                }

                //что бы закруглить углы картинки
                photo_friend.clipToOutline = true


                //перелевание кнопок рандом что бы кнопки не переливались одновременно
                val first_duration = (5000 until 15000).random()
                val second_duration = (5000 until 15000).random()
                val animationDrawable = add_button.background as AnimationDrawable
                animationDrawable.apply {
                    setEnterFadeDuration(first_duration)
                    setExitFadeDuration(second_duration)
                    start()
                }


            }
        }

         class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val photo_friend: ImageView? = view.photo_friend_item
                val username_friend:TextView = view.username_friend_item
                val add_button: AppCompatButton = view.add_friend_button
                val your_friend: TextView = view.your_friend_text_view
                val its_you:TextView = view.its_you_text_view


        }


    //функции подписки и отписки
    fun followed(uid: String) {
        mFollows+=(uid to true)
        notifyItemChanged(mPositions[uid]!!)

    }

    fun unfollowed(uid: String) {
        mFollows += (uid to false)
        notifyItemChanged(mPositions[uid]!!)
    }

        override fun getItemCount(): Int = dataSet.size

        class DiffCallback(
            private val oldDataSet: List<User>,
            private val newDataSet: List<User>,
        ) : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldDataSet.size
            override fun getNewListSize(): Int = newDataSet.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldDataSet[oldItemPosition].uid == newDataSet[newItemPosition].uid

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldDataSet[oldItemPosition] == newDataSet[newItemPosition]
        }
}