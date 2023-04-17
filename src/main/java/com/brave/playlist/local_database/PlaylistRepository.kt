package com.brave.playlist.local_database

import android.content.Context
import com.brave.playlist.model.PlaylistItemModel

class PlaylistRepository(context: Context) {

    var playlistItemModelDao: PlaylistItemModelDao? =
        PlaylistDatabase.getInstance(context)?.playlistItemModelDao()

    fun getAllPlaylistItemModel() : List<PlaylistItemModel>? {
        return playlistItemModelDao?.getAll()
    }

    fun getPlaylistItemById(playlistItemId:String) : PlaylistItemModel? {
        return playlistItemModelDao?.getPlaylistItemById(playlistItemId)
    }

    fun insertPlaylistItemModel(playlistItemModel: PlaylistItemModel) {
        playlistItemModelDao?.insertPlaylistItemModel(playlistItemModel)
    }

    fun updatePlaylistItemModel(playlistItemModel: PlaylistItemModel) {
        playlistItemModelDao?.updatePlaylistItemModel(playlistItemModel)
    }

    fun deletePlaylistItemModel(playlistItemModel: PlaylistItemModel) {
        playlistItemModelDao?.deletePlaylistItemModel(playlistItemModel)
    }

    fun deleteAllPlaylistItemModel() {
        playlistItemModelDao?.deleteAllPlaylistItemModel()
    }
}