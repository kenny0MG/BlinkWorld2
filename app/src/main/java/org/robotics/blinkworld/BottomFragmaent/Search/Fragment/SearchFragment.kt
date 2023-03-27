package org.robotics.blinkworld.BottomFragmaent.Search.Fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.robotics.blinkworld.Adapter.UserListAdapter
import org.robotics.blinkworld.BottomFragmaent.OtherUserProfile.Fragment.OtherUserProfileFragment
import org.robotics.blinkworld.BottomFragmaent.UserProfile.Fragment.UserProfileFragment
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.*
import org.robotics.blinkworld.models.User
import java.util.*


class SearchFragment : BottomSheetDialogFragment(),UserListAdapter.Listener {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var searchTextView:EditText
    private lateinit var textSearchFirst:TextView
    private lateinit var textSearchSecond:TextView
    private lateinit var notResultTestView:TextView


    private var timer: Timer? = null
    private lateinit var mUser: User
    private lateinit var mUsers: List<User>
    private lateinit var mAdapter: UserListAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textSearchFirst.visibility = View.VISIBLE
        textSearchSecond.visibility = View.VISIBLE
        mRecyclerView.visibility = View.GONE
        notResultTestView.visibility =View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {


        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchTextView = view.findViewById(R.id.search_search_fragment)
        mRecyclerView = view.findViewById(R.id.recycler_view_search)
        textSearchFirst = view.findViewById(R.id.search_friend_blink_blink_first)
        textSearchSecond = view.findViewById(R.id.search_friend_blink_blink_second)
        notResultTestView = view.findViewById(R.id.not_result_text_view)



//Поиск по firebase
        searchTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val userText = s?.toString()
                //timer = Timer()
               loadUser(userText)
            }
        })











        //Цветная тень вокруг edit text

        searchTextView.doOnNextLayout {
            val colors = intArrayOf(
                Color.WHITE,
                Color.YELLOW,
                Color.BLUE
            )
            val cornerRadius = 16f
            val padding = 45
            val centerX = it.width.toFloat() / 1 - padding
            val centerY = it.height.toFloat() / 1 - padding

            val shadowDrawable = createShadowDrawable(
                colors = colors,
                cornerRadius = cornerRadius,
                elevation = padding / 0.99f,
                centerX = centerX,
                centerY = centerY
            )
            val colorDrawable = createColorDrawable(
                backgroundColor = Color.DKGRAY,
                cornerRadius = cornerRadius
            )

            it.setColorShadowBackground(
                shadowDrawable = shadowDrawable,
                colorDrawable = colorDrawable,
                padding = 35
            )
            val endColors = intArrayOf(
                Color.YELLOW,
                Color.WHITE,
                Color.RED
            )
            animateShadow(
                shapeDrawable = shadowDrawable,
                startColors = colors,
                endColors = endColors,
                duration = 4000,
                centerX = centerX,
                centerY = centerY
            )
        }







        return view
    }




    //загрузка пользователей в ресайкл вью и запрос поиска
private fun loadUser(userName: String? = null) {
    //запрос по поиску


    val quere = database.child(NODE_USERS)
        .orderByChild("username")
        .startAt(userName)
        .endAt(userName + "\uf8ff")
    mRecyclerView.layoutManager = LinearLayoutManager(context)
    mAdapter =
        UserListAdapter(this)


    if (!userName.isNullOrBlank()) {

        //Появление ресайкл вью и удаление текста
        textSearchFirst.visibility = View.GONE
        textSearchSecond.visibility = View.GONE
        mRecyclerView.visibility = View.VISIBLE
        notResultTestView.visibility =View.GONE

       quere.addListenerForSingleValueEvent(Utils.ValueEventListenerAdapter {
                val users = it.children.map {
                    it.getValue(User::class.java)!!
                }

                //что бы в поиски не высвечивался текущий аккаунт

                if(users.isEmpty()){
                    textSearchFirst.visibility = View.GONE
                    textSearchSecond.visibility = View.GONE
                    mRecyclerView.visibility = View.GONE

                    notResultTestView.visibility = View.VISIBLE
                    Log.d("hi", users.toString())
                }else{
                    Log.d("hi", users.toString())
                    mUser = users.first()
                    mUsers = users
                    mRecyclerView.adapter = mAdapter
                    mAdapter.updateDataSet(users, mUser.followers)
                    notResultTestView.visibility = View.GONE
                }

                //userList += users


            })
    }




    }

    override fun follow(uid: String) {
        setFollow(uid, true) {
            mAdapter.followed(uid)
            database.child(NODE_FOLLOWING).child(currentUid()!!).child(uid).child(CHILD_UID).setValue(uid)
            database.child(NODE_FOLLOWERS).child(uid).child(currentUid()!!).child(CHILD_UID).setValue(currentUid()!!)
        }
    }

    override fun unfollow(uid: String) {
        setFollow(uid, false) {
            mAdapter.unfollowed(uid)
            database.child(NODE_FOLLOWING).child(currentUid()!!).child(uid).child(CHILD_UID).removeValue()
            database.child(NODE_FOLLOWERS).child(uid).child(currentUid()!!).child(CHILD_UID).removeValue()

        }
    }

    override fun click(uid: String) {
        if(uid == currentUid()!!){
            val bottomSheet = UserProfileFragment()
            bottomSheet.show(requireFragmentManager(), "")
        }else{
            val bottomSheet = OtherUserProfileFragment(uid)
            bottomSheet.show(requireFragmentManager(), "")
        }
    }

    //открыть боттом меню во весь рост
    override fun onStart() {
        super.onStart()
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }


    //Функция подписки и отписки

}





