package com.brave.playlist.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.listener.OnPlaylistListener
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.util.PlaylistUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class PlaylistAdapter(allPlaylists: MutableList<PlaylistModel>, private val onPlaylistListener : OnPlaylistListener?) :
    AbstractRecyclerViewAdapter<PlaylistAdapter.AllPlaylistViewHolder, PlaylistModel>(allPlaylists) {

    inner class AllPlaylistViewHolder(view: View) :
        AbstractViewHolder<PlaylistModel>(view) {
        private val ivPlaylistThumbnail: AppCompatImageView
        private val tvPlaylistTitle: AppCompatTextView
        private val tvPlaylistItemCount: AppCompatTextView

        init {
            ivPlaylistThumbnail = view.findViewById(R.id.ivPlaylistThumbnail)
            tvPlaylistTitle = view.findViewById(R.id.tvPlaylistTitle)
            tvPlaylistItemCount = view.findViewById(R.id.tvPlaylistItemCount)
        }

        override fun onBind(position: Int, model: PlaylistModel) {
            if (model.items.isNotEmpty()) {
                Glide.with(itemView.context)
                    .asBitmap()
                    .placeholder(R.drawable.ic_playlist_item_placeholder)
                    .error(R.drawable.ic_playlist_item_placeholder)
                    .load(model.items[0].thumbnailPath)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            ivPlaylistThumbnail.setImageBitmap(
                                PlaylistUtils.getRoundedCornerBitmap(
                                    resource
                                )
                            )
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }
                    })
            } else {
                ivPlaylistThumbnail.setImageResource(R.drawable.ic_playlist_item_placeholder)
            }

            tvPlaylistTitle.text =
                if (model.id == "default") itemView.context.resources.getString(R.string.watch_later) else model.name
            tvPlaylistItemCount.text =
                itemView.context.getString(R.string.number_items, model.items.size)
            itemView.setOnClickListener {
                onPlaylistListener?.onPlaylistClick(model)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return AllPlaylistViewHolder(view)
    }
}