package org.robotics.blinkworld.BottomFragmaent.Messages.Fragment

import android.app.ActionBar
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.robotics.blinkworld.Activity.Messages.MessagesActivity
import org.robotics.blinkworld.Adapter.ChatsAdapter
import org.robotics.blinkworld.BottomFragmaent.NewMessageChat.Fragment.NewMessageChatFragment
import org.robotics.blinkworld.BottomFragmaent.UserProfile.Fragment.UserProfileFragment
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.Chat
import org.robotics.blinkworld.models.Message
import org.robotics.blinkworld.models.User


class ChatsFragment : BottomSheetDialogFragment() {
    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var filterEditText: EditText
    private lateinit var buttonNewMessage:AppCompatButton
    private lateinit var notChat:TextView
    private var mIsScrolling = false
    private var mSmoothScrollToPosition = true
    private lateinit var mLayoutManager: LinearLayoutManager

    private lateinit var mAdapter: ChatsAdapter
    private val mRefMainList = database.child(NODE_MAIN_LIST).child(currentUid()!!)
    private var mListItems = listOf<Chat>()
    private val mRefUsers = database.child(NODE_USERS)
    private val mRefMessages = database.child(NODE_MESSAGES).child(currentUid()!!)
    private var i = 0
    private lateinit var tempList:List<Message>
    private lateinit var mChat:Chat



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        filterEditText = view.findViewById(R.id.filter_chats_text_edit_text)
        chatsRecyclerView = view.findViewById(R.id.recycler_chats_fragment)
        buttonNewMessage = view.findViewById(R.id.button_new_chat)
        notChat = view.findViewById(R.id.not_chat)

       buttonNewMessage.setOnClickListener {
           val bottomSheet = NewMessageChatFragment()
           bottomSheet.show(requireFragmentManager(), "")
       }
        mAdapter = ChatsAdapter{
            val intent = Intent(context, MessagesActivity::class.java)
            intent.putExtra("uid", it.id)
            context?.startActivities(arrayOf(intent))
        }

        val display: Display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        if(width == 720){
            filterEditText.layoutParams.width = 425
        }
        else if(width == 1080){
            filterEditText.layoutParams.width = 645
        }

        mLayoutManager = LinearLayoutManager(context)


        chatsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("size",mLayoutManager.findFirstVisibleItemPosition().toString())
                if (mLayoutManager.findFirstVisibleItemPosition() != 0) {
                    i = 1
                }
                else{
                    i=0
                }
            }


        })
        //Фильтрация recyclerView
        filterEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val userText = s?.toString()

                if (userText.isNullOrBlank()) {
                    i = 0
                    initRecyclerView()
                } else {
                    i = 1
                    val filteredUserList = mListItems.filter {
                        it.username.lowercase().contains(userText.lowercase())
                    }

                    mAdapter.updateDataSet(filteredUserList)


                }
            }
        })
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if(i == 0){
                    initRecyclerView()
                    mAdapter.updateDataSet(mListItems.sortedByDescending { it.timeStamp })
                }

                mainHandler.postDelayed(this, 1000)
            }
        })


        return view
    }


    //открыть боттом меню во весь рост


    private fun initRecyclerView() {

        chatsRecyclerView.adapter = mAdapter

        chatsRecyclerView.layoutManager = mLayoutManager

        // 1 запрос


        mRefMainList.addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter { dataSnapshot ->
            mListItems = dataSnapshot.children.map { it.getChatModel() }

            if(mListItems.isEmpty()){
                notChat.visibility = View.VISIBLE
                chatsRecyclerView.visibility = View.GONE
            }else{
                notChat.visibility = View.GONE
                chatsRecyclerView.visibility = View.VISIBLE
            }

            mListItems.forEach { model ->

                mRefUsers.child(model.id)
                    .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter { dataSnapshot1 ->
                        val newModel = dataSnapshot1.getUserModel()
                        model.username = newModel.username
                        model.photo = newModel.photo


                        // 3 запрос
                        mRefMessages.child(model.id).limitToLast(1)
                            .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter { dataSnapshot2 ->
                                tempList = dataSnapshot2.children.map { it.getMessageModel() }
                                model.timeStamp = tempList[0].timeStamp
                                model.messageList = tempList


                                if (tempList[0].uid == currentUid()!!) {
                                    model.lastMsg = "you: " + tempList[0].text
                                    if (tempList[0].type == "image") {
                                        model.lastMsg = "you: " + "Send post"
                                    }
                                    if(model.lastMsg.length >= 23){
                                        model.lastMsg = "you: " + tempList[0].text.take(20) + "..."
                                    }
                                } else {
                                    model.lastMsg = tempList[0].text
                                    if (tempList[0].type == "image") {
                                        model.lastMsg = "Send post"
                                    }
                                    if(model.lastMsg.length >= 23){
                                        model.lastMsg = tempList[0].text.take(22) + "..."
                                    }
                                }
                                if (tempList.isNotEmpty()) {//вывод картирнки нового сообщения
                                    newModel.uid = tempList[0].uid
                                    //mAdapter.updateListItems(newModel)
                                    if (newModel.uid == currentUid()!!) {
                                        model.read = true

                                    } else {
                                        model.read = tempList[0].read
                                        database.child(NODE_MAIN_LIST).child(currentUid()!!).child(model.id).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                                            mChat = it.getValue(Chat::class.java)!!
                                            mChat.read = tempList[0].read
                                        })



                                    }


                                }


                            })

                    })


            }



        })



    }




    override fun onStart() {
        super.onStart()
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

    }



}