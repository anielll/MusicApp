package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.couturier.musicapp.OnStartAssetManager.Companion.deleteFolder
import com.couturier.musicapp.PlaylistData.OnPlaylistUpdatedListener
import com.couturier.musicapp.PlaylistData.Companion.readPlaylistDataFromFile
import com.couturier.musicapp.databinding.DeletePlaylistBinding
import java.io.File

class DeletePlaylistFragment : DialogFragment() {
    private lateinit var playlistData: PlaylistData
    private var fileIndex: Int? = null
    private var _binding: DeletePlaylistBinding? = null
    private val binding get() = _binding!!

    companion object { // Get what playlist this was fragment was called regarding
        private const val ARG_FILE_INDEX = "file_index"
        fun newInstance(index: Int) = DeletePlaylistFragment().apply {
            arguments = Bundle().apply { putInt(ARG_FILE_INDEX, index) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileIndex = requireArguments().getInt(ARG_FILE_INDEX)
        playlistData = readPlaylistDataFromFile(requireContext(), fileIndex!!)!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DeletePlaylistBinding.inflate(inflater, container, false).apply {
            playlistName.text = playlistData.playlistName
            playlistData.icon?.let {playlistIcon.setImageBitmap(it)}
            cancelButton.setOnClickListener { dismiss() }
            confirmButton.setOnClickListener { onDelete();dismiss() }
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

    private fun onDelete() {
        (requireContext() as OnPlaylistUpdatedListener).onPlaylistUpdate(null, fileIndex)
        deleteFolder(File(requireContext().filesDir, "playlists/$fileIndex"))
    }

}