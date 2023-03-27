package org.robotics.blinkworld.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shulz.galleryapp.data.GalleryItem
import org.robotics.blinkworld.databinding.ItemGalleryBinding

class GalleryListAdapter(
    private val dataList: List<GalleryItem>,
    private val onClick: (GalleryItem) -> Unit
) :
    RecyclerView.Adapter<GalleryListAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(private val binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GalleryItem) {
            Glide.with(binding.root.context).load(item.url).into(binding.image)

            binding.time.isVisible = item.isVideo
            binding.time.text = convertMillis(item.videoDuration)

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemGalleryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    fun convertMillis(millis: Long): String {
        val s = millis / 1000 % 60
        val m = millis / (1000 * 60) % 60
        //val h = millis / (1000 * 60 * 60) % 24
        return String.format("%02d:%02d", m, s)
    }
}