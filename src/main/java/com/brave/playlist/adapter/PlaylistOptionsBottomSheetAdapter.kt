package com.brave.playlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.model.PlaylistOptionsModel

class PlaylistOptionsBottomSheetAdapter(
    itemList: MutableList<PlaylistOptionsModel>,
    private val playlistOptionsListener: PlaylistOptionsListener
) :
    AbstractRecyclerViewAdapter<PlaylistOptionsBottomSheetAdapter.PlaylistOptionsViewHolder, PlaylistOptionsModel>(
        itemList
    ) {

    class PlaylistOptionsViewHolder(
        view: View,
        private val playlistOptionsListener: PlaylistOptionsListener
    ) :
        AbstractViewHolder<PlaylistOptionsModel>(view) {
        private val optionView: View
        private val ivOptionIcon: AppCompatImageView
        private val tvOptionTitle: AppCompatTextView

        init {
            optionView = view
            ivOptionIcon = view.findViewById(R.id.ivOptionIcon)
            tvOptionTitle = view.findViewById(R.id.tvOptionTitle)
        }

        override fun onBind(position: Int, model: PlaylistOptionsModel) {
            ivOptionIcon.setImageResource(model.optionIcon)
            tvOptionTitle.text = model.optionTitle
            optionView.setOnClickListener {
                playlistOptionsListener.onOptionClicked(model)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistOptionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_options_bottom_sheet, parent, false)
        return PlaylistOptionsViewHolder(view, playlistOptionsListener)
    }
}