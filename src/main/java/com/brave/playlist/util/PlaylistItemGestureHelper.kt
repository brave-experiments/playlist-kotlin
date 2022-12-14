package com.brave.playlist.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.R
import com.brave.playlist.adapter.AbstractRecyclerViewAdapter
import com.brave.playlist.listener.OnItemInteractionListener
import kotlin.math.min


@SuppressLint("ClickableViewAccessibility")
class PlaylistItemGestureHelper<VH : AbstractRecyclerViewAdapter.AbstractViewHolder<M>, M>(
    context: Context,
    private val recyclerView: RecyclerView,
    private val adapter: AbstractRecyclerViewAdapter<VH, M>,
    private val onItemInteractionListener: OnItemInteractionListener
) :
    SimpleCallback(
        UP or DOWN,
        START or END
    ), RecyclerView.OnItemTouchListener {

    private val deleteIcon: Drawable?
    private val uploadIcon: Drawable?
    private val removeOfflineIcon: Drawable?
    private val deleteIconBg: Drawable
    private val uploadIconBg: Drawable
    private val removeOfflineIconBg: Drawable
    private val buttonPositions: MutableMap<Int, List<OptionButton>> = mutableMapOf()
    private val gestureDetector: GestureDetector
    private var swipePosition = -1
    private var oldSwipePosition = -1

    private val gestureListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (swipePosition != -1) {
                for (button in buttonPositions[swipePosition]!!)
                    if (button.handleTouch(e))
                        return true
            }
            return false
        }
    }

    init {
        deleteIcon = AppCompatResources.getDrawable(context, R.drawable.ic_playlist_delete)
        uploadIcon =
            AppCompatResources.getDrawable(context, R.drawable.ic_upload_media)
        removeOfflineIcon =
            AppCompatResources.getDrawable(context, R.drawable.ic_remove_offline_data_playlist)
        deleteIconBg = ColorDrawable(context.getColor(R.color.swipe_delete))
        uploadIconBg = ColorDrawable(context.getColor(R.color.upload_option_bg))
        removeOfflineIconBg = ColorDrawable(context.getColor(R.color.remove_offline_option_bg))
        gestureDetector = GestureDetector(context, gestureListener)
        recyclerView.addOnItemTouchListener(this)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        adapter.swap(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (direction == START) {
            if (viewHolder.adapterPosition == oldSwipePosition)
                oldSwipePosition = -1
            if (viewHolder.adapterPosition == swipePosition)
                swipePosition = -1
            buttonPositions.remove(viewHolder.adapterPosition)
            adapter.removeAt(viewHolder.adapterPosition)
            onItemInteractionListener.onItemDelete(viewHolder.adapterPosition)
        } else if (direction == END)
            oldSwipePosition = swipePosition
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        var newDX = dX
        if (dX < 0)
            onSwipeLeft(viewHolder, dX, c)
        else if (dX > 0) {
            newDX = min(onSwipeRight(viewHolder, dX, c), dX)
            swipePosition = viewHolder.adapterPosition
        } else {
            swipePosition = -1
            resetButtons(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, newDX, dY, actionState, isCurrentlyActive)
    }

    private fun resetButtons(c: Canvas) {
        resetDrawableBounds(deleteIconBg, c)
        resetDrawableBounds(uploadIconBg, c)
        resetDrawableBounds(removeOfflineIconBg, c)
    }

    private fun resetDrawableBounds(drawable: Drawable, c: Canvas) {
        drawable.setBounds(0, 0, 0, 0)
        drawable.draw(c)
    }

    private fun onSwipeRight(viewHolder: RecyclerView.ViewHolder, dX: Float, c: Canvas): Float {
        if (uploadIcon == null || removeOfflineIcon == null)
            return 0f

        val itemView = viewHolder.itemView

        val rightBound = itemView.left + dX.toInt()

        val offlineIconMargin = (itemView.height - removeOfflineIcon.intrinsicHeight) / 2
        val offlineIconTop =
            itemView.top + (itemView.height - removeOfflineIcon.intrinsicHeight) / 2
        val offlineIconBottom = offlineIconTop + removeOfflineIcon.intrinsicHeight
        val offlineIconLeft = itemView.left + offlineIconMargin
        val offlineIconRight = offlineIconLeft + removeOfflineIcon.intrinsicWidth

        if (rightBound >= offlineIconRight)
            removeOfflineIcon.setBounds(
                offlineIconLeft,
                offlineIconTop,
                offlineIconRight,
                offlineIconBottom
            )
        else
            removeOfflineIcon.setBounds(0, 0, 0, 0)

        if (!buttonPositions.containsKey(viewHolder.adapterPosition))
            buttonPositions[viewHolder.adapterPosition] =
                instantiateOptions(viewHolder.adapterPosition)


        buttonPositions[viewHolder.adapterPosition]!![0].viewRect = Rect(
            itemView.left,
            itemView.top,
            min(rightBound, offlineIconRight + offlineIconMargin),
            itemView.bottom
        )

        removeOfflineIconBg.bounds = buttonPositions[viewHolder.adapterPosition]!![0].viewRect!!

        removeOfflineIconBg.draw(c)
        removeOfflineIcon.draw(c)

        val uploadIconMargin = (itemView.height - uploadIcon.intrinsicHeight) / 2
        val uploadIconTop = itemView.top + (itemView.height - uploadIcon.intrinsicHeight) / 2
        val uploadIconBottom = uploadIconTop + uploadIcon.intrinsicHeight
        val uploadIconLeft = offlineIconRight + offlineIconMargin + uploadIconMargin
        val uploadIconRight = uploadIconLeft + uploadIcon.intrinsicWidth

        if (rightBound >= uploadIconRight)
            uploadIcon.setBounds(uploadIconLeft, uploadIconTop, uploadIconRight, uploadIconBottom)
        else
            uploadIcon.setBounds(0, 0, 0, 0)

        if (rightBound >= offlineIconRight + offlineIconMargin) {
            buttonPositions[viewHolder.adapterPosition]!![1].viewRect = Rect(
                offlineIconRight + offlineIconMargin,
                itemView.top,
                min(rightBound, uploadIconRight + uploadIconMargin),
                itemView.bottom
            )
            uploadIconBg.bounds = buttonPositions[viewHolder.adapterPosition]!![1].viewRect!!
        } else
            uploadIconBg.setBounds(0, 0, 0, 0)

        uploadIconBg.draw(c)
        uploadIcon.draw(c)

        return (uploadIconRight + uploadIconMargin).toFloat()
    }

    private fun onSwipeLeft(viewHolder: RecyclerView.ViewHolder, dX: Float, c: Canvas) {
        val itemView = viewHolder.itemView
        if (deleteIcon == null)
            return

        val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
        val iconBottom = iconTop + deleteIcon.intrinsicHeight
        val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
        val iconRight = itemView.right - iconMargin
        val leftBound = itemView.right + dX.toInt()

        if (leftBound <= iconLeft)
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        else
            deleteIcon.setBounds(0, 0, 0, 0)

        deleteIconBg.setBounds(leftBound, itemView.top, itemView.right, itemView.bottom)

        deleteIconBg.draw(c)
        deleteIcon.draw(c)
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int
    ) {
        super.onSelectedChanged(viewHolder, actionState)


        if (viewHolder is AbstractRecyclerViewAdapter.AbstractViewHolder<*> && !viewHolder.isSelected(viewHolder.adapterPosition)) {
            viewHolder.itemView.setBackgroundResource(R.color.playlist_background)
        }
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is AbstractRecyclerViewAdapter.AbstractViewHolder<*> && !viewHolder.isSelected(viewHolder.adapterPosition))
            viewHolder.itemView.background = null
    }

    override fun isLongPressDragEnabled(): Boolean = false

    private fun instantiateOptions(position: Int): List<OptionButton> =
        listOf(
            OptionButton(position, onItemInteractionListener::onRemoveFromOffline),
            OptionButton(position, onItemInteractionListener::onUpload)
        )

    inner class OptionButton(
        private val adapterPosition: Int,
        private val click: (position: Int) -> Unit
    ) {
        var viewRect: Rect? = null

        private fun onClick() = click(adapterPosition)

        fun handleTouch(event: MotionEvent): Boolean {
            return viewRect?.contains(event.x.toInt(), event.y.toInt())?.let {
                if (it) {
                    resetSwipedView()
                    onClick()
                }
                it
            } ?: false
        }
    }

    private fun resetSwipedView() {
        adapter.notifyItemChanged(swipePosition)
        swipePosition = -1
        oldSwipePosition = -1
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN && swipePosition != -1) {
            recyclerView.findViewHolderForAdapterPosition(swipePosition)?.itemView?.let {
                val rect = Rect(it.left, it.top, it.right, it.bottom)
                if (!rect.contains(e.x.toInt(), e.y.toInt()))
                    resetSwipedView()
            }
        }
        return gestureDetector.onTouchEvent(e)
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
}