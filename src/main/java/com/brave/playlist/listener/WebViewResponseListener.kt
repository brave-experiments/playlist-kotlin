package com.brave.playlist.listener

interface WebViewResponseListener {
    fun onPageLoadFinished() {}
    fun onError() {}
}