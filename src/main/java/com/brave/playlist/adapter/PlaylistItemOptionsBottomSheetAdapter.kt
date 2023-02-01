package com.brave.playlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.listener.PlaylistItemOptionsListener
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.model.PlaylistItemOptionModel
import com.brave.playlist.model.PlaylistOptionsModel

class PlaylistItemOptionsBottomSheetAdapter(
    itemList: MutableList<PlaylistItemOptionModel>,
    private val playlistItemOptionsListener: PlaylistItemOptionsListener
) :
    AbstractRecyclerViewAdapter<PlaylistItemOptionsBottomSheetAdapter.PlaylistItemOptionsViewHolder, PlaylistItemOptionModel>(
        itemList
    ) {

    class PlaylistItemOptionsViewHolder(view: View, private val playlistItemOptionsListener: PlaylistItemOptionsListener) :
        AbstractViewHolder<PlaylistItemOptionModel>(view) {
        private val optionView: View
        private val ivOptionIcon: AppCompatImageView
        private val tvOptionTitle: AppCompatTextView

        init {
            optionView = view
            ivOptionIcon = view.findViewById(R.id.ivOptionIcon)
            tvOptionTitle = view.findViewById(R.id.tvOptionTitle)
        }

        override fun onBind(position: Int, model: PlaylistItemOptionModel) {
            ivOptionIcon.setImageResource(model.optionIcon)
            tvOptionTitle.text = model.optionTitle
            optionView.setOnClickListener {
                playlistItemOptionsListener.onOptionClicked(model)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistItemOptionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_options_bottom_sheet, parent, false)
        return PlaylistItemOptionsViewHolder(view, playlistItemOptionsListener)
    }
}