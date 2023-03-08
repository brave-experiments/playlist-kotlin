package com.brave.playlist.view.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistAdapter
import com.brave.playlist.enums.PlaylistOptions
import com.brave.playlist.extension.setTopCornersRounded
import com.brave.playlist.fragment.AllPlaylistFragment
import com.brave.playlist.fragment.NewPlaylistFragment
import com.brave.playlist.listener.PlaylistClickListener
import com.brave.playlist.model.MoveOrCopyModel
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.model.PlaylistModel
import com.brave.playlist.util.PlaylistUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray

class MoveOrCopyToPlaylistBottomSheet :
    BottomSheetDialogFragment(), PlaylistClickListener {

    private lateinit var playlistViewModel: PlaylistViewModel
    private val moveOrCopyModel: MoveOrCopyModel = PlaylistUtils.moveOrCopyModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_add_or_move_to_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("BravePlaylist", "MoveOrCopyToPlaylistBottomSheet 1")

        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        Log.e("BravePlaylist", "MoveOrCopyToPlaylistBottomSheet 2")

        val layoutBottomSheet: MaterialCardView = view.findViewById(R.id.layoutBottomSheet)
        layoutBottomSheet.setTopCornersRounded(16)

        Log.e("BravePlaylist", "MoveOrCopyToPlaylistBottomSheet 3")

        var fromPlaylistId = ""
        if (moveOrCopyModel.items.isNotEmpty()) {
            fromPlaylistId = moveOrCopyModel.items[0].playlistId
        }

        playlistViewModel.allPlaylistData.observe(viewLifecycleOwner) { allPlaylistData ->
            val allPlaylistList = mutableListOf<PlaylistModel>()
            val allPlaylistJsonArray = JSONArray(allPlaylistData)
            for (i in 0 until allPlaylistJsonArray.length()) {
                val playlistList = mutableListOf<PlaylistItemModel>()
                val playlistJsonObject = allPlaylistJsonArray.getJSONObject(i)
                val jsonArray: JSONArray = playlistJsonObject.getJSONArray("items")
                for (j in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(j)
                    val playlistItemModel = PlaylistItemModel(
                        jsonObject.getString("id"),
                        playlistJsonObject.getString("id"),
                        jsonObject.getString("name"),
                        jsonObject.getString("page_source"),
                        jsonObject.getString("media_path"),
                        jsonObject.getString("media_src"),
                        jsonObject.getString("thumbnail_path"),
                        jsonObject.getString("author"),
                        jsonObject.getString("duration"),
                        jsonObject.getInt("last_played_position"),
                        jsonObject.getBoolean("cached")
                    )
                    playlistList.add(playlistItemModel)
                }

                if (playlistJsonObject.getString("id") != fromPlaylistId) {
                    allPlaylistList.add(
                        PlaylistModel(
                            playlistJsonObject.getString("id"),
                            playlistJsonObject.getString("name"),
                            playlistList
                        )
                    )
                }
            }

            allPlaylistList.add(
                0,
                PlaylistModel(
                    "new_playlist",
                    getString(R.string.playlist_new_text),
                    arrayListOf()
                )
            )

            Log.e("BravePlaylist", "MoveOrCopyToPlaylistBottomSheet 4")

            val rvPlaylists: RecyclerView = view.findViewById(R.id.rvPlaylists)
            rvPlaylists.layoutManager = LinearLayoutManager(view.context)
            rvPlaylists.adapter = PlaylistAdapter(allPlaylistList, this)
        }

        val behavior = BottomSheetBehavior.from(layoutBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
        Log.e("BravePlaylist", "MoveOrCopyToPlaylistBottomSheet 5")
    }

    override fun onPlaylistClick(playlistModel: PlaylistModel) {
        if (playlistModel.id == "new_playlist") {
            PlaylistUtils.moveOrCopyModel =
                MoveOrCopyModel(moveOrCopyModel.playlistOptions, "", moveOrCopyModel.items)
            val newPlaylistFragment = NewPlaylistFragment.newInstance(
                PlaylistOptions.NEW_PLAYLIST,
                shouldMoveOrCopy = true
            )
            parentFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, newPlaylistFragment)
                .addToBackStack(AllPlaylistFragment::class.simpleName)
                .commit()
        } else {
            PlaylistUtils.moveOrCopyModel = MoveOrCopyModel(
                moveOrCopyModel.playlistOptions,
                playlistModel.id,
                moveOrCopyModel.items
            )
            playlistViewModel.performMoveOrCopy(PlaylistUtils.moveOrCopyModel)
        }
        dismiss()
    }
}
