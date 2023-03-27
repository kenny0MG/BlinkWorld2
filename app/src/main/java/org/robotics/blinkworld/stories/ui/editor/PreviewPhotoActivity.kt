package org.robotics.blinkworld.stories.ui.editor

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import org.robotics.blinkworld.Activity.Messages.MessagesActivity
import org.robotics.blinkworld.stories.App.Companion.sendFileToFirestore


import org.robotics.blinkworld.R
import org.robotics.blinkworld.databinding.ActivityPreviewBinding
import org.robotics.blinkworld.stories.App
import org.robotics.blinkworld.stories.photoeditor.PhotoEditor
import org.robotics.blinkworld.stories.photoeditor.listener.OnPhotoEditorListener
import org.robotics.blinkworld.stories.photoeditor.util.SaveSettings
import org.robotics.blinkworld.stories.ui.editor.TextEditorDialogFragment.Companion.show
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class PreviewPhotoActivity : AppCompatActivity(), OnPhotoEditorListener, View.OnClickListener {
    private lateinit var binding: ActivityPreviewBinding
    private lateinit var mPhotoEditor: PhotoEditor
    private lateinit var imagePath: String
    private lateinit var uid:String
    private lateinit var photo:String
    private var a=1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)

        initViews()
        binding.fullScreanCloseStoriesPhoto.setOnClickListener {
            finish()
        }

        imagePath = intent.getStringExtra("DATA") ?: ""
        if (imagePath.isBlank()) finish()

        Glide.with(this).load(imagePath).into(binding.ivImage.source)

        setContentView(binding.root)
        val animationDrawable = binding.imgDone.background as AnimationDrawable
        animationDrawable.apply {
            setEnterFadeDuration(15000)
            setExitFadeDuration(15000)
            start()
        }
        val contact  = intent.getStringExtra("uid")
        val photo  = intent.getStringExtra("photo")

        Log.d("UidPhoto",contact + photo)
    }

    private fun initViews() {
        mPhotoEditor = PhotoEditor.Builder(this, binding.ivImage)
            .setPinchTextScalable(true)
            .setDeleteView(binding.imgDelete)
            .build()

        mPhotoEditor.setOnPhotoEditorListener(this)
        binding.imgClose.setOnClickListener(this)
        binding.imgDone.setOnClickListener(this)
        binding.imgText.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if(a !=0 ) {
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
        try {
            val fileOrigin = File(imagePath)

            val saveSettings = SaveSettings.Builder()
                .setCompressQuality(80)
                .setCompressFormat(Bitmap.CompressFormat.PNG)
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(false)
                .build()

            mPhotoEditor.saveAsFile(fileOrigin.absolutePath, saveSettings, object :
                PhotoEditor.OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    uid  = intent.getStringExtra("uid").toString()
                    photo = intent.getStringExtra("photo").toString()
                    val username =  intent.getStringExtra("username")
                    val type = 1
                    binding.ivImage.source.setImageURI(Uri.fromFile(File(imagePath)))
                    App.sendFileToFirestore(fileOrigin,uid,photo, username!!,type,this@PreviewPhotoActivity)


                }

                override fun onFailure(exception: Exception) {
                    Toast.makeText(
                        this@PreviewPhotoActivity,
                        "Saving Failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("Error",exception.message.toString())
                }
            })

        } catch (e: IOException) {
            e.printStackTrace()
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