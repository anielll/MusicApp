package com.daniel.finalproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SongOptionsDialogFragment : BottomSheetDialogFragment() {

    private var libraryIndex: Int = -1

    companion object {
        fun newInstance(libraryIndex:Int): SongOptionsDialogFragment {
            val fragment = SongOptionsDialogFragment()
            val args = Bundle()
            args.putInt("library_index", libraryIndex)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryIndex= arguments?.getInt("library_index") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.song_options, container, false)

        view.findViewById<Button>(R.id.editSongButton).setOnClickListener {
            editSong()
            dismiss()
        }

        view.findViewById<Button>(R.id.deleteSongButton).setOnClickListener {
            deleteSong()
            dismiss()
        }

        return view
    }

    private fun editSong() {
        val editSongDialogFragment = EditSongDialogFragment.newInstance(libraryIndex)
        editSongDialogFragment.show(parentFragmentManager, "EditSongDialogFragment")
    }

    private fun deleteSong() {
        Log.i("SongOptions", "Delete song clicked for index: $libraryIndex")
    }
}