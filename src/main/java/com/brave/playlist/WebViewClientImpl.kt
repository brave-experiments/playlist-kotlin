package com.brave.playlist

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.brave.playlist.listener.WebViewResponseListener

class WebViewClientImpl(private val webViewResponseListener: WebViewResponseListener) : WebViewClient() {
    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        webViewResponseListener.onError()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        webViewResponseListener.onPageLoadFinished()
    }
}