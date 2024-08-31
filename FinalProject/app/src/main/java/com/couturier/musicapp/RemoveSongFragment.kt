package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.couturier.musicapp.SongData.Companion.readSongDataFromFile
import com.couturier.musicapp.databinding.RemoveSongBinding

class RemoveSongFragment : DialogFragment() {

    private var libraryIndex: Int? = null
    private lateinit var songData: SongData
    private var _binding: RemoveSongBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistViewModel by activityViewModels()
    companion object { // Get what Song this was fragment was called regarding
        private const val ARG_LIBRARY_INDEX = "library_index"
        fun newInstance(index: Int) = RemoveSongFragment().apply {
            arguments = Bundle().apply { putInt(ARG_LIBRARY_INDEX, index) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryIndex = requireArguments().getInt(ARG_LIBRARY_INDEX)
        songData = readSongDataFromFile(libraryIndex!!)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RemoveSongBinding.inflate(inflater, container, false).apply {
            songTitle.text = songData.title
            songArtist.text = songData.artist
            songData.icon?.let{songIcon.setImageBitmap(it)}
            cancelButton.setOnClickListener {dismiss()}
            confirmButton.setOnClickListener { onRemove();dismiss()}
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    private fun onRemove(){
        viewModel.deleteSong(libraryIndex!!)
    }
}