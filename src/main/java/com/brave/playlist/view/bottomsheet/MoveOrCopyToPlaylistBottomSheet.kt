package com.brave.playlist.view.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brave.playlist.PlaylistViewModel
import com.brave.playlist.R
import com.brave.playlist.adapter.PlaylistAdapter
import com.brave.playlist.extension.setTopCornersRounded
import com.brave.playlist.listener.PlaylistClickListener
import com.brave.playlist.model.MoveOrCopyModel
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.model.PlaylistModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray

class MoveOrCopyToPlaylistBottomSheet(private val moveOrCopyModel: MoveOrCopyModel) :
    BottomSheetDialogFragment(), PlaylistClickListener {

    private lateinit var playlistViewModel: PlaylistViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_add_or_move_to_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistViewModel = activity?.let {
            ViewModelProvider(
                it, ViewModelProvider.NewInstanceFactory()
            )
        }!![PlaylistViewModel::class.java]

        val layoutBottomSheet: MaterialCardView = view.findViewById(R.id.layoutBottomSheet)
        layoutBottomSheet.setTopCornersRounded(16)

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
                    )
                    playlistList.add(playlistItemModel)
                }

                allPlaylistList.add(
                    PlaylistModel(
                        playlistJsonObject.getString("id"),
                        playlistJsonObject.getString("name"),
                        playlistList
                    )
                )
            }

            val rvPlaylists: RecyclerView = view.findViewById(R.id.rvPlaylists)
            rvPlaylists.layoutManager = LinearLayoutManager(view.context)
            rvPlaylists.adapter = PlaylistAdapter(allPlaylistList, this)
        }

        val behavior = BottomSheetBehavior.from(layoutBottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
    }

    override fun onPlaylistClick(playlistModel: PlaylistModel) {
//        if (playlistModel.items.isNotEmpty()) {
//            playlistViewModel.setPlaylistToOpen(playlistModel.id)
//        } else {
//            parentFragmentManager
//                .beginTransaction()
//                .replace(android.R.id.content, EmptyPlaylistFragment())
//                .addToBackStack(AllPlaylistFragment::class.simpleName)
//                .commit()
//        }
    }
}
