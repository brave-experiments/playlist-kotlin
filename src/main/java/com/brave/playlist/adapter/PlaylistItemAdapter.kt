package com.brave.playlist.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.brave.playlist.R
import com.brave.playlist.listener.PlaylistItemClickListener
import com.brave.playlist.listener.StartDragListener
import com.brave.playlist.model.DownloadProgressModel
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.util.MediaUtils
import com.brave.playlist.util.PlaylistUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class PlaylistItemAdapter(
    mediaItemList: MutableList<PlaylistItemModel>,
    private val playlistItemClickListener: PlaylistItemClickListener?,
    private val startDragListener: StartDragListener? = null,
) :
    AbstractRecyclerViewAdapter<PlaylistItemAdapter.MediaItemViewHolder, PlaylistItemModel>(
        mediaItemList
    ) {

    private var editMode = false
    private var isBottomLayout = false

    private var allViewHolderViews = HashMap<String, View>()
    fun updatePlaylistItemDownloadProgress(downloadProgressModel: DownloadProgressModel) {
        val view = allViewHolderViews[downloadProgressModel.id]
        val ivMediaStatus: AppCompatImageView? = view?.findViewById(R.id.ivMediaStatus)
        val tvMediaDownloadProgress: AppCompatTextView? =
            view?.findViewById(R.id.tvMediaDownloadProgress)
        if (downloadProgressModel.totalBytes == downloadProgressModel.receivedBytes) {
            tvMediaDownloadProgress?.visibility = View.GONE
            ivMediaStatus?.visibility = View.VISIBLE
            ivMediaStatus?.setImageResource(R.drawable.ic_downloaded)
        } else {
            ivMediaStatus?.visibility = View.GONE
            tvMediaDownloadProgress?.visibility = View.VISIBLE
            tvMediaDownloadProgress?.text = view?.resources?.getString(R.string.percentage_text)
                ?.let { String.format(it, downloadProgressModel.percentComplete.toString()) }
        }
    }

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
        private val ivMediaOptions: AppCompatImageView
        private val ivMediaSelected: AppCompatImageView
        private val ivMediaStatus: AppCompatImageView
        private val tvMediaDownloadProgress: AppCompatTextView

        init {
            ivMediaThumbnail = view.findViewById(R.id.ivMediaThumbnail)
            tvMediaTitle = view.findViewById(R.id.tvMediaTitle)
            tvMediaDuration = view.findViewById(R.id.tvMediaDuration)
            tvMediaFileSize = view.findViewById(R.id.tvMediaFileSize)
            ivDragMedia = view.findViewById(R.id.ivDragMedia)
            ivMediaOptions = view.findViewById(R.id.ivMediaOptions)
            ivMediaSelected = view.findViewById(R.id.ivMediaSelected)
            ivMediaStatus = view.findViewById(R.id.ivMediaStatus)
            tvMediaDownloadProgress = view.findViewById(R.id.tvMediaDownloadProgress)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBind(position: Int, model: PlaylistItemModel) {
            setViewOnSelected(model.isSelected)
            ivMediaStatus.setImageResource(if (model.isCached) R.drawable.ic_downloaded else R.drawable.ic_offline)
            ivMediaStatus.visibility = if (!editMode) View.VISIBLE else View.GONE
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

            if (!model.thumbnailPath.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .asBitmap()
                    .placeholder(R.drawable.ic_playlist_item_placeholder)
                    .error(R.drawable.ic_playlist_item_placeholder)
                    .load(model.thumbnailPath)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            ivMediaThumbnail.setImageBitmap(
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
                ivMediaThumbnail.setImageResource(R.drawable.ic_playlist_item_placeholder)
            }

//            if (model.isCached) {
//                val fileSize = MediaUtils.getFileSizeFromUri(itemView.context, Uri.parse(model.mediaPath))
//                tvMediaFileSize.text =
//                    Formatter.formatShortFileSize(itemView.context, fileSize)
//            }

            tvMediaFileSize.visibility = if (model.isCached) View.VISIBLE else View.GONE

            if (!model.duration.isNullOrEmpty()) {
                val duration = model.duration.toLongOrNull()
                if (duration != null) {
                    val milliseconds = (duration / 1000) % 1000
                    val seconds = ((duration / 1000) - milliseconds) / 1000 % 60
                    val minutes =
                        (((duration / 1000) - milliseconds) / 1000 - seconds) / 60 % 60
                    val hours =
                        ((((duration / 1000) - milliseconds) / 1000 - seconds) / 60 - minutes) / 60

                    val hourTime : String = if (hours > 0) itemView.context.resources.getString(R.string.time_text , hours.toString()) else ""
                    val minuteTime : String = if (minutes > 0) itemView.context.resources.getString(R.string.time_text , minutes.toString()) else ""
                    tvMediaDuration.text =  itemView.context.resources.getString(R.string.duration_text , hourTime, minuteTime, seconds.toString())
                }
            }
            ivMediaOptions.visibility = if (!editMode) View.VISIBLE else View.GONE
            ivMediaOptions.setOnClickListener {
                playlistItemClickListener?.onPlaylistItemMenuClick(
                    view = it,
                    playlistItemModel = model
                )
            }
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
                    if (isBottomLayout) playlistItemClickListener?.onPlaylistItemClick(position) else playlistItemClickListener?.onPlaylistItemClick(
                        playlistItemModel = model
                    )
                }
            }
            ivDragMedia.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN)
                    startDragListener?.onStartDrag(this)
                false
            }

            allViewHolderViews[model.id] = itemView
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

    fun getPlaylistItems(): MutableList<PlaylistItemModel> {
        return itemList
    }
}
