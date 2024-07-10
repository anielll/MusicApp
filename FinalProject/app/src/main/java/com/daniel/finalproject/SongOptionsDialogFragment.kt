package com.daniel.finalproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SongOptionsDialogFragment : BottomSheetDialogFragment() {

    private var songIndex: Int = -1

    companion object {
        fun newInstance(songIndex: Int): SongOptionsDialogFragment {
            val fragment = SongOptionsDialogFragment()
            val args = Bundle()
            args.putInt("song_index", songIndex)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songIndex = arguments?.getInt("song_index") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.song_options, container, false)

        view.findViewById<Button>(R.id.editSongButton).setOnClickListener {
            editSong(songIndex)
            dismiss()
        }

        view.findViewById<Button>(R.id.deleteSongButton).setOnClickListener {
            deleteSong(songIndex)
            dismiss()
        }

        return view
    }

    private fun editSong(recyclerViewIndex: Int) {
        val editSongDialogFragment = EditSongDialogFragment.newInstance(recyclerViewIndex)
        editSongDialogFragment.show(parentFragmentManager, "EditSongDialogFragment")
    }

    private fun deleteSong(songIndex: Int) {
        Log.i("SongOptions", "Delete song clicked for index: $songIndex")
    }
}