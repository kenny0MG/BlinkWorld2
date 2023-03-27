package org.robotics.blinkworld.BottomFragmaent.OtherUserProfile.Fragment

import android.content.Intent
import android.graphics.Point
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.FirebaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.robotics.blinkworld.Activity.Messages.MessagesActivity
import org.robotics.blinkworld.Adapter.UserListAdapter
import org.robotics.blinkworld.BottomFragmaent.UserProfile.Fragment.UserProfileFragment
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.User


class OtherUserProfileFragment(private val uid:String) : BottomSheetDialogFragment(),UserListAdapter.Listener {
    private lateinit var addfriendButton:AppCompatButton
    private lateinit var photoUser:ImageView
    private lateinit var bioUser:TextView
    private lateinit var usernameUser:TextView
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var textYourFriend:TextView
    private lateinit var goChatButton: AppCompatButton
    private lateinit var notFriend:TextView

    private lateinit var mUser: User
    private lateinit var mUsers: List<User>
    private lateinit var mFollowers: User
    private lateinit var mAdapter: UserListAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //отоброжение имя био и фото
        database.child(NODE_USERS).child(uid)
            .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                photoUser.loadUserPhoto(mUser.photo)
                usernameUser.text = mUser.username
                bioUser.text = mUser.bio
                checkButton(uid,addfriendButton,textYourFriend)

            })

        //подписка
        addfriendButton.setOnClickListener {

            database.child(NODE_USERS).child(currentUid()!!).child(NODE_FOLLOWING).child(uid).setValue(true)
            database.child(NODE_USERS).child(uid).child(NODE_FOLLOWERS).child(currentUid()!!).setValue(true)
            database.child(NODE_FOLLOWING).child(currentUid()!!).child(uid).child("uid").setValue(uid)
            database.child(NODE_FOLLOWERS).child(uid).child(currentUid()!!).child("uid").setValue(currentUid()!!)

            addfriendButton.visibility = View.GONE
        }



        //Переход в чат
        goChatButton.setOnClickListener {
            val intent = Intent(context, MessagesActivity::class.java)
            intent.putExtra("uid" , uid)
            context?.startActivities(arrayOf(intent))
        }

        val display: Display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y
        if(width == 720 ){
            goChatButton.layoutParams.width = 485
        }else if(width == 1080){
            goChatButton.layoutParams.width = 735
        }

        //движение бэкграунда кнопки
        val first_duration = (2000 until 5000).random()
        val second_duration = (2000 until 5000).random()
        val animationDrawable = addfriendButton.background as AnimationDrawable
        animationDrawable.apply {
            setEnterFadeDuration(first_duration)
            setExitFadeDuration(second_duration)
            start()
        }
    }


    //открыть боттом меню во весь рост
    override fun onStart() {
        super.onStart()
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view =inflater.inflate(R.layout.fragment_other_user_profile, container, false)
        photoUser = view.findViewById(R.id.other_user_photo_profile)
        usernameUser = view.findViewById(R.id.other_user_profile_name)
        bioUser = view.findViewById(R.id.other_user_profile_bio)
        addfriendButton= view.findViewById(R.id.add_friend_button_other_fragment)
        goChatButton = view.findViewById(R.id.go_chat_other_user_button)
        mRecyclerView = view.findViewById(R.id.recycler_view_user_profile)
        textYourFriend = view.findViewById(R.id.button_other_text_view)
        notFriend = view.findViewById(R.id.doesnt_have_friend_text_view)
        loadUser()
        return view
    }




    private fun loadUser() {
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter =
            UserListAdapter(this)
        mRecyclerView.adapter = mAdapter
            mRecyclerView.visibility = View.VISIBLE
        val rootRef = database.child("friends").child(uid)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children.map { it.getUserModel() }
                if(children.isEmpty()){
                    mRecyclerView.visibility = View.GONE
                    notFriend.visibility = View.VISIBLE

                }else{
                    mRecyclerView.visibility = View.VISIBLE
                    notFriend.visibility = View.GONE
                }
                children.forEach {
                    database.child(NODE_USERS).child(it.uid).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {datasnapshot ->
                        val users = datasnapshot.getUserModel()
                        it.username = users.username
                        it.photo = users.photo
                        mUser = children.first()
                        mRecyclerView.adapter = mAdapter
                        mAdapter.updateDataSet(children, mUser.followers)
                        Log.d("Followers", users.toString())
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        }

    override fun follow(uid: String) {
        setFollow(uid, true) {
            mAdapter.followed(uid)
            database.child(NODE_FOLLOWING).child(currentUid()!!).child(uid).child(CHILD_UID).setValue(uid)
            database.child(NODE_FOLLOWERS).child(uid).child(currentUid()!!).child(CHILD_UID).setValue(currentUid()!!)
        }
    }

    override fun unfollow(uid: String) {
        setFollow(uid, false) {
            mAdapter.unfollowed(uid)
            database.child(NODE_FOLLOWING).child(currentUid()!!).child(uid).child(CHILD_UID).removeValue()
            database.child(NODE_FOLLOWERS).child(uid).child(currentUid()!!).child(CHILD_UID).removeValue()

        }
    }

    override fun click(uid: String) {
        if(uid == currentUid()!!){
            val bottomSheet = UserProfileFragment()
            bottomSheet.show(requireFragmentManager(), "")
        }else{
            val bottomSheet = OtherUserProfileFragment(uid)
            bottomSheet.show(requireFragmentManager(), "")
        }

    }





}


