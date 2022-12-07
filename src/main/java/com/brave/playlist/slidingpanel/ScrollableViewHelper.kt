package com.brave.playlist.slidingpanel

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ScrollableViewHelper {
    /**
     * Returns the current scroll position of the scrollable view. If this method returns zero or
     * less, it means at the scrollable view is in a position such as the panel should handle
     * scrolling. If the method returns anything above zero, then the panel will let the scrollable
     * view handle the scrolling
     *
     * @param scrollableView the scrollable view
     * @param isSlidingUp whether or not the panel is sliding up or down
     * @return the scroll position
     */
    fun getScrollableViewScrollPosition(scrollableView: View?, isSlidingUp: Boolean): Int {
        if (scrollableView == null) return 0
        return if (scrollableView is RecyclerView && scrollableView.childCount > 0) {
            val rv: RecyclerView = scrollableView
            val lm: RecyclerView.LayoutManager? = rv.layoutManager
            if (rv.adapter == null) return 0
            if (isSlidingUp) {
                val firstChild: View = rv.getChildAt(0)
                // Approximate the scroll position based on the top child and the first visible item
                rv.getChildLayoutPosition(firstChild) * lm!!.getDecoratedMeasuredHeight(firstChild) - lm.getDecoratedTop(
                    firstChild
                )
            } else {
                val lastChild: View = rv.getChildAt(rv.childCount - 1)
                // Approximate the scroll position based on the bottom child and the last visible item
                (rv.adapter!!
                    .itemCount - 1) * lm!!.getDecoratedMeasuredHeight(lastChild) + lm.getDecoratedBottom(
                    lastChild
                ) - rv.bottom
            }
        } else {
            0
        }
    }
}