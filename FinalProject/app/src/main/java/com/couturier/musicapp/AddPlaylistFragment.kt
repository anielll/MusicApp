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
import com.couturier.musicapp.databinding.AddPlaylistBinding

class AddPlaylistFragment : DialogFragment() {
    // Data Variables
    private var photoSelected = false
    private val filePicker = FilePicker(this)
    private var _binding: AddPlaylistBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  AddPlaylistBinding.inflate(inflater, container, false).apply {
            selectImageButton.setOnClickListener { onSelect() }
            saveButton.setOnClickListener { onSave(); dismiss() }
            cancelButton.setOnClickListener { dismiss() }
            setReturnToCloseKeyboard(playlistName)
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

    private fun onSelect() {
        filePicker.openFilePicker("image/png") { photoUri ->
            binding.selectImageBackground.setImageURI(photoUri)
            photoSelected = true
        }
    }

    private fun onSave() {
        // Create and Save new Playlist
        val newPlaylistIndex = MasterList.nextAvailablePlaylistIndex() + 1
        val newPlaylist = PlaylistData(
            context = requireContext(),
            playlistName = binding.playlistName.text.toString(),
            songList = mutableListOf(),
            playlistIndex = newPlaylistIndex,
            icon = (binding.selectImageBackground.drawable as BitmapDrawable).bitmap.takeIf { photoSelected }
        )
        // Update Main Activity
        (requireContext() as OnPlaylistUpdatedListener).onPlaylistUpdate(
            newPlaylist,
            newPlaylistIndex
        )
        dismiss()
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
