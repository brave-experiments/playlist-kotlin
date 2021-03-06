package com.brave.braveandroidplaylist.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.activity.PlaylistActivity
import com.brave.braveandroidplaylist.model.PlaylistModel

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
            ivPlaylistThumbnail.setImageResource(model.cover)
            tvPlaylistTitle.text = model.title
            tvPlaylistItemCount.text = itemView.context.getString(R.string.number_items, model.totalItems)
            itemView.setOnClickListener {
                itemView.context.startActivity(Intent(itemView.context, PlaylistActivity::class.java))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return AllPlaylistViewHolder(view)
    }
}