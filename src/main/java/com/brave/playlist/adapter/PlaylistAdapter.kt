package com.brave.playlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.listener.PlaylistClickListener
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.util.ConstantUtils.DEFAULT_PLAYLIST
import com.bumptech.glide.Glide

class PlaylistAdapter(
    allPlaylists: MutableList<PlaylistModel>,
    private val playlistClickListener: PlaylistClickListener?
) :
    AbstractRecyclerViewAdapter<PlaylistAdapter.AllPlaylistViewHolder, PlaylistModel>(allPlaylists) {

    inner class AllPlaylistViewHolder(view: View) :
        AbstractViewHolder<PlaylistModel>(view) {
        private val ivPlaylistThumbnail: AppCompatImageView
        private val ivNewPlaylistThumbnail: AppCompatImageView
        private val tvPlaylistTitle: AppCompatTextView
        private val tvPlaylistItemCount: AppCompatTextView

        init {
            ivPlaylistThumbnail = view.findViewById(R.id.ivPlaylistThumbnail)
            ivNewPlaylistThumbnail = view.findViewById(R.id.ivNewPlaylistThumbnail)
            tvPlaylistTitle = view.findViewById(R.id.tvPlaylistTitle)
            tvPlaylistItemCount = view.findViewById(R.id.tvPlaylistItemCount)
        }

        override fun onBind(position: Int, model: PlaylistModel) {
            if (model.id == "new_playlist") {
                tvPlaylistItemCount.visibility = View.GONE
                ivNewPlaylistThumbnail.visibility = View.VISIBLE
                ivPlaylistThumbnail.visibility = View.GONE
            } else {
                if (!model.items.isNullOrEmpty() && !model.items[0].thumbnailPath.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .asBitmap()
                        .placeholder(R.drawable.ic_playlist_item_placeholder)
                        .error(R.drawable.ic_playlist_item_placeholder)
                        .load(model.items[0].thumbnailPath)
                        .into(ivPlaylistThumbnail)
                } else {
                    ivPlaylistThumbnail.setImageResource(R.drawable.ic_playlist_item_placeholder)
                }

                tvPlaylistItemCount.text =
                    itemView.context.getString(R.string.playlist_number_items, model.items.size)
            }
            tvPlaylistTitle.text =
                if (model.id == DEFAULT_PLAYLIST) itemView.context.resources.getString(R.string.playlist_play_later) else model.name
            itemView.setOnClickListener {
                playlistClickListener?.onPlaylistClick(model)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return AllPlaylistViewHolder(view)
    }
}