package org.robotics.blinkworld.BottomFragmaent.AddFriendsFragment.Fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_add_friends.*
import org.robotics.blinkworld.Adapter.AddUserListAdapter
import org.robotics.blinkworld.Adapter.UserListAdapter
import org.robotics.blinkworld.BottomFragmaent.OtherUserProfile.Fragment.OtherUserProfileFragment
import org.robotics.blinkworld.BottomFragmaent.UserProfile.Fragment.UserProfileFragment
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.User

class AddFriendsFragment :  BottomSheetDialogFragment(),AddUserListAdapter.Listener {
    private lateinit var mUser: User
    private lateinit var mFollowers: User
    private lateinit var mAdapter: AddUserListAdapter
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_add_friends, container, false)
        mRecyclerView = view.findViewById(R.id.recycler_add_friends)
        loadUser()
        return view
    }


    private fun loadUser() {
       mRecyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter =
            AddUserListAdapter(this)
        //mRecyclerView.visibility = View.VISIBLE
        val rootRef = database.child(NODE_FOLLOWERS).child(currentUid()!!)
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children.map { it.getUserModel() }
                children.forEach {
                    database.child(NODE_USERS).child(it.uid).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter { datasnapshot ->
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
    override fun onStart() {
        super.onStart()

        //открыть боттом меню во весь рост
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    companion object {

    }

    override fun follow(uid: String) {
            database.child(NODE_USERS).child(uid).child("friends")
            .child(currentUid()!!).setValue(true)
            database.child(NODE_USERS).child(currentUid()!!).child("friends")
            .child(uid).setValue(true)
            database.child("friends").child(currentUid()!!).child(uid).child(CHILD_UID).setValue(uid)
            database.child("friends").child(uid).child(currentUid()!!).child(CHILD_UID).setValue(currentUid()!!)

            database.child(NODE_FOLLOWING).child(currentUid()!!).child(uid).child(CHILD_UID).child(uid).removeValue()
            database.child(NODE_FOLLOWING).child(uid).child(currentUid()!!).child(CHILD_UID).child(currentUid()!!).removeValue()


            database.child(NODE_FOLLOWERS).child(uid).child(currentUid()!!).child(CHILD_UID).child(currentUid()!!).removeValue()
            database.child(NODE_FOLLOWERS).child(currentUid()!!).child(uid).child(CHILD_UID).child(uid).removeValue()


        database.child(NODE_USERS).child(currentUid()!!).child(NODE_FOLLOWERS).child(uid).removeValue()
            database.child(NODE_USERS).child(uid).child(NODE_FOLLOWING).child(currentUid()!!).removeValue()



    }

    override fun unfollow(uid: String) {

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