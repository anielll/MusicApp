package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SongOptionsFragment : BottomSheetDialogFragment() {

    private var libraryIndex: Int = -1
    private var playlistNumber: Int = -1
    companion object {
        fun newInstance(libraryIndex:Int, playlistNumber: Int): SongOptionsFragment {
            val fragment = SongOptionsFragment()
            val args = Bundle()
            args.putInt("library_index", libraryIndex)
            args.putInt("playlist_number", playlistNumber)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryIndex= requireArguments().getInt("library_index")
        playlistNumber= requireArguments().getInt("playlist_number")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.song_options, container, false)

        view.findViewById<Button>(R.id.add_to_button).setOnClickListener {
            addTo()
            dismiss()
        }
        view.findViewById<Button>(R.id.edit_song_button).setOnClickListener {
            editSong()
            dismiss()
        }
        val removeSongButton = view.findViewById<Button>(R.id.remove_song_button)
        if (playlistNumber==-1) {
            (removeSongButton.parent as? ViewGroup)?.removeView(removeSongButton)
        }else {
            removeSongButton.setOnClickListener {
                removeFrom()
                dismiss()
            }
        }
        view.findViewById<Button>(R.id.delete_song_button).setOnClickListener {
            deleteSong()
            dismiss()
        }

        return view
    }
    private fun addTo(){
        val addSongToPlaylistFragment = AddSongToPlaylistFragment.newInstance(libraryIndex)
        addSongToPlaylistFragment.show(parentFragmentManager, "AddSongToPlaylistFragment")
    }
    private fun editSong() {
        val editSongFragment = EditSongFragment.newInstance(libraryIndex)
        editSongFragment.show(parentFragmentManager, "EditSongFragment")
    }
    private fun removeFrom(){
        val removeSongFragment= RemoveSongFragment.newInstance(libraryIndex)
        removeSongFragment.show(parentFragmentManager, "RemoveSongFragment")
    }
    private fun deleteSong() {
        val deleteSongFragment = DeleteSongFragment.newInstance(libraryIndex)
        deleteSongFragment.show(parentFragmentManager, "DeleteSongFragment")
    }
}