package com.brave.playlist.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object ConnectionUtils {
    fun isDeviceOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)))
    }

    @JvmStatic
    fun isWifiAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
    }
}
