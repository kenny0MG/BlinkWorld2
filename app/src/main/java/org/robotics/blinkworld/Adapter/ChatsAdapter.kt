package org.robotics.blinkworld.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_chats.view.*
import org.robotics.blinkworld.Utils.fromUrl
import org.robotics.blinkworld.databinding.ItemChatsBinding
import org.robotics.blinkworld.models.Chat
import org.robotics.blinkworld.models.Message

class ChatsAdapter(private val onClick: (Chat) -> Unit): RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {
    private val dataSet: MutableList<Chat> = mutableListOf()


    fun updateDataSet(dataSet: List<Chat>) {
        val diffCallback = DiffCallback(this.dataSet, dataSet)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.dataSet.clear()
        this.dataSet.addAll(dataSet)
        diffResult.dispatchUpdatesTo(this)
    }


    class DiffCallback(
        private val oldDataSet: MutableList<Chat>,
        private val newDataSet: List<Chat>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldDataSet.size
        override fun getNewListSize(): Int = newDataSet.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldDataSet[oldItemPosition].id == newDataSet[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldDataSet[oldItemPosition] == newDataSet[newItemPosition]
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ChatsAdapter.ChatsViewHolder {

        return ChatsViewHolder(ItemChatsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ChatsAdapter.ChatsViewHolder, position: Int) {

        holder.bind(dataSet[position])

    }

    override fun getItemCount(): Int = dataSet.count()

    inner class ChatsViewHolder(private val binding: ItemChatsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatItem: Chat) {
            binding.usernameChat.text = chatItem.username
            binding.lastMessagesChat.text = chatItem.lastMsg
            binding.imageChat.fromUrl(chatItem.photo)
            if (!chatItem.read) {
                binding.circleNewMessage.visibility = View.VISIBLE
                binding.circleNewMessage.text = ""
                binding.lastMessagesChat.setTextColor(Color.parseColor("#0098FD"))
                //holder.itemLastMessage.typeface = Typeface.DEFAULT_BOLD


            } else {
                binding.circleNewMessage.visibility = View.GONE

            }


            //что бы закруглить углы картинки
            binding.imageChat.clipToOutline = true

            binding.root.setOnClickListener {
                onClick.invoke(chatItem)
                chatItem.read
            }
        }


    }




}