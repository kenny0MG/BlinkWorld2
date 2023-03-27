package org.robotics.blinkworld.BottomFragmaent.StoriesVideoView.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.activity_registration_phone.*
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.User

class PhotoStoriesFragment(private val uid:String,private val img:String) : BottomSheetDialogFragment()  {
    private lateinit var mUser:User
    private lateinit var close:ImageView
    private lateinit var name:TextView
    private lateinit var image:ImageView
    private lateinit var photoUser:ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_photo_stories, container, false)
        close = view.findViewById(R.id.full_screan_close_photo)
        photoUser=view.findViewById(R.id.full_screan_avatar_photo)
        name=view.findViewById(R.id.name_full_photo)
        image=view.findViewById(R.id.itemVideoPlayerThumbnail_photo)
        close.setOnClickListener {
            dismiss()
        }

        database.child(NODE_USERS).child(uid!!).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
            mUser = it.getValue(User::class.java)!!
            photoUser.clipToOutline = true
            photoUser.loadUserPhoto(mUser.photo)
            name.text = mUser.username

            image.loadUserPhoto(img)



        })

        return view
    }
    override fun onStart() {
        super.onStart()
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

    }


}