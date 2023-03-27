package org.robotics.blinkworld.Activity.Messages

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import kotlinx.android.synthetic.main.activity_messages.*
import org.robotics.blinkworld.Adapter.MessagesAdapter
import org.robotics.blinkworld.BottomFragmaent.OtherUserProfile.Fragment.OtherUserProfileFragment
import org.robotics.blinkworld.BottomFragmaent.StoriesVideoView.Fragment.PhotoStoriesFragment
import org.robotics.blinkworld.BottomFragmaent.StoriesVideoView.Fragment.VideoStoriesFragment
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.Chat
import org.robotics.blinkworld.models.Message
import org.robotics.blinkworld.models.User
import org.robotics.blinkworld.stories.ui.main.StoriesActivity

class MessagesActivity : AppCompatActivity(),MessagesAdapter.Listener {
    private lateinit var mListener: Utils.ValueEventListenerAdapter
    private lateinit var mChat: Chat
    private lateinit var mUser:User
    private lateinit var mOtherUser:User
    private lateinit var nameChatTextView: TextView
    private lateinit var mMessage:Message
    private val mRefUsersNode = database.child(NODE_USERS)
    private lateinit var mRefUser: DatabaseReference
    private lateinit var mRefMessages: DatabaseReference
    private var lastMsg: String? = null
    private val mRefMessagesNode = database.child(NODE_MESSAGES).child(currentUid()!!)
    private val mRefMainList = database.child(NODE_MAIN_LIST).child(currentUid()!!)
    private var mListItems = listOf<Message>()
    private lateinit var contact:String

    private lateinit var mAdapter: MessagesAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mMessagesListener: AppChildEventListener
    private lateinit var photoChatUser:ImageView
    private var mCountMessages = 15
    private var mIsScrolling = false
    private var mSmoothScrollToPosition = true
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var userChatsPhotoProfile: ImageView
    private lateinit var cameraButton: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        mSwipeRefreshLayout = refresh_chat
        initRecycleView()
        contact  = intent.getStringExtra("uid")!!



        //Инициализация полей собеседника
        database.child(NODE_USERS).child(contact!!).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
            mOtherUser = it.getValue(User::class.java)!!
        })
        full_screan_close_chat.setOnClickListener {
            finish()

        }









        chattext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
               if(s!!.isNotEmpty()){
                   sendmsg.visibility =View.VISIBLE
                   camera_image_button.visibility = View.GONE
               }else{
                   sendmsg.visibility =View.GONE
                   camera_image_button.visibility = View.VISIBLE

               }

            }
        })








        //Инициализация параметро "User"
        database.child(NODE_USERS).child(currentUid()!!).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {

            mUser = it.getValue(User::class.java)!!



        })


        //Загрузка последнего сообщение для отоброжение в чате


        //Инициализация параметров "Chat"
        mListener = Utils.ValueEventListenerAdapter {
            mChat = it.getChatModel()
            mMessage= it.getMessageModel()

            initinfo()

        }

        //Функция отправки сообщение и сохранине ее в main list

        mRefUser = database.child(NODE_USERS).child(contact!!)
        mRefUser.addValueEventListener(mListener)



            sendmsg.setOnClickListener {
                mSmoothScrollToPosition = true
                val msg = chattext.text.toString()
                if (chattext.text!!.isNotEmpty()) {

                    sendmessage(msg, contact!!, TYPE_TEXT, mUser.photo!!) {
                        saveToMainList(contact, "chat")
                        chattext.setText("")
                        vibratePhone()

                    }
                }

            }


    }







    //Функция сохрание в main list главный экран
    fun saveToMainList(id: String, type: String) {
        val refUsers = "$NODE_MAIN_LIST/${currentUid()}/$id"
        val refReceived = "$NODE_MAIN_LIST/$id/${currentUid()}"

        val mapUser = hashMapOf<String,Any>()
        val mapReceived = hashMapOf<String,Any>()

        mapUser[CHILD_ID] = id
        mapUser[CHILD_TYPE] = type
        mapUser["read"] = true


        mapReceived[CHILD_ID] = currentUid()!!
        mapReceived[CHILD_TYPE] = type
        mapReceived["read"] = false

        val commonMap = hashMapOf<String,Any>()
        commonMap[refUsers] = mapUser
        commonMap[refReceived] = mapReceived

        database.updateChildren(commonMap)
            .addOnFailureListener {  }
    }





    //Выгрузка последних 10 сообщений и инициализация переменных
    private fun updateData() {
        mSmoothScrollToPosition = false
        mIsScrolling = false
        mCountMessages += 15
        mRefMessages.removeEventListener(mMessagesListener)
        mRefMessages.limitToLast(mCountMessages).addChildEventListener(mMessagesListener)
    }





    //инициализация  адаптера и recyclew view
    private fun initRecycleView() {
        mAdapter = MessagesAdapter(this)
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView = messages_recycle_adapter
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.isNestedScrollingEnabled = false
        mRecyclerView.layoutManager = mLayoutManager
        val itemDecorator = SpaceItemDecorator()
        mRecyclerView.addItemDecoration(itemDecorator)



        val contact = intent.getStringExtra("uid")

        mRefMessages = database.child(NODE_MESSAGES).child(currentUid()!!).child(contact!!)
        mRecyclerView.adapter = mAdapter
        mMessagesListener = AppChildEventListener {
            val message = it.getMessageModel()
            if (mSmoothScrollToPosition) {
                mAdapter.addItemToBottom(message) {
                    mRecyclerView.smoothScrollToPosition(mAdapter.itemCount)
                }
            } else {
                mAdapter.addItemToTop(message) {
                    mSwipeRefreshLayout.isRefreshing = false
                }
            }


        }
        //Прогрузка сообщений при свайпе
        mRefMessages.limitToLast(mCountMessages).addChildEventListener(mMessagesListener)
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (mIsScrolling && dy < 0 && mLayoutManager.findFirstVisibleItemPosition() <= 3) {
                    updateData()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    mIsScrolling = true
                }
            }
        })
        mSwipeRefreshLayout.setOnRefreshListener { updateData() }
    }




    private fun initinfo() {
 //Инициализация имени и фото собеседника






        photoChatUser = findViewById(R.id.user_profile_chat)
        photoChatUser.loadUserPhoto(mOtherUser.photo)
        nameChatTextView  =findViewById(R.id.text_name_chat)
        nameChatTextView.text = mOtherUser.username
        val contact  = intent.getStringExtra("uid")
        cameraButton = findViewById(R.id.camera_image_button)
        cameraButton.setOnClickListener {
            val intent = Intent(this , StoriesActivity::class.java)

            intent.putExtra("uid" , contact)
            intent.putExtra("photo" , mUser.photo)
            intent.putExtra("username" , mUser.username)



            startActivity(intent)
        }



        //
        photoChatUser.setOnClickListener {
            val bottomfrSheets = OtherUserProfileFragment(mOtherUser.uid)
            val fragmentManager = supportFragmentManager
            fragmentManager.let {
                bottomfrSheets.show(it, "")
            }
        }

        //что бы закруглить углы картинки
        photoChatUser.clipToOutline = true


    }






    //Функция отправки сообщений
    private fun sendmessage(msg: String, recivingUserId: String, typeText: String,photo:String, function: () -> Unit) {
        var refDialogUser = "messages/${currentUid()!!}/$recivingUserId"
        var refDialogRecivingUser = "messages/$recivingUserId/${currentUid()!!}"
        val messageKey = database.child(refDialogUser).push().key


        val mapMessage = hashMapOf<String, Any>()
        mapMessage["uid"] = currentUid()!!
        mapMessage["type"] = typeText
        mapMessage["text"] = msg
        mapMessage["read"] = false
        mapMessage["id"] = messageKey.toString()
        mapMessage["timeStamp"] = ServerValue.TIMESTAMP
        mapMessage["photo"] = photo


        val mapDialog = hashMapOf<String,Any>()
        mapDialog["$refDialogUser/$messageKey"] = mapMessage
        mapDialog["$refDialogRecivingUser/$messageKey"] = mapMessage

        database
            .updateChildren(mapDialog)
            .addOnSuccessListener { function() }
            .addOnFailureListener { }
    }
    override fun onPause() {
        super.onPause()
        mRefUser.removeEventListener(mListener)
        mRefMessages.removeEventListener(mMessagesListener)

    }





    //Функция прочитки сообщений
    override fun readMsg(id: String,uid: String) {
        contact  = intent.getStringExtra("uid")!!//функция читки сообщений
        if(uid != currentUid()) {
            val reference =
                database.child(NODE_MESSAGES).child(currentUid()!!).child(uid).child(id).child("read")
            reference.addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                reference.setValue(true)
            })
            val chatMsg =
                database.child(NODE_MAIN_LIST).child(currentUid()!!).child(contact).child("read")
               chatMsg.addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter{
                   chatMsg.setValue(true)
               })


        }
    }

    override fun openPosts(uid: String, img: String,type:Int) {
        if(type == 2){
            val bottomfrSheets = VideoStoriesFragment(img,uid)
            val fragmentManager = supportFragmentManager
            fragmentManager.let {
                bottomfrSheets.show(it, "")
            }
        }
        else{
            val bottomfrSheets = PhotoStoriesFragment(uid,img)
            val fragmentManager = supportFragmentManager
            fragmentManager.let {
                bottomfrSheets.show(it, "")
            }
        }

    }


    fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.EFFECT_TICK))
        } else {
            vibrator.vibrate(20)
        }
    }

    override fun notificstion(uid: String, text: String) {

    }







}




//Логика функции howChat()


//                        if (tempList.isEmpty()){
//                            newModel.read = true
//                            newModel.lastmsg = "Chat is cleared"
//                        } else {
//                            newModel.timeStampUser = tempList[0].timeStamp.toString().asTime()
//                            if(tempList[0].type == "posts"){
//                                newModel.lastmsg = "Recorded message"
//                            }
//                            else {
//                                if(tempList[0].text.length >=25){
//                                    newModel.lastmsg = tempList[0].text.substring(0, Math.min(tempList[0].text.length, 25)) + " ..."
//                                }
//                                else{
//                                    newModel.lastmsg = tempList[0].text
//                                }
//                            }
//                        }
//
//                        if(tempList.isNotEmpty()){//вывод картирнки нового сообщения
//                            newModel.from = tempList[0].from
//                            //mAdapter.updateListItems(newModel)
//                            if(newModel.from == currentUid()!!){
//                                newModel.read = true
//
//                            }else{
//                                newModel.read = tempList[0].read
//
//
//
//                            }
//
//                        }