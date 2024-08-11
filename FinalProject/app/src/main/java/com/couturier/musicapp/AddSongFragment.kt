package com.couturier.musicapp

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.couturier.musicapp.PlaylistViewFragment.OnSongUpdatedListener
import com.couturier.musicapp.SongData.Companion.importMp3FromInputStream
import com.couturier.musicapp.SongData.Companion.parseMetaData
import com.couturier.musicapp.databinding.AddSongBinding

class AddSongFragment : DialogFragment() {
    // Data Variables
    private var selectedMp3Uri: Uri? = null
    private val filePicker = FilePicker(this)
    private var photoSelected = false
    private var _binding: AddSongBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = AddSongBinding.inflate(inflater, container, false).apply {
            cancelButton.setOnClickListener { dismiss() }
            saveButton.setOnClickListener { onSaveSong(); dismiss() }
            selectSongButton.setOnClickListener { onSelectSong() }
            selectImageButton.setOnClickListener { onSelectPhoto() }
            setReturnToCloseKeyboard(songTitle)
            setReturnToCloseKeyboard(songArtist)
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


    private fun onSelectSong() {
        filePicker.openFilePicker("audio/mpeg") { uri ->
            // If ends with.mp3
            val fileName = getFileNameFromUri(uri)
            if (!fileName.endsWith(".mp3")) {
                Toast.makeText(
                    requireContext(),
                    "Invalid File: Selected file must be .mp3",
                    Toast.LENGTH_SHORT
                ).show()
                return@openFilePicker
            }
            // If can read from file
            val metadata = requireContext().contentResolver.openFileDescriptor(uri, "r")
                ?.use { uriFD -> parseMetaData(uriFD.fileDescriptor, fileName) }
                ?: run {
                    Toast.makeText(
                        requireContext(),
                        "Error Saving File",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@openFilePicker
                }
            // Update visual elements with metadata from selected file
            selectedMp3Uri = uri
            binding.selectSongButton.text = fileName
            binding.songTitle.setText(metadata.title)
            binding.songArtist.setText(metadata.artist)
            if(metadata.icon!=null){
                binding.selectImageBackground.setImageBitmap(metadata.icon)
                photoSelected = true
            }
        }
    }


    private fun onSelectPhoto() {
        filePicker.openFilePicker("image/png") { photoUri ->
            binding.selectImageBackground.setImageURI(photoUri)
            photoSelected = true
        }
    }

    private fun onSaveSong() {
        // Create and Save new Song
        selectedMp3Uri?.let { uri ->
            val newSongIndex = MasterList.addSong()
            val newSong = SongData(
                context = requireContext(),
                title = binding.songTitle.text.toString(),
                artist = binding.songArtist.text.toString(),
                songIndex = newSongIndex,
                songIcon = (binding.selectImageBackground.drawable as BitmapDrawable).bitmap.takeIf { photoSelected }
            )
            // Copy mp3 from external storage to local
            requireContext().contentResolver.openInputStream(uri)!!.use { inputStream ->
                importMp3FromInputStream(
                    requireContext(),
                    inputStream,
                    newSongIndex,
                    binding.selectSongButton.text.toString()
                )
            }
            // Update PlaylistViewFragment via Main Activity
            (requireContext() as OnSongUpdatedListener).onSongUpdate(newSong, newSongIndex)
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

    private fun getFileNameFromUri(uri: Uri): String =
        // Get "DISPLAY_NAME" else "lastPathSegment" else ERROR (which is processed as invalid because no .mp3)
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).let { colIndex ->
                cursor.getString(colIndex)
                    .takeIf { colIndex != -1 }
                    ?: uri.lastPathSegment
                    ?: "ERROR"
            }
        } ?: "ERROR"
}
