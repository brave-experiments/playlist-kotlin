package com.brave.playlist.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.extension.sizeStr
import com.brave.playlist.listener.PlaylistItemClickListener
import com.brave.playlist.listener.StartDragListener
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.util.PlaylistUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlaylistItemAdapter(
    mediaItemList: MutableList<PlaylistItemModel>,
    private val playlistItemClickListener: PlaylistItemClickListener?,
    private val startDragListener: StartDragListener? = null,
) :
    AbstractRecyclerViewAdapter<PlaylistItemAdapter.MediaItemViewHolder, PlaylistItemModel>(mediaItemList) {

    private var editMode = false
    private var isBottomLayout = false

    fun setEditMode(enable: Boolean) {
        editMode = enable
        itemList.forEach { it.isSelected = false }
        notifyItemRangeChanged(0, size)
    }

    fun getEditMode(): Boolean {
        return editMode
    }

    fun setBottomLayout() {
        isBottomLayout = true
    }

    inner class MediaItemViewHolder(view: View) :
        AbstractViewHolder<PlaylistItemModel>(view) {
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
        override fun onBind(position: Int, model: PlaylistItemModel) {
            setViewOnSelected(model.isSelected)
            tvMediaTitle.text = model.name
//            val thumbnailFile = File(model.thumbnailPath)
//            if (thumbnailFile.exists()) {
//                val myBitmap = BitmapFactory.decodeFile(thumbnailFile.absolutePath)
//                ivMediaThumbnail.setImageBitmap(myBitmap)
//            }
//            val requestManager: RequestManager = Glide.with(itemView.context)
//            val requestBuilder: RequestBuilder<*> = requestManager.load(model.thumbnailPath)
//            requestBuilder.into(ivMediaThumbnail)
//            Glide.with(itemView.context)
//                .load(File("https://www.gstatic.com/webp/gallery/1.webp"))
//            .into(ivMediaThumbnail)

            Glide.with(itemView.context)
                .asBitmap()
                .placeholder(R.drawable.ic_playlist_item_placeholder)
                .error(R.drawable.ic_playlist_item_placeholder)
                .load(model.thumbnailPath)
                .into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        ivMediaThumbnail.setImageBitmap(PlaylistUtils.getRoundedCornerBitmap(resource))
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })

            tvMediaFileSize.text = File(model.mediaPath).sizeStr()
            val df = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvMediaDuration.text = model.duration.toLongOrNull()
                ?.let { df.format(Date(TimeUnit.MICROSECONDS.toSeconds(it) * 1000L)) }.toString()
            Log.e("BravePlaylist", model.name)
            ivDragMedia.visibility = if (editMode) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                if (editMode) {
                    model.isSelected = !model.isSelected
                    setViewOnSelected(model.isSelected)
                    var count = 0
                    itemList.forEach {
                        if (it.isSelected) {
                            count++
                        }
                    }
                    playlistItemClickListener?.onPlaylistItemClick(count)
                } else {
                    if (isBottomLayout) playlistItemClickListener?.onPlaylistItemClick(position) else playlistItemClickListener?.onPlaylistItemClick(playlistItemModel = model)
                }
            }
            ivDragMedia.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN)
                    startDragListener?.onStartDrag(this)
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

    fun getSelectedItems(): ArrayList<PlaylistItemModel> {
        val selectedItems = arrayListOf<PlaylistItemModel>()
        itemList.forEach {
            if (it.isSelected) {
                selectedItems.add(it)
            }
        }
        return selectedItems
    }
}