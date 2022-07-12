package com.brave.braveandroidplaylist.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import com.brave.braveandroidplaylist.R

@Suppress("DEPRECATION")
class BraveToast(context: Context) : Toast(context) {

    private val tvToastDescription: AppCompatTextView
    private val tvToastAction: AppCompatTextView

    init {
        val mainView = View.inflate(context, R.layout.toast_brave, null)
        tvToastDescription = mainView.findViewById(R.id.tvToastDescription)
        tvToastAction = mainView.findViewById(R.id.tvToastAction)

        view = mainView
        setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        duration = LENGTH_LONG
    }

    fun setToastDescription(description: String): BraveToast {
        tvToastDescription.text = description
        return this
    }

    fun setToastActionText(text: String): BraveToast {
        tvToastAction.text = text
        return this
    }

    fun setToastActionOnClickListener(clickListener: View.OnClickListener): BraveToast {
        tvToastAction.setOnClickListener(clickListener)
        return this
    }
}