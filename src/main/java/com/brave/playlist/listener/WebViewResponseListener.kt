package com.brave.playlist.listener

import com.brave.playlist.model.PlaylistOptionsModel

interface WebViewResponseListener {
    fun onPageLoadFinished() {}
    fun onError() {}
}