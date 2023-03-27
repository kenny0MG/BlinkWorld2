package org.robotics.blinkworld.BottomFragmaent.NewMessageChat.Fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.robotics.blinkworld.Activity.Messages.MessagesActivity
import org.robotics.blinkworld.Adapter.ChatsAdapter
import org.robotics.blinkworld.Adapter.NewMessageAdapter
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.Chat
import org.robotics.blinkworld.models.User

class NewMessageChatFragment :  BottomSheetDialogFragment() {
    private lateinit var mAdapter: NewMessageAdapter
    private lateinit var mUser: User
    private var mListItems = listOf<User>()
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var filterEditText:EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_message_chat, container, false)
        mRecyclerView = view.findViewById(R.id.recycler_new_chats_fragment)
        filterEditText = view.findViewById(R.id.filter_new_chats_text_edit_text)






        filterEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val userText = s?.toString()

                if (userText.isNullOrBlank()) {
                    initRecyclerView()
                } else {
                    val filteredUserList = mListItems.filter {
                        it.username.lowercase().contains(userText.lowercase())
                    }

                    mAdapter.updateDataSet(filteredUserList)


                }
            }
        })


        initRecyclerView()

        return view
    }


private fun initRecyclerView(){
    mAdapter = NewMessageAdapter{
        val intent = Intent(context, MessagesActivity::class.java)
        intent.putExtra("uid", it.uid)
        context?.startActivities(arrayOf(intent))
    }
    mRecyclerView.adapter = mAdapter
    mRecyclerView.layoutManager = LinearLayoutManager(context)
    database.child("friends").child(currentUid()!!).addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter { dataSnapshot ->
        mListItems = dataSnapshot.children.map { it.getUserModel() }
        mListItems.forEach { model ->
            database.child(NODE_USERS).child(model.uid)
                .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter { dataSnapshot1 ->
                    val newModel = dataSnapshot1.getUserModel()
                    model.username =newModel.username
                    model.photo =newModel.photo
                    // 3 запрос
                    database.child(NODE_MAIN_LIST).child(currentUid()!!).child(model.uid)
                        .addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter { dataSnapshot2 ->
                            //val tempList = dataSnapshot2.children.map { it.getMessageModel() }
                            mAdapter.updateDataSet(mListItems)


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