package org.robotics.blinkworld.stories

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivities
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.ServerValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import org.robotics.blinkworld.Activity.Messages.MessagesActivity
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import java.io.File
import kotlin.coroutines.coroutineContext

private lateinit var dialog: CustomDialog
private lateinit var cpTitle: TextView
private lateinit var cpCardView: CardView
private lateinit var progressBar: ProgressBar

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // empty cache dir
        deleteDir(cacheDir)
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete()
    }


    companion object {




        private fun addUploadRecordToDb(uri: String, uid: String, photo: String,username:String,type:Int) {

            sendmessage(uri, uid, TYPE_IMAGE, photo,username,type) {
                saveToMainList(uid, "chat")
            }


        }


        fun saveToMainList(id: String, type: String) {
            val refUsers = "$NODE_MAIN_LIST/${currentUid()}/$id"
            val refReceived = "$NODE_MAIN_LIST/$id/${currentUid()}"

            val mapUser = hashMapOf<String, Any>()
            val mapReceived = hashMapOf<String, Any>()

            mapUser[CHILD_ID] = id
            mapUser[CHILD_TYPE] = type

            mapReceived[CHILD_ID] = currentUid()!!
            mapReceived[CHILD_TYPE] = type

            val commonMap = hashMapOf<String, Any>()
            commonMap[refUsers] = mapUser
            commonMap[refReceived] = mapReceived

            database.updateChildren(commonMap)
                .addOnFailureListener { }
        }


        private fun sendmessage(
            image: String,
            recivingUserId: String,
            typeImage: String,
            photo: String,
            username:String,
            type:Int,
            function: () -> Unit
        ) {
            var refDialogUser = "messages/${currentUid()!!}/$recivingUserId"
            var refDialogRecivingUser = "messages/$recivingUserId/${currentUid()!!}"
            val messageKey = database.child(refDialogUser).push().key


            val mapMessage = hashMapOf<String, Any>()
            mapMessage["uid"] = currentUid()!!
            mapMessage["type"] = typeImage
            mapMessage["imagePosts"] = image
            mapMessage["read"] = false
            mapMessage["id"] = messageKey.toString()
            mapMessage["timeStamp"] = ServerValue.TIMESTAMP
            mapMessage["userPhoto"] = photo
            mapMessage["typeImage"] = type
            mapMessage["authorPost"] = username



            val mapDialog = hashMapOf<String, Any>()
            mapDialog["$refDialogUser/$messageKey"] = mapMessage
            mapDialog["$refDialogRecivingUser/$messageKey"] = mapMessage

            database
                .updateChildren(mapDialog)
                .addOnSuccessListener { function() }
                .addOnFailureListener { }
        }

        fun sendFileToFirestore(file: File, uid: String, photo: String,username:String,type:Int, activity: Activity) {

            val inflater = (activity).layoutInflater
            val view = inflater.inflate(R.layout.custom_progress_dialog, null)
            cpTitle = view.findViewById(R.id.cp_title)
            cpCardView = view.findViewById(R.id.cp_cardview)
            progressBar = view.findViewById(R.id.cp_pbar)

            // Card Color
            cpCardView.setCardBackgroundColor(Color.parseColor("#70000000"))

            // Progress Bar Color
            setColorFilter(
                progressBar.indeterminateDrawable,
                ResourcesCompat.getColor(activity.resources, R.color.yellow, null)
            )

            // Text Color
            cpTitle.setTextColor(Color.WHITE)

            // Custom Dialog initialization
            dialog = CustomDialog(activity)
            dialog.setContentView(view)
            dialog.show()

            val fileUri = Uri.fromFile(file)

            val dataRef = storage.child("data/${fileUri.lastPathSegment}")
            val uploadTask = dataRef.putFile(fileUri)


            uploadTask
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation dataRef.downloadUrl
            })
                .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                    addUploadRecordToDb(downloadUri.toString(), uid, photo,username,type)
                    activity.finish()
                    val intent = Intent(activity, MessagesActivity::class.java)
                    intent.putExtra("uid", uid)
                    activity.startActivities(arrayOf(intent))
                }



                    // remove file after send

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










}