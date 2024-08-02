package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaylistOptionsFragment : BottomSheetDialogFragment() {

    private var playlistIndex: Int = -1

    companion object {
        fun newInstance(playlistIndex:Int): PlaylistOptionsFragment {
            val fragment = PlaylistOptionsFragment()
            val args = Bundle()
            args.putInt("playlist_index", playlistIndex)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistIndex= arguments?.getInt("playlist_index") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.playlist_options, container, false)

        view.findViewById<Button>(R.id.edit_playlist_button).setOnClickListener {
            editPlaylist()
            dismiss()
        }

        view.findViewById<Button>(R.id.delete_playlist_button).setOnClickListener {
            deletePlaylist()
            dismiss()
        }

        return view
    }

    private fun editPlaylist() {
        val editPlaylistFragment = EditPlaylistFragment.newInstance(playlistIndex)
        editPlaylistFragment.show(parentFragmentManager, "EditPlaylistFragment")
    }

    private fun deletePlaylist() {
        val deletePlaylistFragment = DeletePlaylistFragment.newInstance(playlistIndex)
        deletePlaylistFragment.show(parentFragmentManager, "DeletePlaylistFragment")
    }
}