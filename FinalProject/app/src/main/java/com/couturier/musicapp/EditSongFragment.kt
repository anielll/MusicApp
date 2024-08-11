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
import com.couturier.musicapp.SongData.Companion.readSongDataFromFile
import com.couturier.musicapp.PlaylistViewFragment.OnSongUpdatedListener
import com.couturier.musicapp.databinding.EditSongBinding

class EditSongFragment : DialogFragment() {

    private var libraryIndex: Int? = null
    private val  filePicker = FilePicker(this)
    private var photoSelected = false
    private lateinit var songData: SongData
    private var _binding: EditSongBinding? = null
    private val binding get() = _binding!!
    companion object { // Get what Song this was fragment was called regarding
        private const val ARG_LIBRARY_INDEX = "library_index"
        fun newInstance(index: Int) = EditSongFragment().apply {
            arguments = Bundle().apply { putInt(ARG_LIBRARY_INDEX, index) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryIndex = requireArguments().getInt(ARG_LIBRARY_INDEX)
        songData = readSongDataFromFile(requireContext(),libraryIndex!!)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditSongBinding.inflate(inflater, container, false).apply{
            editSongTitle.setText(songData.title)
            editSongArtist.setText(songData.artist)
            songData.icon?.let{selectImageBackground.setImageBitmap(it)}
            cancelButton.setOnClickListener {dismiss()}
            saveButton.setOnClickListener {onSave();dismiss()}
            selectImageButton.setOnClickListener{onSelect() }
            setReturnToCloseKeyboard(editSongTitle)
            setReturnToCloseKeyboard(editSongArtist)
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
    private fun onSave(){
        val updatedSong =
            SongData(
                context = requireContext(),
                title = binding.editSongTitle.text.toString(),
                artist = binding.editSongArtist.text.toString(),
                songIndex =  libraryIndex!!,
                songIcon = (binding.selectImageBackground.drawable as BitmapDrawable).bitmap.takeIf { photoSelected }
            )
        (requireContext() as OnSongUpdatedListener).onSongUpdate(updatedSong)
    }
    private fun onSelect(){
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