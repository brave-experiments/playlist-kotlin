package com.brave.playlist.slidingpanel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.view.View.OnClickListener
import android.view.accessibility.AccessibilityEvent
import android.view.animation.Interpolator
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import com.brave.playlist.R
import java.util.concurrent.CopyOnWriteArrayList

class BottomPanelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    ViewGroup(context, attrs, defStyle) {
    /**
     * @return The current minimin fling velocity
     */
    /**
     * Sets the minimum fling velocity for the panel
     *
     * @param val the new value
     */
    /**
     * Minimum velocity that will be detected as a fling
     */
    var minFlingVelocity = DEFAULT_MIN_FLING_VELOCITY

    /**
     * The fade color used for the panel covered by the slider. 0 = no fading.
     */
    private var mCoveredFadeColor = DEFAULT_FADE_COLOR

    /**
     * The paint used to dim the main layout when sliding
     */
    private val mCoveredFadePaint = Paint()

    /**
     * Drawable used to draw the shadow between panes.
     */
    private var mShadowDrawable: Drawable? = null

    /**
     * The size of the overhang in pixels.
     */
    private var mPanelHeight = -1

    /**
     * The size of the shadow in pixels.
     */
    private var mShadowHeight = -1

    /**
     * Parallax offset
     */
    private var mParallaxOffset = -1

    /**
     * True if the collapsed panel should be dragged up.
     */
    private var mIsSlidingUp = false
    /**
     * Check if the panel is set as an overlay.
     */
    /**
     * Sets whether or not the panel overlays the content
     *
     * @param overlayed
     */
    /**
     * Panel overlays the windows instead of putting it underneath it.
     */
    var isOverlayed = DEFAULT_OVERLAY_FLAG
    /**
     * Check whether or not the main content is clipped to the top of the panel
     */
    /**
     * Sets whether or not the main content is clipped to the top of the panel
     *
     * @param clip
     */
    /**
     * The main view is clipped to the main top border
     */
    var isClipPanel = DEFAULT_CLIP_PANEL_FLAG

    /**
     * If provided, the panel can be dragged by only this view. Otherwise, the entire panel can be
     * used for dragging.
     */
    private var mDragView: View? = null

    /**
     * If provided, the panel can be dragged by only this view. Otherwise, the entire panel can be
     * used for dragging.
     */
    private var mDragViewResId = -1

    /**
     * If provided, the panel will transfer the scroll from this view to itself when needed.
     */
    private var mScrollableView: View? = null
    private var mScrollableViewResId = 0
    private var mScrollableViewHelper = ScrollableViewHelper()

    /**
     * The child view that can slide, if any.
     */
    private var mSlideableView: View? = null

    /**
     * The main view
     */
    private var mMainView: View? = null

    /**
     * Current state of the slideable view.
     */
    enum class PanelState {
        EXPANDED, COLLAPSED, ANCHORED, HIDDEN, DRAGGING
    }

    var mSlideState: PanelState? = DEFAULT_SLIDE_STATE

    /**
     * If the current slide state is DRAGGING, this will store the last non dragging state
     */
    private var mLastNotDraggingSlideState: PanelState? = DEFAULT_SLIDE_STATE

    /**
     * How far the panel is offset from its expanded position.
     * range [0, 1] where 0 = collapsed, 1 = expanded.
     */
    private var mSlideOffset = 0f

    /**
     * How far in pixels the slideable panel may move.
     */
    private var mSlideRange = 0

    /**
     * An anchor point where the panel can stop during sliding
     */
    private var mAnchorPoint = 1f

    /**
     * A panel view is locked into internal scrolling or another condition that
     * is preventing a drag.
     */
    private var mIsUnableToDrag = false

    /**
     * Flag indicating that sliding feature is enabled\disabled
     */
    private var mIsTouchEnabled: Boolean
    private var mPrevMotionX = 0f
    private var mPrevMotionY = 0f
    private var mInitialMotionX = 0f
    private var mInitialMotionY = 0f
    private var mIsScrollableViewHandlingTouch = false
    private val mPanelSlideListeners: MutableList<PanelSlideListener> = CopyOnWriteArrayList()
    private var mFadeOnClickListener: OnClickListener? = null
    private var mDragHelper: ViewDragHelper?

    /**
     * Stores whether or not the pane was expanded the last time it was slideable.
     * If expand/collapse operations are invoked this state is modified. Used by
     * instance state save/restore.
     */
    private var mFirstLayout = true
    private val mTmpRect = Rect()

    /**
     * Listener for monitoring events about sliding panes.
     */
    interface PanelSlideListener {
        /**
         * Called when a sliding pane's position changes.
         *
         * @param panel       The child view that was moved
         * @param slideOffset The new offset of this sliding pane within its range, from 0-1
         */
        fun onPanelSlide(panel: View?, slideOffset: Float)

        /**
         * Called when a sliding panel state changes
         *
         * @param panel The child view that was slid to an collapsed position
         */
        fun onPanelStateChanged(panel: View?, previousState: PanelState?, newState: PanelState?)
    }

    init {
        var scrollerInterpolator: Interpolator? = null
        if (attrs != null) {
            val defAttrs = context.obtainStyledAttributes(attrs, DEFAULT_ATTRS)
            val gravity = defAttrs.getInt(0, Gravity.NO_GRAVITY)
            setGravity(gravity)
            defAttrs.recycle()
            val ta = context.obtainStyledAttributes(attrs, R.styleable.BottomPanelLayout)
            if (ta != null) {
                mPanelHeight =
                    ta.getDimensionPixelSize(R.styleable.BottomPanelLayout_panelHeight, -1)
//                mShadowHeight =
//                    ta.getDimensionPixelSize(R.styleable.SlidingUpPanelLayout_umanoShadowHeight, -1)
//                mParallaxOffset = ta.getDimensionPixelSize(
//                    R.styleable.SlidingUpPanelLayout_umanoParallaxOffset,
//                    -1
//                )
//                minFlingVelocity = ta.getInt(
//                    R.styleable.SlidingUpPanelLayout_umanoFlingVelocity,
//                    DEFAULT_MIN_FLING_VELOCITY
//                )
//                mCoveredFadeColor =
//                    ta.getColor(R.styleable.SlidingUpPanelLayout_umanoFadeColor, DEFAULT_FADE_COLOR)
//                mDragViewResId =
//                    ta.getResourceId(R.styleable.SlidingUpPanelLayout_umanoDragView, -1)
                mScrollableViewResId =
                    ta.getResourceId(R.styleable.BottomPanelLayout_scrollableView, -1)
//                isOverlayed = ta.getBoolean(
//                    R.styleable.SlidingUpPanelLayout_umanoOverlay,
//                    DEFAULT_OVERLAY_FLAG
//                )
//                isClipPanel = ta.getBoolean(
//                    R.styleable.SlidingUpPanelLayout_umanoClipPanel,
//                    DEFAULT_CLIP_PANEL_FLAG
//                )
//                mAnchorPoint = ta.getFloat(
//                    R.styleable.SlidingUpPanelLayout_umanoAnchorPoint,
//                    DEFAULT_ANCHOR_POINT
//                )
                mSlideState = PanelState.values()[ta.getInt(
                    R.styleable.BottomPanelLayout_initialState,
                    DEFAULT_SLIDE_STATE.ordinal
                )]
//                val interpolatorResId =
//                    ta.getResourceId(R.styleable.SlidingUpPanelLayout_umanoScrollInterpolator, -1)
//                if (interpolatorResId != -1) {
//                    scrollerInterpolator =
//                        AnimationUtils.loadInterpolator(context, interpolatorResId)
//                }
                ta.recycle()
            }
        }
        val density = context.resources.displayMetrics.density
        if (mPanelHeight == -1) {
            mPanelHeight = (DEFAULT_PANEL_HEIGHT * density + 0.5f).toInt()
        }
        if (mShadowHeight == -1) {
            mShadowHeight = (DEFAULT_SHADOW_HEIGHT * density + 0.5f).toInt()
        }
        if (mParallaxOffset == -1) {
            mParallaxOffset = (DEFAULT_PARALLAX_OFFSET * density).toInt()
        }
        // If the shadow height is zero, don't show the shadow
        mShadowDrawable = if (mShadowHeight > 0) {
            if (mIsSlidingUp) {
                resources.getDrawable(R.drawable.above_shadow)
            } else {
                resources.getDrawable(R.drawable.below_shadow)
            }
        } else {
            null
        }
        setWillNotDraw(false)
        mDragHelper = ViewDragHelper.create(this, 0.5f, scrollerInterpolator, DragHelperCallback())
        mDragHelper?.minVelocity = minFlingVelocity * density
        mIsTouchEnabled = true
    }

    /**
     * Set the Drag View after the view is inflated
     */
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (mDragViewResId != -1) {
            setDragView(findViewById(mDragViewResId))
        }
        if (mScrollableViewResId != -1) {
            setScrollableView(findViewById(mScrollableViewResId))
        }
    }

    fun setGravity(gravity: Int) {
        require(!(gravity != Gravity.TOP && gravity != Gravity.BOTTOM)) { "gravity must be set to either top or bottom" }
        mIsSlidingUp = gravity == Gravity.BOTTOM
        if (!mFirstLayout) {
            requestLayout()
        }
    }
    /**
     * @return The ARGB-packed color value used to fade the fixed pane
     */
    /**
     * Set the color used to fade the pane covered by the sliding pane out when the pane
     * will become fully covered in the expanded state.
     *
     * @param color An ARGB-packed color value
     */
    var coveredFadeColor: Int
        get() = mCoveredFadeColor
        set(color) {
            mCoveredFadeColor = color
            requestLayout()
        }

    /**
     * Set sliding enabled flag
     *
     * @param enabled flag value
     */
    var isTouchEnabled: Boolean
        get() = mIsTouchEnabled && mSlideableView != null && mSlideState != PanelState.HIDDEN
        set(enabled) {
            mIsTouchEnabled = enabled
        }

    fun smoothToBottom() {
        smoothSlideTo(0f, 0)
    }
    /**
     * @return The current shadow height
     */
    /**
     * Set the shadow height
     *
     * @param val A height in pixels
     */
    var shadowHeight: Int
        get() = mShadowHeight
        set(value) {
            mShadowHeight = value
            if (!mFirstLayout) {
                invalidate()
            }
        }
    /**
     * @return The current collapsed panel height
     */
    /**
     * Set the collapsed panel height in pixels
     *
     * @param val A height in pixels
     */
    var panelHeight: Int
        get() = mPanelHeight
        set(value) {
            if (panelHeight == value) {
                return
            }
            mPanelHeight = value
            if (!mFirstLayout) {
                requestLayout()
            }
            if (panelState == PanelState.COLLAPSED) {
                smoothToBottom()
                invalidate()
                return
            }
        }// Clamp slide offset at zero for parallax computation;

    /**
     * @return The current parallax offset
     */
    val currentParallaxOffset: Int
        get() {
            // Clamp slide offset at zero for parallax computation;
            val offset = (mParallaxOffset * Math.max(mSlideOffset, 0f)).toInt()
            return if (mIsSlidingUp) -offset else offset
        }

    /**
     * Set parallax offset for the panel
     *
     * @param val A height in pixels
     */
    fun setParallaxOffset(`val`: Int) {
        mParallaxOffset = `val`
        if (!mFirstLayout) {
            requestLayout()
        }
    }

    /**
     * Adds a panel slide listener
     *
     * @param listener
     */
    fun addPanelSlideListener(listener: PanelSlideListener) {
        synchronized(mPanelSlideListeners) { mPanelSlideListeners.add(listener) }
    }

    /**
     * Removes a panel slide listener
     *
     * @param listener
     */
    fun removePanelSlideListener(listener: PanelSlideListener) {
        synchronized(mPanelSlideListeners) { mPanelSlideListeners.remove(listener) }
    }

    /**
     * Provides an on click for the portion of the main view that is dimmed. The listener is not
     * triggered if the panel is in a collapsed or a hidden position. If the on click listener is
     * not provided, the clicks on the dimmed area are passed through to the main layout.
     *
     * @param listener
     */
    fun setFadeOnClickListener(listener: OnClickListener?) {
        mFadeOnClickListener = listener
    }

    /**
     * Set the draggable view portion. Use to null, to allow the whole panel to be draggable
     *
     * @param dragView A view that will be used to drag the panel.
     */
    fun setDragView(dragView: View?) {
        if (mDragView != null) {
            mDragView!!.setOnClickListener(null)
        }
        mDragView = dragView
        if (mDragView != null) {
            mDragView!!.isClickable = true
            mDragView!!.isFocusable = false
            mDragView!!.isFocusableInTouchMode = false
            mDragView!!.setOnClickListener(OnClickListener {
                if (!isEnabled || !isTouchEnabled) return@OnClickListener
                panelState =
                    if (mSlideState != PanelState.EXPANDED && mSlideState != PanelState.ANCHORED) {
                        if (mAnchorPoint < 1.0f) {
                            PanelState.ANCHORED
                        } else {
                            PanelState.EXPANDED
                        }
                    } else {
                        PanelState.COLLAPSED
                    }
            })
        }
    }

    /**
     * Set the draggable view portion. Use to null, to allow the whole panel to be draggable
     *
     * @param dragViewResId The resource ID of the new drag view
     */
    fun setDragView(dragViewResId: Int) {
        mDragViewResId = dragViewResId
        setDragView(findViewById(dragViewResId))
    }

    /**
     * Set the scrollable child of the sliding layout. If set, scrolling will be transfered between
     * the panel and the view when necessary
     *
     * @param scrollableView The scrollable view
     */
    fun setScrollableView(scrollableView: View?) {
        mScrollableView = scrollableView
    }

    /**
     * Sets the current scrollable view helper. See ScrollableViewHelper description for details.
     *
     * @param helper
     */
    fun setScrollableViewHelper(helper: ScrollableViewHelper) {
        mScrollableViewHelper = helper
    }
    /**
     * Gets the currently set anchor point
     *
     * @return the currently set anchor point
     */
    /**
     * Set an anchor point where the panel can stop during sliding
     *
     * @param anchorPoint A value between 0 and 1, determining the position of the anchor point
     * starting from the top of the layout.
     */
    var anchorPoint: Float
        get() = mAnchorPoint
        set(anchorPoint) {
            if (anchorPoint > 0 && anchorPoint <= 1) {
                mAnchorPoint = anchorPoint
                mFirstLayout = true
                requestLayout()
            }
        }

    fun dispatchOnPanelSlide(panel: View?) {
        synchronized(mPanelSlideListeners) {
            for (l in mPanelSlideListeners) {
                l.onPanelSlide(panel, mSlideOffset)
            }
        }
    }

    fun dispatchOnPanelStateChanged(
        panel: View?,
        previousState: PanelState?,
        newState: PanelState?
    ) {
        synchronized(mPanelSlideListeners) {
            for (l in mPanelSlideListeners) {
                l.onPanelStateChanged(panel, previousState, newState)
            }
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
    }

    fun updateObscuredViewVisibility() {
        if (childCount == 0) {
            return
        }
        val leftBound = paddingLeft
        val rightBound = width - paddingRight
        val topBound = paddingTop
        val bottomBound = height - paddingBottom
        val left: Int
        val right: Int
        val top: Int
        val bottom: Int
        if (mSlideableView != null && hasOpaqueBackground(
                mSlideableView!!
            )
        ) {
            left = mSlideableView!!.left
            right = mSlideableView!!.right
            top = mSlideableView!!.top
            bottom = mSlideableView!!.bottom
        } else {
            bottom = 0
            top = bottom
            right = top
            left = right
        }
        val child = getChildAt(0)
        val clampedChildLeft = Math.max(leftBound, child.left)
        val clampedChildTop = Math.max(topBound, child.top)
        val clampedChildRight = Math.min(rightBound, child.right)
        val clampedChildBottom = Math.min(bottomBound, child.bottom)
        val vis: Int
        vis =
            if (clampedChildLeft >= left && clampedChildTop >= top && clampedChildRight <= right && clampedChildBottom <= bottom) {
                INVISIBLE
            } else {
                VISIBLE
            }
        child.visibility = vis
    }

    fun setAllChildrenVisible() {
        var i = 0
        val childCount = childCount
        while (i < childCount) {
            val child = getChildAt(i)
            if (child.visibility == INVISIBLE) {
                child.visibility = VISIBLE
            }
            i++
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mFirstLayout = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mFirstLayout = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        check(!(widthMode != MeasureSpec.EXACTLY && widthMode != MeasureSpec.AT_MOST)) { "Width must have an exact value or MATCH_PARENT" }
        check(!(heightMode != MeasureSpec.EXACTLY && heightMode != MeasureSpec.AT_MOST)) { "Height must have an exact value or MATCH_PARENT" }
        val childCount = childCount
        check(childCount == 2) { "Sliding up panel layout must have exactly 2 children!" }
        mMainView = getChildAt(0)
        mSlideableView = getChildAt(1)
        if (mDragView == null) {
            setDragView(mSlideableView)
        }

        // If the sliding panel is not visible, then put the whole view in the hidden state
        if (mSlideableView?.visibility != VISIBLE) {
            mSlideState = PanelState.HIDDEN
        }
        val layoutHeight = heightSize - paddingTop - paddingBottom
        val layoutWidth = widthSize - paddingLeft - paddingRight

        // First pass. Measure based on child LayoutParams width/height.
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams

            // We always measure the sliding panel in order to know it's height (needed for show panel)
            if (child.visibility == GONE && i == 0) {
                continue
            }
            var height = layoutHeight
            var width = layoutWidth
            if (child === mMainView) {
                if (!isOverlayed && mSlideState != PanelState.HIDDEN) {
                    height -= mPanelHeight
                }
                width -= lp.leftMargin + lp.rightMargin
            } else if (child === mSlideableView) {
                // The slideable view should be aware of its top margin.
                // See https://github.com/umano/AndroidSlidingUpPanel/issues/412.
                height -= lp.topMargin
            }
            var childWidthSpec: Int
            childWidthSpec = if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
            } else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            } else {
                MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY)
            }
            var childHeightSpec: Int
            if (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
            } else {
                // Modify the height based on the weight.
                if (lp.weight > 0 && lp.weight < 1) {
                    height = (height * lp.weight).toInt()
                } else if (lp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                    height = lp.height
                }
                childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            }
            child.measure(childWidthSpec, childHeightSpec)
            if (child === mSlideableView) {
                mSlideRange = mSlideableView!!.measuredHeight - mPanelHeight
            }
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val childCount = childCount
        if (mFirstLayout) {
            mSlideOffset = when (mSlideState) {
                PanelState.EXPANDED -> 1.0f
                PanelState.ANCHORED -> mAnchorPoint
                PanelState.HIDDEN -> {
                    val newTop =
                        computePanelTopPosition(0.0f) + if (mIsSlidingUp) +mPanelHeight else -mPanelHeight
                    computeSlideOffset(newTop)
                }

                else -> 0f
            }
        }
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams

            // Always layout the sliding view on the first layout
            if (child.visibility == GONE && (i == 0 || mFirstLayout)) {
                continue
            }
            val childHeight = child.measuredHeight
            var childTop = paddingTop
            if (child === mSlideableView) {
                childTop = computePanelTopPosition(mSlideOffset)
            }
            if (!mIsSlidingUp) {
                if (child === mMainView && !isOverlayed) {
                    childTop =
                        computePanelTopPosition(mSlideOffset) + mSlideableView!!.measuredHeight
                }
            }
            val childBottom = childTop + childHeight
            val childLeft = paddingLeft + lp.leftMargin
            val childRight = childLeft + child.measuredWidth
            child.layout(childLeft, childTop, childRight, childBottom)
        }
        if (mFirstLayout) {
            updateObscuredViewVisibility()
        }
        applyParallaxForCurrentSlideOffset()
        mFirstLayout = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Recalculate sliding panes and their details
        if (h != oldh) {
            mFirstLayout = true
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // If the scrollable view is handling touch, never intercept
        if (mIsScrollableViewHandlingTouch || !isTouchEnabled) {
            mDragHelper!!.abort()
            return false
        }
        val action = MotionEventCompat.getActionMasked(ev)
        val x = ev.x
        val y = ev.y
        val adx = Math.abs(x - mInitialMotionX)
        val ady = Math.abs(y - mInitialMotionY)
        val dragSlop = mDragHelper!!.touchSlop
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mIsUnableToDrag = false
                mInitialMotionX = x
                mInitialMotionY = y
                if (!isViewUnder(mDragView, x.toInt(), y.toInt())) {
                    mDragHelper!!.cancel()
                    mIsUnableToDrag = true
                    return false
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (ady > dragSlop && adx > ady) {
                    mDragHelper!!.cancel()
                    mIsUnableToDrag = true
                    return false
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                // If the dragView is still dragging when we get here, we need to call processTouchEvent
                // so that the view is settled
                // Added to make scrollable views work (tokudu)
                if (mDragHelper!!.isDragging) {
                    mDragHelper!!.processTouchEvent(ev)
                    return true
                }
                // Check if this was a click on the faded part of the screen, and fire off the listener if there is one.
                if (ady <= dragSlop && adx <= dragSlop && mSlideOffset > 0 && !isViewUnder(
                        mSlideableView,
                        mInitialMotionX.toInt(),
                        mInitialMotionY.toInt()
                    ) && mFadeOnClickListener != null
                ) {
                    playSoundEffect(SoundEffectConstants.CLICK)
                    mFadeOnClickListener!!.onClick(this)
                    return true
                }
            }
        }
        return mDragHelper!!.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (!isEnabled || !isTouchEnabled) {
            super.onTouchEvent(ev)
        } else try {
            mDragHelper!!.processTouchEvent(ev)
            true
        } catch (ex: Exception) {
            // Ignore the pointer out of range exception
            false
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)
        if (!isEnabled || !isTouchEnabled || mIsUnableToDrag && action != MotionEvent.ACTION_DOWN) {
            mDragHelper!!.abort()
            return super.dispatchTouchEvent(ev)
        }
        val x = ev.x
        val y = ev.y
        if (action == MotionEvent.ACTION_DOWN) {
            mIsScrollableViewHandlingTouch = false
            mPrevMotionX = x
            mPrevMotionY = y
        } else if (action == MotionEvent.ACTION_MOVE) {
            val dx = x - mPrevMotionX
            val dy = y - mPrevMotionY
            mPrevMotionX = x
            mPrevMotionY = y
            if (Math.abs(dx) > Math.abs(dy)) {
                // Scrolling horizontally, so ignore
                return super.dispatchTouchEvent(ev)
            }

            // If the scroll view isn't under the touch, pass the
            // event along to the dragView.
            if (!isViewUnder(mScrollableView, mInitialMotionX.toInt(), mInitialMotionY.toInt())) {
                return super.dispatchTouchEvent(ev)
            }

            // Which direction (up or down) is the drag moving?
            if (dy * (if (mIsSlidingUp) 1 else -1) > 0) { // Collapsing
                // Is the child less than fully scrolled?
                // Then let the child handle it.
                if (mScrollableViewHelper.getScrollableViewScrollPosition(
                        mScrollableView,
                        mIsSlidingUp
                    ) > 0
                ) {
                    mIsScrollableViewHandlingTouch = true
                    return super.dispatchTouchEvent(ev)
                }

                // Was the child handling the touch previously?
                // Then we need to rejigger things so that the
                // drag panel gets a proper down event.
                if (mIsScrollableViewHandlingTouch) {
                    // Send an 'UP' event to the child.
                    val up = MotionEvent.obtain(ev)
                    up.action = MotionEvent.ACTION_CANCEL
                    super.dispatchTouchEvent(up)
                    up.recycle()

                    // Send a 'DOWN' event to the panel. (We'll cheat
                    // and hijack this one)
                    ev.action = MotionEvent.ACTION_DOWN
                }
                mIsScrollableViewHandlingTouch = false
                return onTouchEvent(ev)
            } else if (dy * (if (mIsSlidingUp) 1 else -1) < 0) { // Expanding
                // Is the panel less than fully expanded?
                // Then we'll handle the drag here.
                if (mSlideOffset < 1.0f) {
                    mIsScrollableViewHandlingTouch = false
                    return onTouchEvent(ev)
                }

                // Was the panel handling the touch previously?
                // Then we need to rejigger things so that the
                // child gets a proper down event.
                if (!mIsScrollableViewHandlingTouch && mDragHelper!!.isDragging) {
                    mDragHelper!!.cancel()
                    ev.action = MotionEvent.ACTION_DOWN
                }
                mIsScrollableViewHandlingTouch = true
                return super.dispatchTouchEvent(ev)
            }
        } else if (action == MotionEvent.ACTION_UP) {
            // If the scrollable view was handling the touch and we receive an up
            // we want to clear any previous dragging state so we don't intercept a touch stream accidentally
            if (mIsScrollableViewHandlingTouch) {
                mDragHelper!!.setDragState(ViewDragHelper.STATE_IDLE)
            }
        }

        // In all other cases, just let the default behavior take over.
        return super.dispatchTouchEvent(ev)
    }

    private fun isViewUnder(view: View?, x: Int, y: Int): Boolean {
        if (view == null) return false
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        val parentLocation = IntArray(2)
        getLocationOnScreen(parentLocation)
        val screenX = parentLocation[0] + x
        val screenY = parentLocation[1] + y
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.width && screenY >= viewLocation[1] && screenY < viewLocation[1] + view.height
    }

    /*
     * Computes the top position of the panel based on the slide offset.
     */
    private fun computePanelTopPosition(slideOffset: Float): Int {
        val slidingViewHeight = if (mSlideableView != null) mSlideableView!!.measuredHeight else 0
        val slidePixelOffset = (slideOffset * mSlideRange).toInt()
        // Compute the top of the panel if its collapsed
        return if (mIsSlidingUp) measuredHeight - paddingBottom - mPanelHeight - slidePixelOffset else paddingTop - slidingViewHeight + mPanelHeight + slidePixelOffset
    }

    /*
     * Computes the slide offset based on the top position of the panel
     */
    private fun computeSlideOffset(topPosition: Int): Float {
        // Compute the panel top position if the panel is collapsed (offset 0)
        val topBoundCollapsed = computePanelTopPosition(0f)

        // Determine the new slide offset based on the collapsed top position and the new required
        // top position
        return if (mIsSlidingUp) (topBoundCollapsed - topPosition).toFloat() / mSlideRange else (topPosition - topBoundCollapsed).toFloat() / mSlideRange
    }
    /**
     * Returns the current state of the panel as an enum.
     *
     * @return the current panel state
     */// Abort any running animation, to allow state change
    /**
     * Change panel state to the given state with
     *
     * @param state - new panel state
     */
    private var panelState: PanelState? = null
        get() = mSlideState

    private fun setPanelStateInternal(state: PanelState) {
        if (mSlideState == state) return
        val oldState = mSlideState
        mSlideState = state
        dispatchOnPanelStateChanged(this, oldState, state)
    }

    /**
     * Update the parallax based on the current slide offset.
     */
    @SuppressLint("NewApi")
    private fun applyParallaxForCurrentSlideOffset() {
        if (mParallaxOffset > 0) {
            val mainViewOffset = currentParallaxOffset
            ViewCompat.setTranslationY(mMainView, mainViewOffset.toFloat())
        }
    }

    private fun onPanelDragged(newTop: Int) {
        if (mSlideState != PanelState.DRAGGING) {
            mLastNotDraggingSlideState = mSlideState
        }
        setPanelStateInternal(PanelState.DRAGGING)
        // Recompute the slide offset based on the new top position
        mSlideOffset = computeSlideOffset(newTop)
        applyParallaxForCurrentSlideOffset()
        // Dispatch the slide event
        dispatchOnPanelSlide(mSlideableView)
        // If the slide offset is negative, and overlay is not on, we need to increase the
        // height of the main content
        val lp = mMainView!!.layoutParams as LayoutParams
        val defaultHeight = height - paddingBottom - paddingTop - mPanelHeight
        if (mSlideOffset <= 0 && !isOverlayed) {
            // expand the main view
            lp.height =
                if (mIsSlidingUp) newTop - paddingBottom else height - paddingBottom - mSlideableView!!.measuredHeight - newTop
            if (lp.height == defaultHeight) {
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            mMainView!!.requestLayout()
        } else if (lp.height != ViewGroup.LayoutParams.MATCH_PARENT && !isOverlayed) {
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            mMainView!!.requestLayout()
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val result: Boolean
        val save = canvas.save()
        if (mSlideableView != null && mSlideableView !== child) { // if main view
            // Clip against the slider; no sense drawing what will immediately be covered,
            // Unless the panel is set to overlay content
            canvas.getClipBounds(mTmpRect)
            if (!isOverlayed) {
                if (mIsSlidingUp) {
                    mTmpRect.bottom = Math.min(mTmpRect.bottom, mSlideableView!!.top)
                } else {
                    mTmpRect.top = Math.max(mTmpRect.top, mSlideableView!!.bottom)
                }
            }
            if (isClipPanel) {
                canvas.clipRect(mTmpRect)
            }
            result = super.drawChild(canvas, child, drawingTime)
            if (mCoveredFadeColor != 0 && mSlideOffset > 0) {
                val baseAlpha = mCoveredFadeColor and -0x1000000 ushr 24
                val imag = (baseAlpha * mSlideOffset).toInt()
                val color = imag shl 24 or (mCoveredFadeColor and 0xffffff)
                mCoveredFadePaint.color = color
                canvas.drawRect(mTmpRect, mCoveredFadePaint)
            }
        } else {
            result = super.drawChild(canvas, child, drawingTime)
        }
        canvas.restoreToCount(save)
        return result
    }

    /**
     * Smoothly animate mDraggingPane to the target X position within its range.
     *
     * @param slideOffset position to animate to
     * @param velocity    initial velocity in case of fling, or 0.
     */
    fun smoothSlideTo(slideOffset: Float, velocity: Int): Boolean {
        if (!isEnabled || mSlideableView == null) {
            // Nothing to do.
            return false
        }
        val panelTop = computePanelTopPosition(slideOffset)
        if (mDragHelper!!.smoothSlideViewTo(mSlideableView, mSlideableView!!.left, panelTop)) {
            setAllChildrenVisible()
            ViewCompat.postInvalidateOnAnimation(this)
            return true
        }
        return false
    }

    override fun computeScroll() {
        if (mDragHelper != null && mDragHelper?.continueSettling(true) == true) {
            if (!isEnabled) {
                mDragHelper?.abort()
                return
            }
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun draw(c: Canvas) {
        super.draw(c)

        // draw the shadow
        if (mShadowDrawable != null && mSlideableView != null) {
            val right = mSlideableView!!.right
            val top: Int
            val bottom: Int
            if (mIsSlidingUp) {
                top = mSlideableView!!.top - mShadowHeight
                bottom = mSlideableView!!.top
            } else {
                top = mSlideableView!!.bottom
                bottom = mSlideableView!!.bottom + mShadowHeight
            }
            val left = mSlideableView!!.left
            mShadowDrawable?.setBounds(left, top, right, bottom)
            mShadowDrawable?.draw(c)
        }
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view v passed should itself be checked for scrollability (true),
     * or just its children (false).
     * @param dx     Delta scrolled in pixels
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        if (v is ViewGroup) {
            val group = v
            val scrollX = v.getScrollX()
            val scrollY = v.getScrollY()
            val count = group.childCount
            // Count backwards - let topmost views consume scroll distance first.
            for (i in count - 1 downTo 0) {
                val child = group.getChildAt(i)
                if (x + scrollX >= child.left && x + scrollX < child.right && y + scrollY >= child.top && y + scrollY < child.bottom &&
                    canScroll(
                        child, true, dx, x + scrollX - child.left,
                        y + scrollY - child.top
                    )
                ) {
                    return true
                }
            }
        }
        return checkV && ViewCompat.canScrollHorizontally(v, -dx)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return if (p is MarginLayoutParams) LayoutParams(p) else LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams && super.checkLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    public override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putSerializable(
            SLIDING_STATE,
            if (mSlideState != PanelState.DRAGGING) mSlideState else mLastNotDraggingSlideState
        )
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        var state: Parcelable? = state
        if (state is Bundle) {
            val bundle = state
            mSlideState = bundle.getSerializable(SLIDING_STATE) as PanelState?
            mSlideState = if (mSlideState == null) DEFAULT_SLIDE_STATE else mSlideState
            state = bundle.getParcelable("superState")
        }
        super.onRestoreInstanceState(state)
    }

    private inner class DragHelperCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View?, pointerId: Int): Boolean {
            return !mIsUnableToDrag && child === mSlideableView
        }

        override fun onViewDragStateChanged(state: Int) {
            if (mDragHelper != null && mDragHelper?.viewDragState == ViewDragHelper.STATE_IDLE) {
                mSlideOffset = computeSlideOffset(mSlideableView!!.top)
                applyParallaxForCurrentSlideOffset()
                if (mSlideOffset == 1f) {
                    updateObscuredViewVisibility()
                    setPanelStateInternal(PanelState.EXPANDED)
                } else if (mSlideOffset == 0f) {
                    setPanelStateInternal(PanelState.COLLAPSED)
                } else if (mSlideOffset < 0) {
                    setPanelStateInternal(PanelState.HIDDEN)
                    mSlideableView!!.visibility = INVISIBLE
                } else {
                    updateObscuredViewVisibility()
                    setPanelStateInternal(PanelState.ANCHORED)
                }
            }
        }

        override fun onViewCaptured(capturedChild: View?, activePointerId: Int) {
            setAllChildrenVisible()
        }

        override fun onViewPositionChanged(
            changedView: View?,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            onPanelDragged(top)
            invalidate()
        }

        override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
            var target = 0

            // direction is always positive if we are sliding in the expanded direction
            val direction = if (mIsSlidingUp) -yvel else yvel
            target = if (direction > 0 && mSlideOffset <= mAnchorPoint) {
                // swipe up -> expand and stop at anchor point
                computePanelTopPosition(mAnchorPoint)
            } else if (direction > 0 && mSlideOffset > mAnchorPoint) {
                // swipe up past anchor -> expand
                computePanelTopPosition(1.0f)
            } else if (direction < 0 && mSlideOffset >= mAnchorPoint) {
                // swipe down -> collapse and stop at anchor point
                computePanelTopPosition(mAnchorPoint)
            } else if (direction < 0 && mSlideOffset < mAnchorPoint) {
                // swipe down past anchor -> collapse
                computePanelTopPosition(0.0f)
            } else if (mSlideOffset >= (1f + mAnchorPoint) / 2) {
                // zero velocity, and far enough from anchor point => expand to the top
                computePanelTopPosition(1.0f)
            } else if (mSlideOffset >= mAnchorPoint / 2) {
                // zero velocity, and close enough to anchor point => go to anchor
                computePanelTopPosition(mAnchorPoint)
            } else {
                // settle at the bottom
                computePanelTopPosition(0.0f)
            }
            if (mDragHelper != null) {
                mDragHelper?.settleCapturedViewAt(releasedChild!!.left, target)
            }
            invalidate()
        }

        override fun getViewVerticalDragRange(child: View?): Int {
            return mSlideRange
        }

        override fun clampViewPositionVertical(child: View?, top: Int, dy: Int): Int {
            val collapsedTop = computePanelTopPosition(0f)
            val expandedTop = computePanelTopPosition(1.0f)
            return if (mIsSlidingUp) {
                Math.min(Math.max(top, expandedTop), collapsedTop)
            } else {
                Math.min(Math.max(top, collapsedTop), expandedTop)
            }
        }
    }

    class LayoutParams : MarginLayoutParams {
        var weight = 0f

        constructor() : super(MATCH_PARENT, MATCH_PARENT)
        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, weight: Float) : super(width, height) {
            this.weight = weight
        }

        constructor(source: ViewGroup.LayoutParams?) : super(source)
        constructor(source: MarginLayoutParams?) : super(source)
        constructor(source: LayoutParams?) : super(source)
        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val ta = c.obtainStyledAttributes(attrs, ATTRS)
            if (ta != null) {
                weight = ta.getFloat(0, 0f)
                ta.recycle()
            }
        }

        companion object {
            private val ATTRS = intArrayOf(
                android.R.attr.layout_weight
            )
        }
    }

    companion object {
        private val TAG = BottomPanelLayout::class.java.simpleName

        /**
         * Default peeking out panel height
         */
        const val DEFAULT_PANEL_HEIGHT = 68 // dp;

        /**
         * Default anchor point height
         */
        private const val DEFAULT_ANCHOR_POINT = 1.0f // In relative %

        /**
         * Default initial state for the component
         */
        private val DEFAULT_SLIDE_STATE = PanelState.COLLAPSED

        /**
         * Default height of the shadow above the peeking out panel
         */
        private const val DEFAULT_SHADOW_HEIGHT = 4 // dp;

        /**
         * If no fade color is given by default it will fade to 80% gray.
         */
        private const val DEFAULT_FADE_COLOR = -0x67000000

        /**
         * Default Minimum velocity that will be detected as a fling
         */
        private const val DEFAULT_MIN_FLING_VELOCITY = 400 // dips per second

        /**
         * Default is set to false because that is how it was written
         */
        private const val DEFAULT_OVERLAY_FLAG = false

        /**
         * Default is set to true for clip panel for performance reasons
         */
        private const val DEFAULT_CLIP_PANEL_FLAG = true

        /**
         * Default attributes for layout
         */
        private val DEFAULT_ATTRS = intArrayOf(
            android.R.attr.gravity
        )

        /**
         * Tag for the sliding state stored inside the bundle
         */
        const val SLIDING_STATE = "sliding_state"

        /**
         * Default parallax length of the main view
         */
        private const val DEFAULT_PARALLAX_OFFSET = 0
        private fun hasOpaqueBackground(v: View): Boolean {
            val bg = v.background
            return bg != null && bg.opacity == PixelFormat.OPAQUE
        }
    }
}