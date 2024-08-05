package com.couturier.musicapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.couturier.musicapp.PlaylistViewFragment.OnSongUpdatedListener
import com.couturier.musicapp.SongData.Companion.SongMetadata
import com.couturier.musicapp.SongData.Companion.titleAndArtistFromFileName
import com.couturier.musicapp.SongData.Companion.toBitMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddSongFragment : DialogFragment() {

    private var listener: OnSongUpdatedListener? = null
    private var selectedMp3Uri: Uri? = null
    private var selectedMp3Name: String? = null
    private val filePicker = FilePicker(this)
    private var photoSelected = false

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
        val selectBackground = view.findViewById<ImageView>(R.id.select_image_background)
        val selectButton = view.findViewById<Button>(R.id.select_image_button)
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
            filePicker.openFilePicker("audio/mpeg") { uri ->
                selectedMp3Uri = uri
                selectedMp3Name = getFileNameFromUri(uri)
                if (selectedMp3Name != null) {
                    mp3Button.text = selectedMp3Name
                    val metadata = parseMetaData(requireContext(), uri)
                    titleEditText.setText(metadata.title)
                    artistEditText.setText(metadata.artist)
                    selectBackground.setImageBitmap(metadata.icon)
                    photoSelected = true
                }
            }
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
        selectButton.setOnClickListener{
            filePicker.openFilePicker("image/png"){ photoUri ->
                selectBackground.setImageURI(photoUri)
                photoSelected = true
            }
        }

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


    private fun saveCurrentUri(){
        val library = PlaylistData.readPlaylistDataFromFile(requireContext(),-1)!!
        val newSongIndex = if(library.songList.isEmpty()) {
            0
        }else {
             library.songList.last() + 1
        }
        val titleEditText = requireView().findViewById<EditText>(R.id.add_song_title)
        val artistEditText = requireView().findViewById<EditText>(R.id.add_song_artist)
        val selectBackground = requireView().findViewById<ImageView>(R.id.select_image_background)
        val art = if(photoSelected){
            (selectBackground.drawable as BitmapDrawable).bitmap
        }else{
            null
        }
        val newSong = SongData(requireContext(),titleEditText.text.toString(),artistEditText.text.toString(),newSongIndex, art)
        copyMp3ToInternalStorage(newSongIndex)
        library.songList.add(newSongIndex)
        PlaylistData.writePlaylistDataToFile(requireContext(),library)
        listener!!.onSongUpdate(newSong,newSongIndex)
    }

    private fun copyMp3ToInternalStorage(newSongIndex: Int){
        val file = File(requireContext().filesDir, "songs/$newSongIndex")
        if (!file.exists()) {
            file.mkdirs()
        }
        val destinationFile = File(file, selectedMp3Name!!)
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
        return
    }

    private fun parseMetaData(context: Context, uri: Uri): SongMetadata {
        val retriever = MediaMetadataRetriever()
        var title = ""
        var artist = ""
        var icon: Bitmap? = null
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use {uriFile ->
                retriever.setDataSource(uriFile.fileDescriptor)
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                val artByteArray= retriever.embeddedPicture
                icon = toBitMap(artByteArray)
            }
        } catch (e: Exception) {
            // use default values of "", "", empty
        } finally {
            retriever.release()
        }
        val (inferredTitle: String, inferredArtist: String) = titleAndArtistFromFileName(getFileNameFromUri(uri)!!.substringAfterLast('/'))
        if(title == "" || artist ==""){
            title = inferredTitle
            artist= inferredArtist

        }
        return SongMetadata(title, artist, icon)
    }
    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                fileName = if (colIndex != -1) {
                    cursor.getString(colIndex)
                }else{
                    "ERROR"
                }
            }
        }
        return fileName
    }

}
