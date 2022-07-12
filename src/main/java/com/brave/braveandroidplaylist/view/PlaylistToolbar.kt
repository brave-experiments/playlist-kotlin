package com.brave.braveandroidplaylist.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.brave.braveandroidplaylist.R


class PlaylistToolbar(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val ivOptionsToolbarPlayList: AppCompatImageView
    private val tvActionToolbarPlaylist: AppCompatTextView
    private val layoutMainToolbar: ConstraintLayout
    private val layoutEditToolbar: LinearLayoutCompat
    private val defaultStatusBarColor: Int

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    init {
        inflate(context, R.layout.toolbar_playlist, this)
        setBackgroundColor(getColor(android.R.color.transparent))
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlaylistToolbar)
        val showOptions = typedArray.getBoolean(R.styleable.PlaylistToolbar_showOptions, false)
        val showCreateButton =
            typedArray.getBoolean(R.styleable.PlaylistToolbar_showActionButton, false)
        val requireDarkMode =
            typedArray.getBoolean(R.styleable.PlaylistToolbar_requireDarkMode, false)
        val backButtonIcon = typedArray.getResourceId(
            R.styleable.PlaylistToolbar_backButtonIcon,
            R.drawable.ic_back_toolbar_playlist
        )

        val optionButtonIcon = typedArray.getResourceId(
            R.styleable.PlaylistToolbar_optionButtonIcon,
            R.drawable.ic_options_toolbar_playlist
        )

        val tvTitleToolbarPlaylist: AppCompatTextView = findViewById(R.id.tvTitleToolbarPlaylist)
        val ivBackToolbarPlaylist: AppCompatImageView = findViewById(R.id.ivBackToolbarPlaylist)
        val ivExitEditMode: AppCompatImageView = findViewById(R.id.ivExitEditMode)

        layoutMainToolbar = findViewById(R.id.layoutMainToolbar)
        layoutEditToolbar = findViewById(R.id.layoutEditToolbar)
        ivOptionsToolbarPlayList = findViewById(R.id.ivOptionsToolbarPlaylist)
        tvActionToolbarPlaylist = findViewById(R.id.tvActionToolbarPlaylist)

        ivOptionsToolbarPlayList.setImageResource(optionButtonIcon)
        ivBackToolbarPlaylist.setImageResource(backButtonIcon)

        defaultStatusBarColor = if (context is Activity)
            context.window.statusBarColor
        else
            0

        ivOptionsToolbarPlayList.visibility = if (showOptions) VISIBLE else GONE
        tvActionToolbarPlaylist.visibility = if (showCreateButton) VISIBLE else GONE

        tvTitleToolbarPlaylist.text = typedArray.getString(R.styleable.PlaylistToolbar_title)
        tvActionToolbarPlaylist.text =
            typedArray.getString(R.styleable.PlaylistToolbar_actionButtonText)

        if (requireDarkMode) {
            tvTitleToolbarPlaylist.setTextColor(getColor(R.color.playlist_white))
            ivBackToolbarPlaylist.setColorFilter(getColor(R.color.playlist_white))
        }

        ivBackToolbarPlaylist.setOnClickListener {
            if (context is Activity)
                context.finish()
        }

        ivExitEditMode.setOnClickListener { enableEditMode(false) }
        ivOptionsToolbarPlayList.setOnClickListener { showEditPlaylistPopupWindow() }

        typedArray.recycle()
    }

    private fun getColor(color: Int): Int = context.getColor(color)

    private fun showEditPlaylistPopupWindow() {

    }

    fun enableEditMode(enable: Boolean) {
        layoutMainToolbar.visibility = if (enable) GONE else VISIBLE
        layoutEditToolbar.visibility = if (enable) VISIBLE else GONE
        setStatusBarInEditMode(enable)
    }

    private fun setStatusBarInEditMode(editMode: Boolean) {
        if (context is Activity) {
            val activity = context as Activity
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.statusBarColor =
                if (editMode) getColor(R.color.edit_toolbar) else defaultStatusBarColor
        }
    }

    fun setActionButtonOnClickListener(clickListener: OnClickListener) {
        tvActionToolbarPlaylist.setOnClickListener(clickListener)
    }
}