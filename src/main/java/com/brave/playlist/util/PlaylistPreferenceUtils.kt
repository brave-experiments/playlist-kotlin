package com.brave.playlist.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PlaylistPreferenceUtils {
    const val RECENTLY_PLAYED_PLAYLIST = "recently_played_playlist"
    const val SHOULD_SHOW_PLAYLIST_ONBOARDING = "should_show_playlist_onboarding"
    const val ADD_MEDIA_COUNT = "add_media_count"

    fun defaultPrefs(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    operator fun SharedPreferences.set(key: String, value: Any?) = when (value) {
        is String? -> edit { it.putString(key, value) }
        is Int -> edit { it.putInt(key, value) }
        is Boolean -> edit { it.putBoolean(key, value) }
        is Float -> edit { it.putFloat(key, value) }
        is Long -> edit { it.putLong(key, value) }
        else -> throw UnsupportedOperationException("Not yet implemented")
    }

    inline operator fun <reified T : Any> SharedPreferences.get(
        key: String,
        defaultValue: T? = null
    ): T = when (T::class) {
        String::class -> getString(key, defaultValue as? String ?: "") as T
        Int::class -> getInt(key, defaultValue as? Int ?: -1) as T
        Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T
        Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T
        Long::class -> getLong(key, defaultValue as? Long ?: -1) as T
        else -> throw UnsupportedOperationException("Not yet implemented")
    }

    @JvmStatic
    fun resetPlaylistPrefs(context: Context) {
        defaultPrefs(context).apply {
            this[RECENTLY_PLAYED_PLAYLIST] = ""
            this[SHOULD_SHOW_PLAYLIST_ONBOARDING] = true
            this[ADD_MEDIA_COUNT] = -1
        }
    }
}
