package org.robotics.blinkworld.BottomFragmaent.Gallery.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shulz.galleryapp.data.GalleryItem
import org.robotics.blinkworld.Adapter.GalleryListAdapter
import org.robotics.blinkworld.databinding.BottomSheetChooseDataBinding

class ChooseDataBottomSheet(val dataList: List<GalleryItem>, val onClick: (GalleryItem) -> Unit) :
    BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetChooseDataBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetChooseDataBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dataList.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = GalleryListAdapter(dataList) {
                onClick(it)
                dismiss()
            }
        }
    }
}