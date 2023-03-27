package org.robotics.blinkworld.BottomFragmaent.UserProfile.Fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.robotics.blinkworld.Adapter.UserListAdapter
import org.robotics.blinkworld.BottomFragmaent.EditProfile.Fragment.EditProfileFragmentFragment
import org.robotics.blinkworld.BottomFragmaent.OtherUserProfile.Fragment.OtherUserProfileFragment
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.User


class UserProfileFragment : BottomSheetDialogFragment(),UserListAdapter.Listener {
    private lateinit var photo: ImageView
    private lateinit var username: TextView
    private lateinit var bio:TextView
    private lateinit var settings:ImageView

    private lateinit var mUser: User
    private lateinit var mUsers: List<User>
    private lateinit var mFollowers: User
    private lateinit var recylerFriendsUserProgileFragment: RecyclerView
    private lateinit var mAdapter: UserListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database.child(NODE_USERS).child(currentUid()!!)
            .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                mUser = it.getValue(User::class.java)!!
                photo.loadUserPhoto(mUser.photo)
                username.text = mUser.username
                bio.text = mUser.bio

            })
        settings.setOnClickListener {
            val bottomSheet = EditProfileFragmentFragment()
            bottomSheet.show(requireFragmentManager(), "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)
        photo = view.findViewById(R.id.user_photo_profile)
        username = view.findViewById(R.id.user_profile_name)
        bio = view.findViewById(R.id.user_profile_bio)
        settings = view.findViewById(R.id.edit_profile_image)
        recylerFriendsUserProgileFragment = view.findViewById(R.id.recycler_view_user_profile)
        loadUser()
        return view
    }

    override fun onStart() {
        super.onStart()

        //открыть боттом меню во весь рост
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }


    private fun loadUser() {
        recylerFriendsUserProgileFragment.layoutManager = LinearLayoutManager(context)
        mAdapter =
            UserListAdapter(this)
        //mRecyclerView.visibility = View.VISIBLE
        val rootRef = database.child("friends").child(currentUid()!!)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children.map { it.getUserModel() }
                children.forEach {
                    database.child(NODE_USERS).child(it.uid).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {datasnapshot ->
                        val users = datasnapshot.getUserModel()
                        it.username = users.username
                        it.photo = users.photo
                        mUser = children.first()
                        recylerFriendsUserProgileFragment.adapter = mAdapter
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
        }    }
}