package com.brave.playlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.model.PlaylistModel

class PlaylistAdapter(private val allPlaylists: MutableList<PlaylistModel>) :
    AbstractRecyclerViewAdapter<PlaylistAdapter.AllPlaylistViewHolder, PlaylistModel>(allPlaylists) {

    class AllPlaylistViewHolder(view: View) :
        AbstractRecyclerViewAdapter.AbstractViewHolder<PlaylistModel>(view) {
        private val ivPlaylistThumbnail: AppCompatImageView
        private val tvPlaylistTitle: AppCompatTextView
        private val tvPlaylistItemCount: AppCompatTextView

        init {
            ivPlaylistThumbnail = view.findViewById(R.id.ivPlaylistThumbnail)
            tvPlaylistTitle = view.findViewById(R.id.tvPlaylistTitle)
            tvPlaylistItemCount = view.findViewById(R.id.tvPlaylistItemCount)
        }
        override fun onBind(position: Int, model: PlaylistModel) {
//            ivPlaylistThumbnail.setImageResource(model.id)
            tvPlaylistTitle.text = model.name
            tvPlaylistItemCount.text = itemView.context.getString(R.string.number_items, model.items.size)
            itemView.setOnClickListener {
//                itemView.context.startActivity(Intent(itemView.context, PlaylistActivity::class.java))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return AllPlaylistViewHolder(view)
    }
}