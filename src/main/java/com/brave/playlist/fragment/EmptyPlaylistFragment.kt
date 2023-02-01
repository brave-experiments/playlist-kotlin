package com.brave.playlist.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.brave.playlist.R

class EmptyPlaylistFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_empty_playlist, container, false)
        view.findViewById<Button>(R.id.btBrowseForMedia).setOnClickListener {
            requireActivity().finish()
        }
        return view
    }
}