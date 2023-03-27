package org.robotics.blinkworld.BottomFragmaent.StoriesVideoView.Fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mapbox.maps.logD
import kotlinx.android.synthetic.main.fragment_video_stories.*
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.User

class VideoStoriesFragment(private val image:String,private val uid:String) : BottomSheetDialogFragment() {
    private lateinit var playerVideo: ExoPlayer
    private lateinit var mUser: User
    private lateinit var photo:ImageView
    private lateinit var video: PlayerView
    private lateinit var imageTh: ImageView
    private lateinit var close:ImageView
    private lateinit var videoBitmapDecoder:VideoBitmapDecoder



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video_stories, container, false)
        close = view.findViewById(R.id.full_screan_close_video)

        close.setOnClickListener {
            dismiss()
        }

        photo = view.findViewById(R.id.full_screan_avatar_video)
        imageTh = view.findViewById(R.id.itemVideoPlayerThumbnail_posts)
        video = view.findViewById(R.id.full_screen_video_player)


        database.child(NODE_USERS).child(uid).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
            mUser = it.getValue(User::class.java)!!
            photo.clipToOutline = true
            photo.loadUserPhoto(mUser.photo)
            name_full_video.text = mUser.username



        })




        playerVideo = SimpleExoPlayer.Builder(requireContext()).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL
        }



        video.player = playerVideo



       imageTh.loadUserPhoto(image)








        playerVideo.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {

                super.onPlaybackStateChanged(playbackState)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    //getTimeLeft()

                    Handler().postDelayed(
                        {
                            if (video.player != null) {
//                                videoBinding.timeLeft.visibility = View.VISIBLE
                                video!!.visibility = View.VISIBLE
                                imageTh.visibility =
                                    View.INVISIBLE
                            }
                        },
                        DELAY_BEFORE_HIDE_THUMBNAIL
                    ) // wait to be sure the texture view is render
                }
            }
        })


        playerVideo.setMediaItem(MediaItem.fromUri(image!!))
        playerVideo.prepare()
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        playerVideo.stop()
        playerVideo.release()

    }
    override fun onStart() {
        super.onStart()
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

    }
}