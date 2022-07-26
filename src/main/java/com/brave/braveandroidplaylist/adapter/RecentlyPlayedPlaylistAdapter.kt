package com.brave.braveandroidplaylist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.model.PlaylistModel

class RecentlyPlayedPlaylistAdapter(private val playlists: MutableList<PlaylistModel>) :
    AbstractRecyclerViewAdapter<RecentlyPlayedPlaylistAdapter.RecentlyPlayedPlaylistViewHolder, PlaylistModel>(
        playlists
    ) {

    class RecentlyPlayedPlaylistViewHolder(view: View) :
        AbstractRecyclerViewAdapter.AbstractViewHolder<PlaylistModel>(view) {
        private val ivPlaylistCover: AppCompatImageView
        private val tvPlaylistName: AppCompatTextView
        private val tvPlaylistItemCount: AppCompatTextView

        init {
            ivPlaylistCover = view.findViewById(R.id.ivPlaylistCover)
            tvPlaylistName = view.findViewById(R.id.tvPlaylistName)
            tvPlaylistItemCount = view.findViewById(R.id.tvPlaylistItemCount)
        }
        override fun onBind(position: Int, model: PlaylistModel) {
            ivPlaylistCover.setImageResource(model.cover)
            tvPlaylistName.text = model.title
            tvPlaylistItemCount.text = itemView.context.getString(R.string.number_items, model.totalItems)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentlyPlayedPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recently_played_playlist, parent, false)
        return RecentlyPlayedPlaylistViewHolder(view)
    }
}