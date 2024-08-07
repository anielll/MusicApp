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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.couturier.musicapp.PlaylistViewFragment.OnSongUpdatedListener
import com.couturier.musicapp.SongData.Companion.importMp3FromInputStream
import com.couturier.musicapp.SongData.Companion.parseMetaData

class AddSongFragment : DialogFragment() {
    //Data Variables
    private lateinit var selectedMp3Uri: Uri
    private lateinit var selectedMp3Name: String
    private val filePicker = FilePicker(this)
    private var photoSelected = false
    //View variables
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var selectSongButton: Button
    private lateinit var titleEditText: EditText
    private lateinit var artistEditText: EditText
    private lateinit var selectBackground: ImageView
    private lateinit var selectButton: Button
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_song, container, false).apply {
            saveButton = findViewById(R.id.save_button)
            cancelButton = findViewById(R.id.cancel_button)
            selectSongButton = findViewById(R.id.select_song_button)
            titleEditText = findViewById(R.id.song_title)
            artistEditText = findViewById(R.id.song_artist)
            selectBackground = findViewById(R.id.select_image_background)
            selectButton = findViewById(R.id.select_image_button)

            cancelButton.setOnClickListener { dismiss() }
            saveButton.setOnClickListener { onSaveSong(); dismiss() }
            selectSongButton.setOnClickListener { onSelectSong() }
            selectButton.setOnClickListener { onSelectPhoto() }
            setReturnToCloseKeyboard(titleEditText)
            setReturnToCloseKeyboard(artistEditText)
        }
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
            getFileNameFromUri(requireContext(),uri).let {
                if (!it.endsWith(".mp3")) {
                    Toast.makeText(
                        requireContext(),
                        "Invalid File: Selected file must be .mp3",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    selectedMp3Uri = uri
                    selectedMp3Name = it
                    selectSongButton.text = it
                    val metadata = requireContext().contentResolver.openFileDescriptor(uri, "r")?.use { uriFD ->
                        parseMetaData(uriFD.fileDescriptor, selectedMp3Name)
                    }!!
                    titleEditText.setText(metadata.title)
                    artistEditText.setText(metadata.artist)
                    selectBackground.setImageBitmap(metadata.icon)
                    photoSelected = true
                }
            }
        }
    }
    private fun onSelectPhoto(){
        filePicker.openFilePicker("image/png") { photoUri ->
            selectBackground.setImageURI(photoUri)
            photoSelected = true
        }
    }
    private fun onSaveSong() {
        val newSongIndex = MasterList.addSong()
        val newSong = SongData(
            context = requireContext(),
            title = titleEditText.text.toString(),
            artist = artistEditText.text.toString(),
            songIndex = newSongIndex,
            songIcon = (selectBackground.drawable as BitmapDrawable).bitmap.takeIf { photoSelected }
        )
        requireContext().contentResolver.openInputStream(selectedMp3Uri)!!.use { inputStream ->
            importMp3FromInputStream(requireContext(), inputStream, newSongIndex,selectedMp3Name)
        }
        (requireContext() as OnSongUpdatedListener).onSongUpdate(newSong, newSongIndex)
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
    private fun getFileNameFromUri(context:Context, uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null).use { cursor ->
            if (cursor != null) {
                cursor.moveToFirst()
                val colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (colIndex != -1) {
                    cursor.getString(colIndex)
                }
            }
        }
        return uri.lastPathSegment!!
    }
}
