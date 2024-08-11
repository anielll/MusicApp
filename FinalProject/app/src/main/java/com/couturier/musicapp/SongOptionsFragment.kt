package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.couturier.musicapp.databinding.SongOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SongOptionsFragment : BottomSheetDialogFragment() {

    private var libraryIndex: Int? = null
    private var fileIndex: Int? = null
    private var _binding: SongOptionsBinding? = null
    private val binding get() = _binding!!
    companion object {
        private const val ARG_LIBRARY_INDEX = "library_index"
        private const val ARG_FILE_INDEX= "file_index"
        fun newInstance(libraryIndex: Int ,fileIndex: Int) = SongOptionsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_LIBRARY_INDEX, libraryIndex)
                putInt(ARG_FILE_INDEX, fileIndex)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryIndex= requireArguments().getInt(ARG_LIBRARY_INDEX)
        fileIndex= requireArguments().getInt(ARG_FILE_INDEX)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SongOptionsBinding.inflate(inflater, container, false).apply{
            addToButton.setOnClickListener { addTo() ;dismiss()}
            editSongButton.setOnClickListener {editSong();dismiss()}
            if (fileIndex==-1) { // Library doesn't have removeSongButton
                (removeSongButton.parent as ViewGroup).removeView(removeSongButton)
            }else {
                removeSongButton.setOnClickListener {removeFrom(); dismiss()}
            }
            deleteSongButton.setOnClickListener { deleteSong() ;dismiss() }
        }
        return binding.root
    }
    private fun addTo(){
        AddSongToPlaylistFragment.newInstance(libraryIndex!!)
            .show(parentFragmentManager, "AddSongToPlaylistFragment")
    }
    private fun editSong() {
        EditSongFragment.newInstance(libraryIndex!!)
            .show(parentFragmentManager, "EditSongFragment")
    }
    private fun removeFrom(){
        RemoveSongFragment.newInstance(libraryIndex!!)
            .show(parentFragmentManager, "RemoveSongFragment")
    }
    private fun deleteSong() {
        DeleteSongFragment.newInstance(libraryIndex!!)
            .show(parentFragmentManager, "DeleteSongFragment")
    }
}