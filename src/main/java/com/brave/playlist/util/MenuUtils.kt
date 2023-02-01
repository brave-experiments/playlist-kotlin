package com.brave.playlist.util

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import com.brave.playlist.R
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.listener.PlaylistItemOptionsListener
import com.brave.playlist.listener.PlaylistOptionsListener
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.model.PlaylistItemOptionModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.model.PlaylistOptionsModel
import com.brave.playlist.view.bottomsheet.PlaylistItemOptionsBottomSheet
import com.brave.playlist.view.bottomsheet.PlaylistOptionsBottomSheet

object MenuUtils {

    @JvmStatic
    fun showPlaylistItemMenu(
        context: Context,
        fragmentManager: FragmentManager,
        playlistItemModel: PlaylistItemModel,
        playlistId : String?,
        playlistItemOptionsListener: PlaylistItemOptionsListener
    ) {
        PlaylistItemOptionsBottomSheet(
            mutableListOf(
//                PlaylistItemOptionModel(
//                    context.resources.getString(R.string.move_item),
//                    R.drawable.ic_move_media,
//                    PlaylistOptions.MOVE_PLAYLIST_ITEM,
//                    playlistItemModel = playlistItemModel,
//                    playlistId = playlistId
//                ), PlaylistItemOptionModel(
//                    context.resources.getString(R.string.copy_item),
//                    R.drawable.ic_copy_media,
//                    PlaylistOptions.COPY_PLAYLIST_ITEM,
//                    playlistItemModel = playlistItemModel,
//                    playlistId = playlistId
//                ),
                PlaylistItemOptionModel(
                    context.resources.getString(R.string.delete_item_offline_data),
                    R.drawable.ic_remove_offline_data_playlist,
                    PlaylistOptions.DELETE_ITEMS_OFFLINE_DATA,
                    playlistItemModel = playlistItemModel,
                    playlistId = playlistId
                ), PlaylistItemOptionModel(
                    context.resources.getString(R.string.share_item),
                    R.drawable.ic_share,
                    PlaylistOptions.SHARE_PLAYLIST_ITEM,
                    playlistItemModel = playlistItemModel,
                    playlistId = playlistId
                ), PlaylistItemOptionModel(
                    context.resources.getString(R.string.open_in_new_tab),
                    R.drawable.ic_new_tab,
                    PlaylistOptions.OPEN_IN_NEW_TAB,
                    playlistItemModel = playlistItemModel,
                    playlistId = playlistId
                ), PlaylistItemOptionModel(
                    context.resources.getString(R.string.open_in_private_tab),
                    R.drawable.ic_private_tab,
                    PlaylistOptions.OPEN_IN_PRIVATE_TAB,
                    playlistItemModel = playlistItemModel,
                    playlistId = playlistId
                ), PlaylistItemOptionModel(
                    context.resources.getString(R.string.delete_item),
                    R.drawable.ic_playlist_delete,
                    PlaylistOptions.DELETE_PLAYLIST_ITEM,
                    playlistItemModel = playlistItemModel,
                    playlistId = playlistId
                )
            ), playlistItemOptionsListener
        ).show(fragmentManager, null)
    }

    @JvmStatic
    fun showAllPlaylistsMenu(
        context: Context,
        fragmentManager: FragmentManager,
        allPlaylistList: MutableList<PlaylistModel>,
        playlistOptionsListener: PlaylistOptionsListener
    ) {
        PlaylistOptionsBottomSheet(
            mutableListOf(
                PlaylistOptionsModel(
                    context.resources.getString(R.string.remove_all_offline_data),
                    R.drawable.ic_remove_offline_data_playlist,
                    PlaylistOptions.REMOVE_ALL_OFFLINE_DATA,
                    allPlaylistList
                ), PlaylistOptionsModel(
                    context.resources.getString(R.string.download_all_playlists_for_offline_use),
                    R.drawable.ic_cloud_download,
                    PlaylistOptions.DOWNLOAD_ALL_PLAYLISTS_FOR_OFFLINE_USE,
                    allPlaylistList
                )
            ), playlistOptionsListener
        ).show(fragmentManager, null)
    }

    @JvmStatic
    fun showMoveOrCopyMenu(
        view: View,
        fragmentManager: FragmentManager,
        selectedItems: ArrayList<PlaylistItemModel>,
        playlistOptionsListener: PlaylistOptionsListener
    ) {
        PlaylistOptionsBottomSheet(
            mutableListOf(
                PlaylistOptionsModel(
                    view.resources.getString(R.string.move_item),
                    R.drawable.ic_move_media,
                    PlaylistOptions.MOVE_PLAYLIST_ITEMS,
                    playlistItemModels = selectedItems
                ), PlaylistOptionsModel(
                    view.resources.getString(R.string.copy_item),
                    R.drawable.ic_copy_media,
                    PlaylistOptions.COPY_PLAYLIST_ITEMS,
                    playlistItemModels = selectedItems
                )
            ), playlistOptionsListener
        ).show(fragmentManager, null)
    }

    @JvmStatic
    fun showPlaylistMenu(
        context: Context,
        fragmentManager: FragmentManager,
        playlistModel: PlaylistModel,
        playlistOptionsListener: PlaylistOptionsListener
    ) {
        PlaylistOptionsBottomSheet(
            mutableListOf(
                PlaylistOptionsModel(
                    context.resources.getString(R.string.edit_text),
                    R.drawable.ic_edit_playlist,
                    PlaylistOptions.EDIT_PLAYLIST,
                    playlistModel = playlistModel
                ), PlaylistOptionsModel(
                    context.resources.getString(R.string.rename_text),
                    R.drawable.ic_rename_playlist,
                    PlaylistOptions.RENAME_PLAYLIST,
                    playlistModel = playlistModel
                ), PlaylistOptionsModel(
                    context.resources.getString(R.string.remove_playlist_offline_data),
                    R.drawable.ic_remove_offline_data_playlist,
                    PlaylistOptions.REMOVE_PLAYLIST_OFFLINE_DATA,
                    playlistModel = playlistModel
                ), PlaylistOptionsModel(
                    context.resources.getString(R.string.download_playlist_for_offline_use),
                    R.drawable.ic_cloud_download,
                    PlaylistOptions.DOWNLOAD_PLAYLIST_FOR_OFFLINE_USE,
                    playlistModel = playlistModel
                ), PlaylistOptionsModel(
                    context.resources.getString(R.string.delete_playlist),
                    R.drawable.ic_playlist_delete,
                    PlaylistOptions.DELETE_PLAYLIST,
                    playlistModel = playlistModel
                )
            ), playlistOptionsListener
        ).show(fragmentManager, null)
    }
}