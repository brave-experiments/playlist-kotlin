package com.brave.braveandroidplaylist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.model.PlaylistOptionsModel

class PlaylistOptionsBottomSheetAdapter(itemList: MutableList<PlaylistOptionsModel>) :
    AbstractRecyclerViewAdapter<PlaylistOptionsBottomSheetAdapter.PlaylistOptionsViewHolder, PlaylistOptionsModel>(
        itemList
    ) {

    class PlaylistOptionsViewHolder(view: View) :
        AbstractRecyclerViewAdapter.AbstractViewHolder<PlaylistOptionsModel>(view) {
        private val ivOptionIcon: AppCompatImageView
        private val tvOptionTitle: AppCompatTextView

        init {
            ivOptionIcon = view.findViewById(R.id.ivOptionIcon)
            tvOptionTitle = view.findViewById(R.id.tvOptionTitle)
        }

        override fun onBind(position: Int, model: PlaylistOptionsModel) {
            ivOptionIcon.setImageResource(model.optionIcon)
            tvOptionTitle.text = model.optionTitle
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistOptionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_options_bottom_sheet, parent, false)
        return PlaylistOptionsViewHolder(view)
    }
}