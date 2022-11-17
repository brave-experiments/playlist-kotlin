package com.brave.braveandroidplaylist.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.braveandroidplaylist.R
import com.brave.braveandroidplaylist.activity.PlaylistPlayerActivity
import com.brave.braveandroidplaylist.extension.sizeStrInMb
import com.brave.braveandroidplaylist.listener.OnStartDragListener
import com.brave.braveandroidplaylist.model.MediaModel
import com.brave.braveandroidplaylist.util.MediaUtils
import com.brave.braveandroidplaylist.util.PlaylistUtils
import java.io.File

class MediaItemAdapter(
    mediaItemList: MutableList<MediaModel>,
    private val onStartDragListener: OnStartDragListener
) :
    AbstractRecyclerViewAdapter<MediaItemAdapter.MediaItemViewHolder, MediaModel>(mediaItemList) {

    private var editMode = false

    fun setEditMode(enable: Boolean) {
        editMode = enable
        itemList.forEach { it.isSelected = false }
        notifyItemRangeChanged(0, size)
    }

    inner class MediaItemViewHolder(view: View) :
        AbstractViewHolder<MediaModel>(view) {
        private val ivMediaThumbnail: AppCompatImageView
        private val tvMediaTitle: AppCompatTextView
        private val tvMediaDuration: AppCompatTextView
        private val tvMediaFileSize: AppCompatTextView
        private val ivDragMedia: AppCompatImageView
        private val ivMediaSelected: AppCompatImageView

        init {
            ivMediaThumbnail = view.findViewById(R.id.ivMediaThumbnail)
            tvMediaTitle = view.findViewById(R.id.tvMediaTitle)
            tvMediaDuration = view.findViewById(R.id.tvMediaDuration)
            tvMediaFileSize = view.findViewById(R.id.tvMediaFileSize)
            ivDragMedia = view.findViewById(R.id.ivDragMedia)
            ivMediaSelected = view.findViewById(R.id.ivMediaSelected)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBind(position: Int, model: MediaModel) {
            setViewOnSelected(model.isSelected)
            tvMediaTitle.text = model.name
//            tvMediaFileSize.text = File(model.mediaPath).sizeStrInMb()
//            tvMediaDuration.text = MediaUtils.getMediaDuration(itemView.context ,model.mediaPath).toString()
            Log.e("BravePlaylist", model.name);
            ivDragMedia.visibility = if (editMode) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                if (editMode) {
                    model.isSelected = !model.isSelected
                    setViewOnSelected(model.isSelected)
                } else {
                    PlaylistUtils.openPlaylistPlayer(itemView.context, model)
                }
            }
            ivDragMedia.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN)
                    onStartDragListener.onStartDrag(this)
                false
            }
        }

        override fun isSelected(position: Int): Boolean {
            return position in 0 until size && itemList[position].isSelected
        }

        private fun setViewOnSelected(isSelected: Boolean) {
            ivMediaSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
            itemView.setBackgroundColor(if (isSelected) itemView.context.getColor(R.color.selected_media) else Color.TRANSPARENT)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaItemViewHolder(view)
    }
}