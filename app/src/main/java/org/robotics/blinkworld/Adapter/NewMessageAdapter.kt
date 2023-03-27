package org.robotics.blinkworld.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_chats.view.*
import kotlinx.android.synthetic.main.new_chat_item.view.*
import org.robotics.blinkworld.R
import org.robotics.blinkworld.Utils.fromUrl
import org.robotics.blinkworld.Utils.loadUserPhoto
import org.robotics.blinkworld.databinding.ItemChatsBinding
import org.robotics.blinkworld.databinding.NewChatItemBinding
import org.robotics.blinkworld.models.Chat
import org.robotics.blinkworld.models.User

class NewMessageAdapter(private val onClick: (User) -> Unit): RecyclerView.Adapter<NewMessageAdapter.NewChatsViewHolder>() {

    private val dataSet: MutableList<User> = mutableListOf()




    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NewMessageAdapter.NewChatsViewHolder {

        return NewChatsViewHolder(NewChatItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: NewMessageAdapter.NewChatsViewHolder, position: Int) {
        holder.bind(dataSet[position])


    }

    override fun getItemCount(): Int =dataSet.size


    inner class NewChatsViewHolder(private val binding: NewChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userItem: User) {
            binding.imageNewChat.loadUserPhoto(dataSet[position].photo)
            binding.usernameNewChat.text = dataSet[position].username
            binding.imageNewChat.clipToOutline = true

            //что бы закруглить углы картинки
            binding.imageNewChat.clipToOutline = true

            binding.root.setOnClickListener {
                onClick.invoke(userItem)
            }
        }
    }


    fun updateDataSet(dataSet: List<User>) {
        val diffCallback = DiffCallback(this.dataSet, dataSet)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        diffResult.dispatchUpdatesTo(this)
    }




    class DiffCallback(
        private val oldDataSet: MutableList<User>,
        private val newDataSet: List<User>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldDataSet.size
        override fun getNewListSize(): Int = newDataSet.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldDataSet[oldItemPosition].uid == newDataSet[newItemPosition].uid

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldDataSet[oldItemPosition] == newDataSet[newItemPosition]
    }
}