package com.brave.playlist.local_database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.brave.playlist.model.PlaylistItemModel

@Dao
interface PlaylistItemModelDao {
    @Query("SELECT * FROM PlaylistItemModel")
    fun getAll(): List<PlaylistItemModel>

    @Query("SELECT * FROM PlaylistItemModel WHERE id = :playlistItemId LIMIT 1")
    fun getPlaylistItemById(playlistItemId: String): PlaylistItemModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaylistItemModel(vararg playlistItemModel: PlaylistItemModel)

    @Update
    fun updatePlaylistItemModel(vararg playlistItemModel: PlaylistItemModel)

    @Delete
    fun deletePlaylistItemModel(vararg playlistItemModel: PlaylistItemModel)

    @Query("DELETE FROM PlaylistItemModel")
    fun deleteAllPlaylistItemModel()
}