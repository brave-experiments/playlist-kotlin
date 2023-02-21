package com.brave.playlist.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.listener.PlaylistClickListener
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.util.ConstantUtils
import com.brave.playlist.util.PlaylistUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class RecentlyPlayedPlaylistAdapter(playlists: MutableList<PlaylistModel>,private val  playlistClickListener : PlaylistClickListener?) :
    AbstractRecyclerViewAdapter<RecentlyPlayedPlaylistAdapter.RecentlyPlayedPlaylistViewHolder, PlaylistModel>(
        playlists
    ) {

    class RecentlyPlayedPlaylistViewHolder(
        view: View,
        private val playlistClickListener: PlaylistClickListener?
    ) :
        AbstractViewHolder<PlaylistModel>(view) {
        private val ivPlaylistCover: AppCompatImageView
        private val tvPlaylistName: AppCompatTextView
        private val tvPlaylistItemCount: AppCompatTextView

        init {
            ivPlaylistCover = view.findViewById(R.id.ivPlaylistCover)
            tvPlaylistName = view.findViewById(R.id.tvPlaylistName)
            tvPlaylistItemCount = view.findViewById(R.id.tvPlaylistItemCount)
        }
        override fun onBind(position: Int, model: PlaylistModel) {
//            ivPlaylistCover.setImageResource(model)
            if (!model.items.isNullOrEmpty() && !model.items[0].thumbnailPath.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .asBitmap()
                    .placeholder(R.drawable.ic_playlist_item_placeholder)
                    .error(R.drawable.ic_playlist_item_placeholder)
                    .load(model.items[0].thumbnailPath)
                    .into(ivPlaylistCover)
            } else {
                ivPlaylistCover.setImageResource(R.drawable.ic_playlist_item_placeholder)
            }
            tvPlaylistName.text = if (model.id == ConstantUtils.DEFAULT_PLAYLIST) itemView.context.resources.getString(R.string.playlist_play_later) else model.name
            tvPlaylistItemCount.text = itemView.context.getString(R.string.playlist_number_items, model.items.size)
            itemView.setOnClickListener {
                playlistClickListener?.onPlaylistClick(model)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecentlyPlayedPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recently_played_playlist, parent, false)
        return RecentlyPlayedPlaylistViewHolder(view, playlistClickListener)
    }
}