package org.robotics.blinkworld.stories.ui.editor

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import org.robotics.blinkworld.Activity.Messages.MessagesActivity
import org.robotics.blinkworld.Activity.RegistrationPhone.LoginRegistrationActivity
import org.robotics.blinkworld.stories.App

import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.CustomDialog
import org.robotics.blinkworld.databinding.ActivityPreviewVideoBinding
import org.robotics.blinkworld.stories.photoeditor.PhotoEditor
import org.robotics.blinkworld.stories.photoeditor.listener.OnPhotoEditorListener
import org.robotics.blinkworld.stories.photoeditor.util.SaveSettings
import org.robotics.blinkworld.stories.ui.editor.TextEditorDialogFragment.Companion.show
import java.io.File
import java.io.IOException

class PreviewVideoActivity : AppCompatActivity(), OnPhotoEditorListener, View.OnClickListener {
    private lateinit var binding: ActivityPreviewVideoBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var mPhotoEditor: PhotoEditor
    private var mediaPlayer: MediaPlayer? = null

    private var videoPath = "";
    private var imagePath = ""
    private var a=1

    private lateinit var uid:String
    private lateinit var photo:String


    private lateinit var dialog: CustomDialog
    private lateinit var cpTitle: TextView
    private lateinit var cpCardView: CardView
    private lateinit var progressBar: ProgressBar
    private val onCompletionListener = OnCompletionListener { mediaPlayer -> mediaPlayer.start() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewVideoBinding.inflate(layoutInflater)
        binding.fullScreanCloseStoriesVideo.setOnClickListener {
            finish()
        }

        videoPath = intent.getStringExtra("DATA") ?: ""
        if (videoPath.isBlank()) finish()

        initViews()

        //Glide.with(this).load(R.drawable.trans).centerCrop().into(binding.ivImage.source)
        setContentView(binding.root)
    }

    private fun initViews() {
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

        mPhotoEditor = PhotoEditor.Builder(this, binding.ivImage)
            .setPinchTextScalable(true)
            .setDeleteView(binding.imgDelete)
            .build()

        mPhotoEditor.setOnPhotoEditorListener(this)
        binding.imgClose.setOnClickListener(this)
        binding.imgDone.setOnClickListener(this)
        binding.imgText.setOnClickListener(this)

        binding.videoSurface.surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                i: Int,
                i1: Int
            ) {
                val surface = Surface(surfaceTexture)
                try {
                    mediaPlayer = MediaPlayer()

                    mediaPlayer?.setDataSource(videoPath);
                    mediaPlayer?.setSurface(surface)
                    mediaPlayer?.prepare()
                    mediaPlayer?.setOnCompletionListener(onCompletionListener)
                    mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer?.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                i: Int,
                i1: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
        }
    }

    override fun onClick(v: View) {

        if(a !=0) {
            when (v.id) {
                R.id.imgClose -> onBackPressed()
                R.id.imgDone -> {
                    a = 0
                    saveImage()
                }
                R.id.imgText -> {
                    val textEditorDialogFragment = show(this, 0)
                    textEditorDialogFragment.setOnTextEditorListener(object :
                        TextEditorDialogFragment.TextEditor {
                        override fun onDone(inputText: String?, position: Int) {
                            mPhotoEditor.addText(
                                inputText,
                                position
                            )
                        }
                    })
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveImage() {


        val fileImage = File(cacheDir, "${System.currentTimeMillis()}.png")
        if (fileImage.exists()) fileImage.delete()

        try {
            fileImage.createNewFile()
            val saveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(false)
                .build()

            mPhotoEditor.saveAsFile(fileImage.absolutePath, saveSettings, object :
                PhotoEditor.OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    this@PreviewVideoActivity.imagePath = fileImage.absolutePath
                    binding.ivImage.source.setImageURI(Uri.fromFile(File(fileImage.absolutePath)))
                    applayWaterMark()
                }

                override fun onFailure(exception: Exception) {
                    Toast.makeText(
                        this@PreviewVideoActivity,
                        "Saving Failed...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun applayWaterMark() {
        val fileVideo = File(cacheDir, "${System.currentTimeMillis()}.mp4")

        if (fileVideo.exists()) fileVideo.delete()

        fileVideo.createNewFile()

        val command = "-y -i $videoPath -i $imagePath -filter_complex \"overlay\" ${fileVideo.absolutePath}"
        val inflater = (this).layoutInflater
        val view = inflater.inflate(R.layout.custom_progress_dialog, null)
        cpTitle = view.findViewById(R.id.cp_title)
        cpCardView = view.findViewById(R.id.cp_cardview)
        progressBar = view.findViewById(R.id.cp_pbar)

        // Card Color
        cpCardView.setCardBackgroundColor(Color.parseColor("#70000000"))

        // Progress Bar Color
        setColorFilter(
           progressBar.indeterminateDrawable,
            ResourcesCompat.getColor(resources, R.color.yellow, null)
        )

        // Text Color
       cpTitle.setTextColor(Color.WHITE)

        // Custom Dialog initialization
        dialog = CustomDialog(this)
        dialog.setContentView(view)
        dialog.show()

        dialog.setTitle("Пожалуйста подождите")

        FFmpeg.executeAsync(command) { _, returnCode ->
            when (returnCode) {
                RETURN_CODE_SUCCESS -> {
                    progressDialog.dismiss()
                    uid = intent.getStringExtra("uid").toString()
                    photo = intent.getStringExtra("photo").toString()
                    val username =  intent.getStringExtra("username")
                    val type = 2
                    App.sendFileToFirestore(fileVideo, uid, photo,username!!,type,this)

                }
                RETURN_CODE_CANCEL -> {
                    dialog.hide()
                }
                else -> {
                    dialog.hide()
                    val msg = String.format(
                        "Async command execution failed with returnCode=%d.",
                        returnCode
                    )

                    Toast.makeText(this, msg, Toast.LENGTH_LONG ).show()
                    Log.i("TAG_TEST", msg)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }
    override fun onEditTextChangeListener(
        rootView: View?,
        text: String?,
        colorCode: Int,
        pos: Int
    ) {
        val textEditorDialogFragment = show(this, text ?: "", pos)
        textEditorDialogFragment.setOnTextEditorListener(object :
            TextEditorDialogFragment.TextEditor {
            override fun onDone(inputText: String?, position: Int) {
                mPhotoEditor.editText(
                    rootView ?: return, inputText, position
                )
            }
        })
    }
}