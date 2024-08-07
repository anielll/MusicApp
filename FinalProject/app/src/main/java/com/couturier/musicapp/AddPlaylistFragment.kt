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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.couturier.musicapp.PlaylistViewFragment.OnPlaylistUpdatedListener

class AddPlaylistFragment : DialogFragment() {
    // Data Variables
    private var photoSelected = false
    private val filePicker = FilePicker(this)
    // View Variables
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var selectButton: Button
    private lateinit var playlistNameEditText: EditText
    private lateinit var selectBackground: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_playlist, container, false).apply {
            saveButton = findViewById(R.id.save_button)
            cancelButton = findViewById(R.id.cancel_button)
            playlistNameEditText = findViewById(R.id.playlist_name)
            selectBackground = findViewById(R.id.select_image_background)
            selectButton = findViewById(R.id.select_image_button)

            selectButton.setOnClickListener { onSelect() }
            saveButton.setOnClickListener { onSave(); dismiss() }
            cancelButton.setOnClickListener { dismiss() }
            setReturnToCloseKeyboard(playlistNameEditText)
        }
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
            selectBackground.setImageURI(photoUri)
            photoSelected = true
        }
    }

    private fun onSave() {
        val newPlaylistIndex = MasterList.nextAvailablePlaylistIndex() + 1
        val newPlaylist = PlaylistData(
            context = requireContext(),
            playlistName = playlistNameEditText.text.toString(),
            songList = mutableListOf(),
            playlistIndex = newPlaylistIndex,
            icon = (selectBackground.drawable as BitmapDrawable).bitmap.takeIf { photoSelected }
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
