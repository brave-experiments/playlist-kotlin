/*
 * Copyright (c) 2023 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.brave.playlist.local_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.brave.playlist.model.DownloadQueueModel
import com.brave.playlist.model.LastPlayedPositionModel

@Database(
    entities = [LastPlayedPositionModel::class, DownloadQueueModel::class],
    version = 1,
    exportSchema = false
)
//@TypeConverters(PlaylistItemModelConverter::class)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun playlistItemModelDao(): PlaylistItemModelDao

    companion object {
        private var INSTANCE: PlaylistDatabase? = null

        fun getInstance(context: Context): PlaylistDatabase? {
            if (INSTANCE == null) {
                synchronized(PlaylistDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        PlaylistDatabase::class.java, "playlist.db"
                    ).build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
