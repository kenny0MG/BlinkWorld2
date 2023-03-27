package org.robotics.blinkworld.stories.ui.main

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.iammert.library.cameravideobuttonlib.CameraVideoButton
import com.mapbox.maps.logD
import com.shulz.galleryapp.data.GalleryItem
import org.robotics.blinkworld.BottomFragmaent.EditProfile.Fragment.EditProfileFragmentFragment
import org.robotics.blinkworld.BottomFragmaent.Gallery.Fragment.ChooseDataBottomSheet

import org.robotics.blinkworld.databinding.ActivityStoriesBinding
import org.robotics.blinkworld.stories.ui.editor.PreviewPhotoActivity
import org.robotics.blinkworld.stories.ui.editor.PreviewVideoActivity
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.io.path.Path
import kotlin.io.path.appendBytes

class StoriesActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityStoriesBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private var camera:Camera? = null

    var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val PERMISSION_STORAGE = 101


    private lateinit var cameraExecutor: ExecutorService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityStoriesBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.fullScreanCloseStories.setOnClickListener {
            finish()
        }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        setUpButtonClickListeners()





        cameraExecutor = Executors.newSingleThreadExecutor()

    }







    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissions not granted by ther user.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setUpButtonClickListeners() {
        viewBinding.button.enablePhotoTaking(true)
        viewBinding.button.enableVideoRecording(true)
        viewBinding.button.setVideoDuration(15 * 1000L)

        viewBinding.button.actionListener = object : CameraVideoButton.ActionListener {
            override fun onDurationTooShortError() {
                Toast.makeText(
                    this@StoriesActivity,
                    "Длительность видео должно быть >1 сек",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onEndRecord() {
                captureVideo()
            }

            override fun onSingleTap() {
                takePhoto()
            }

            override fun onStartRecord() {
                captureVideo()
            }
        }

        viewBinding.btnToggleCamera.setOnClickListener {
            toggleCamera()
        }
//        viewBinding.galleryTextStories.setOnClickListener {
//            if (setupPermissions()) {
//
//
//
//                val data = mutableListOf<GalleryItem>().apply {
//                    addAll(fetchImages())
//                    addAll(fetchVideos())
//                    sortByDescending { it.dateCreated }
//                }
//
//
//                ChooseDataBottomSheet(data) {
//
//
//                    if (it.isVideo && it.videoDuration > 1 * 60 * 1000L) {
//                        Toast.makeText(this, "Видео дольше 1 минуты", Toast.LENGTH_SHORT).show()
//                        return@ChooseDataBottomSheet
//                    }
//                    if(!it.isVideo){
//                        startActivity(
//                            Intent(
//                                this@StoriesActivity,
//                                PreviewPhotoActivity::class.java
//                            ).apply {
//
//                                val contact  = intent.getStringExtra("uid")
//                                val photo  = intent.getStringExtra("photo")
//                                val username =  intent.getStringExtra("username")
//                                putExtra("username", username)
//                                putExtra("uid", contact)
//                                putExtra("photo", photo)
//
//                                val file = File(it.url).absolutePath
//                                val fileUri = Uri.fromFile(File(file))
//                                putExtra("DATA", fileUri.toString())
//
//
//                            })
//                    }
//                    else{
//                        startActivity(
//                            Intent(
//                                this@StoriesActivity,
//                                PreviewVideoActivity::class.java
//                            ).apply {
//
//                                val contact  = intent.getStringExtra("uid")
//                                val photo  = intent.getStringExtra("photo")
//                                val username =  intent.getStringExtra("username")
//                                putExtra("username", username)
//                                putExtra("uid", contact)
//                                val file = File(it.url).absolutePath
//                                val fileUri = Uri.fromFile(File(file))
//                                putExtra("DATA", fileUri.toString())
//
//
//                            })
//                    }
//
//
//
//                }.show(supportFragmentManager, "ChooseDataBottomSheet")
//
//            } else {
//                Toast.makeText(this, "Разрешите доступ к медиа", Toast.LENGTH_LONG).show()
//            }
//        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val cacheFile = File(cacheDir, "$name.png")
        
        if (cacheFile.exists()) cacheFile.delete()
        
        cacheFile.createNewFile()


        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(cacheFile).build(),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    startActivity(
                        Intent(
                            this@StoriesActivity,
                            PreviewPhotoActivity::class.java
                        ).apply {
                            putExtra("DATA", cacheFile.absolutePath)
                            val contact  = intent.getStringExtra("uid")
                            val photo  = intent.getStringExtra("photo")
                            val username =  intent.getStringExtra("username")
                            putExtra("username", username)
                            putExtra("uid", contact)
                            putExtra("photo", photo)
                            putExtra("username", username)

                        })
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG, "onError: $exception")
                    exception.printStackTrace()
                }
            })
    }

    @SuppressLint("MissingPermission")
    private fun captureVideo() {
        val videoCapture = videoCapture ?: return

        viewBinding.btnToggleCamera.isVisible = false

        val currentRecording = recording
        if (null != currentRecording) {
            currentRecording.stop()
            recording = null
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val cacheFile = File(cacheDir, "$name.mp4")

        if (cacheFile.exists()) cacheFile.delete()

        cacheFile.createNewFile()

        val fileOutputOptions = FileOutputOptions.Builder(cacheFile).build()

        videoCapture.output.prepareRecording(this, fileOutputOptions)

        recording = videoCapture.output
            .prepareRecording(this, fileOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        this@StoriesActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        viewBinding.btnToggleCamera.isVisible = false
                    }
                    is VideoRecordEvent.Finalize -> {
                        viewBinding.btnToggleCamera.isVisible = true
                        if (!recordEvent.hasError()) {
                            startActivity(
                                Intent(
                                    this@StoriesActivity,
                                    PreviewVideoActivity::class.java
                                ).apply {
                                    putExtra("DATA", cacheFile.absolutePath)
                                    val contact  = intent.getStringExtra("uid")
                                    val photo  = intent.getStringExtra("photo")
                                    val username =  intent.getStringExtra("username")
                                    putExtra("username", username)
                                    putExtra("uid", contact)
                                    putExtra("photo", photo)

                                })
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " + "${recordEvent.error}")
                        }
                    }
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()



            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture =
                ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build()

            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HIGHEST,
                        FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                    )
                )
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, videoCapture
                )
                val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val scale = camera!!.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                        camera!!.cameraControl.setZoomRatio(scale)
                        return true
                    }
                }

                val scaleGestureDetector = ScaleGestureDetector(this, listener)

                viewBinding.viewFinder.setOnTouchListener { _, event ->
                    scaleGestureDetector.onTouchEvent(event)
                    return@setOnTouchListener true
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Use Case binding failed", ex)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun toggleCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else
            CameraSelector.DEFAULT_BACK_CAMERA

        startCamera()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }


    //Gallery
    private fun setupPermissions(): Boolean {
        val permission1 =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val permission2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION)
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    arrayOf(
                        Manifest.permission.ACCESS_MEDIA_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                } else {
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }, 1
            )

            return false
        } else {
            return true
        }
    }


    private fun fetchImages(): ArrayList<GalleryItem> {
        val imageList: ArrayList<GalleryItem> = ArrayList()

        val columns = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )
        val imagecursor: Cursor = managedQuery(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
            null, ""
        )
        for (i in 0 until imagecursor.count) {
            imagecursor.moveToPosition(i)
            val dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val dateColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)


            imageList.add(
                GalleryItem(
                    imagecursor.getString(dataColumnIndex),
                    imagecursor.getLong(dateColumnIndex)
                )
            )
        }
        return imageList
    }

    private fun fetchVideos(): ArrayList<GalleryItem> {
        val videoList: ArrayList<GalleryItem> = ArrayList()

        val columns = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED
        )
        val imagecursor: Cursor = managedQuery(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null,
            null, ""
        )
        for (i in 0 until imagecursor.count) {
            imagecursor.moveToPosition(i)
            val dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Video.Media.DATA)
            val dateColumnIndex = imagecursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumnIndex = imagecursor.getColumnIndex(MediaStore.Video.Media.DURATION)
            videoList.add(
                GalleryItem(
                    imagecursor.getString(dataColumnIndex),
                    imagecursor.getLong(dateColumnIndex),
                    true,
                    imagecursor.getLong(sizeColumnIndex)
                )
            )
        }
        return videoList
    }








}

