package com.daniel.finalproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.daniel.finalproject.PlaylistViewFragment.OnSongUpdatedListener
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddSongDialogFragment : DialogFragment() {

    private var listener: OnSongUpdatedListener? = null
    private var selectedMp3Uri: Uri? = null
    private var selectedMp3Name: String? = null
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    selectedMp3Uri = uri
                    selectedMp3Name = getFileNameFromUri(uri)
                    val titleEditText = requireView().findViewById<EditText>(R.id.add_song_title)
                    val btnSelectSong = view?.findViewById<Button>(R.id.add_song_mp3_button)!!
                    if (selectedMp3Name != null) {
                        btnSelectSong.text = selectedMp3Name
                        titleEditText.setText(selectedMp3Name)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_song, container, false)
        val saveButton = view.findViewById<Button>(R.id.add_song_save_button)
        val cancelButton = view.findViewById<Button>(R.id.add_song_cancel_button)
        val mp3Button: Button = view.findViewById(R.id.add_song_mp3_button)
        val titleEditText = view.findViewById<EditText>(R.id.add_song_title)
        val artistEditText = view.findViewById<EditText>(R.id.add_song_artist)

        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
        if(selectedMp3Uri== null || selectedMp3Name == null ){
            Toast.makeText(requireContext(), "Failed to save song: Invalid file selected", Toast.LENGTH_SHORT).show()
        }else if(!selectedMp3Name!!.endsWith("mp3")){
            Toast.makeText(requireContext(), "Failed to save song: Selected file must be .mp3", Toast.LENGTH_SHORT).show()
        }else{
            saveCurrentUri()
            dismiss()
        }
        }
        mp3Button.setOnClickListener {
            openFilePicker()
        }
        titleEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm =titleEditText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(titleEditText.windowToken, 0)
                titleEditText.clearFocus()
                return@OnEditorActionListener true
            }
            false
        })
        artistEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm = artistEditText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(artistEditText.windowToken, 0)
                artistEditText.clearFocus()
                return@OnEditorActionListener true
            }
            false
        })

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as OnSongUpdatedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/mpeg"
        }
        filePickerLauncher.launch(intent)
    }

    private fun saveCurrentUri(){
        val library = PlaylistData.readPlaylistDataFromFile(requireContext(),-1)!!
        val newSongIndex = if(library.songList.isEmpty()) {
            0
        }else {
             library.songList.last() + 1
        }
        val titleEditText = requireView().findViewById<EditText>(R.id.add_song_title)
        val artistEditText = requireView().findViewById<EditText>(R.id.add_song_artist)
        val newSong = SongData(requireContext(),titleEditText.text.toString(),artistEditText.text.toString(),newSongIndex)
        copyMp3ToInternalStorage(newSongIndex)
        library.songList.add(newSongIndex)
        PlaylistData.writePlaylistDataToFile(requireContext(),library)
        listener!!.onSongUpdated(newSong,newSongIndex)
    }

    private fun copyMp3ToInternalStorage(newSongIndex: Int): File? {
        val file = File(requireContext().filesDir, "songs/$newSongIndex")
        if (!file.exists()) {
            file.mkdirs()
        }
        val destinationFile = File(file, selectedMp3Name)
        val inputStream: InputStream = requireContext().contentResolver.openInputStream(selectedMp3Uri!!)!!
        val outputStream = FileOutputStream(destinationFile)
        val buffer = ByteArray(4 * 1024)
        while (true) {
            val temp = inputStream.read(buffer)
            if (temp == -1) {
                break
            }
            outputStream.write(buffer, 0, temp)
        }
        inputStream.close()
        outputStream.close()
        return destinationFile
    }
    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (colIndex != -1) {
                    fileName = cursor.getString(colIndex)
                }else{
                    fileName = "ERROR"
                }
            }
        }
        return fileName
    }

}
