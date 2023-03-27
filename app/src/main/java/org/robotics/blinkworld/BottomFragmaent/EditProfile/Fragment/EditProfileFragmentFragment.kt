package org.robotics.blinkworld.BottomFragmaent.EditProfile.Fragment

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivities
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.storage.UploadTask
import com.mapbox.maps.logD
import com.shulz.galleryapp.data.GalleryItem
import org.robotics.blinkworld.Activity.RegistrationPhone.RegistrationPhoneActivity
import org.robotics.blinkworld.Activity.Web.WebActivity
import org.robotics.blinkworld.BottomFragmaent.Gallery.Fragment.ChooseDataBottomSheet
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.User
import java.io.File
import java.lang.Exception


private lateinit var dialogs: CustomDialog
private lateinit var cpTitle: TextView
private lateinit var cpCardView: CardView
private lateinit var progressBar: ProgressBar
private lateinit var copyEmail:TextView
private lateinit var privacyPolicy:TextView

private const val PERMISSION_STORAGE = 101
private var myClip: ClipData? = null
private var myClipboard: ClipboardManager? = null


class EditProfileFragmentFragment : BottomSheetDialogFragment() {
    private lateinit var mUser: User

    private lateinit var editImage:ImageView
    private lateinit var username:EditText
    private lateinit var bio:EditText
    private lateinit var check:ImageView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myClipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?;



        database.child(NODE_USERS).child(currentUid()!!)
            .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                mUser = it.getUserModel()
                editImage.loadUserPhoto(mUser.photo)
                username.setText(mUser.username, TextView.BufferType.EDITABLE)
                bio.setText(mUser.bio, TextView.BufferType.EDITABLE)
            })

        copyEmail.setOnClickListener {
            myClip = ClipData.newPlainText("text", copyEmail.text);
            myClipboard?.setPrimaryClip(myClip!!);

            Toast.makeText(requireContext(), "Text Copied", Toast.LENGTH_SHORT).show();
        }


        privacyPolicy.setOnClickListener {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra("link","https://docs.google.com/document/d/e/2PACX-1vR1cSthMdPZZjgXYlw3Wt15ouqunmuuir-f6WiaRbd9nrRQ1YUYSG_4aP9ii_iZMQ/pub")
            requireContext().startActivities(arrayOf(intent))
        }


        check.setOnClickListener {
            if (username.text.isNotEmpty()) {
                if(username.text.toString() != mUser.username){
                    database.child(NODE_USERS).child(currentUid()!!).child("username")
                        .setValue(username.text.toString())
                    Toast.makeText(context, "Changes saved", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                if(bio.text.toString() != mUser.bio){
                    database.child(NODE_USERS).child(currentUid()!!).child("bio").
                    setValue(bio.text.toString())
                    Toast.makeText(context, "Changes saved", Toast.LENGTH_SHORT).show()
                    dismiss()

                }

            } else {
                Toast.makeText(context, "Username is empty", Toast.LENGTH_SHORT).show()


            }
        }


            editImage.setOnClickListener {
                val inflater = (activity)!!.layoutInflater
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
                dialogs = CustomDialog(requireContext())
                dialogs.setContentView(view)



                if (setupPermissions()) {


                    val data = mutableListOf<GalleryItem>().apply {
                        addAll(fetchImages())

                        sortByDescending { it.dateCreated }
                    }

                    ChooseDataBottomSheet(data) {
                        //binding.image.setImageResource(android.R.color.transparent)
                        val file = File(it.url).absolutePath
                        val fileUri = Uri.fromFile(File(file))
                        val dataRef = storage.child("avatar/${fileUri.lastPathSegment}")
                        val uploadTask = dataRef.putFile(fileUri)
                        dialogs.show()
                        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            return@Continuation dataRef.downloadUrl
                        }).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                dialogs!!.hide()
                                Toast.makeText(activity, "Image is saved", Toast.LENGTH_LONG)
                                    .show()
                                addUploadRecordToDb(task.result.toString())
                                dismiss()



                                // remove file after send
                            } else {
                                // Handle failures
                            }
                        }.addOnFailureListener {
                            dialogs!!.hide()
                            Toast.makeText(activity, "Error: ${it.message}", Toast.LENGTH_LONG)
                                .show()
                        }






                    }.show(requireFragmentManager(), "ChooseDataBottomSheet")

                } else {
                    Toast.makeText(context, "Allow media access", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_edit_profile_fragment, container, false)
        editImage = view.findViewById(R.id.image_edit_profile)
        privacyPolicy = view.findViewById(R.id.privacy_policy_text)
        username = view.findViewById(R.id.text_user_name)
        bio = view.findViewById(R.id.text_description)
        check = view.findViewById(R.id.check_edit_image)
        copyEmail = view.findViewById(R.id.email_text_copy)
        return view
    }


    override fun onStart() {
        super.onStart()

        //открыть боттом меню во весь рост
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }



    //Получаем доступ
    private fun setupPermissions(): Boolean {
        val permission1 =
            ContextCompat.checkSelfPermission( requireContext() as Activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        val permission2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission( requireContext() as Activity, Manifest.permission.ACCESS_MEDIA_LOCATION)
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
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


    //Получаем изображения
    private fun fetchImages(): ArrayList<GalleryItem> {
        val imageList: ArrayList<GalleryItem> = ArrayList()

        val columns = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )
        val imagecursor: Cursor = requireActivity().managedQuery(
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

//загрузка Uri в базу данный
    private fun addUploadRecordToDb(uri: String){
        database.child(NODE_USERS).child(currentUid()!!).child("photo").setValue(uri)
            .addOnSuccessListener { documentReference ->
                //Picasso.get().load(uri).placeholder(R.drawable.ic_close_foreground).into(imageView2)

                //Toast.makeText(context, "Saved image", Toast.LENGTH_LONG).show()



            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving image", Toast.LENGTH_LONG).show()
            }
    }

    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }








}