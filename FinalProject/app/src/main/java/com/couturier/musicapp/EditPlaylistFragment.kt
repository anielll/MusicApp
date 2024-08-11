package com.couturier.musicapp

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.couturier.musicapp.PlaylistViewFragment.OnPlaylistUpdatedListener
import com.couturier.musicapp.PlaylistData.Companion.readPlaylistDataFromFile
import com.couturier.musicapp.databinding.EditPlaylistBinding

class EditPlaylistFragment : DialogFragment() {
    private var fileIndex: Int? = null
    private val filePicker = FilePicker(this)
    private var photoSelected = false
    private lateinit var playlistData: PlaylistData
    private var _binding: EditPlaylistBinding? = null
    private val binding get() = _binding!!

    companion object { // Get what Playlist this was fragment was called regarding
        private const val ARG_FILE_INDEX = "file_index"
        fun newInstance(index: Int) = EditPlaylistFragment().apply {
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
        _binding = EditPlaylistBinding.inflate(inflater, container, false).apply {
            editPlaylistName.setText(playlistData.playlistName)
            cancelButton.setOnClickListener { dismiss() }
            saveButton.setOnClickListener { onSave();dismiss() }
            selectImageButton.setOnClickListener { onSelect() }
            setReturnToCloseKeyboard(editPlaylistName)
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

    private fun onSave() {
        val updatedPlaylist = PlaylistData(
            context = requireContext(),
            playlistName = binding.editPlaylistName.text.toString(),
            songList =  playlistData.songList,
            playlistIndex= playlistData.fileIndex,
            icon = (binding.selectImageBackground.drawable as BitmapDrawable).bitmap.takeIf { photoSelected }
        )
        (requireContext() as OnPlaylistUpdatedListener).onPlaylistUpdate(updatedPlaylist)
    }

    private fun onSelect() {
        filePicker.openFilePicker("image/png") { photoUri ->
            binding.selectImageBackground.setImageURI(photoUri)
            photoSelected = true
        }
    }

    private fun setReturnToCloseKeyboard(editText: EditText) {
        editText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                (editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(editText.windowToken, 0)
                editText.clearFocus()
                true
            } else {
                false
            }
        }
    }


}