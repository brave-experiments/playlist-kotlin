/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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