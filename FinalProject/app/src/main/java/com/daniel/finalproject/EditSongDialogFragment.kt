package com.daniel.finalproject
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.daniel.finalproject.SongData.Companion.readSongDataFromFile
import com.daniel.finalproject.PlaylistViewFragment.OnSongUpdatedListener
class EditSongDialogFragment : DialogFragment() {

    private var libraryIndex: Int? = null
    private var listener: OnSongUpdatedListener? = null
    companion object {

        fun newInstance(index: Int): EditSongDialogFragment {
            val fragment = EditSongDialogFragment()
            val args = Bundle()
            args.putInt("library_index", index)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryIndex = arguments?.getInt("library_index")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.edit_song, container, false)

        val titleEditText = view.findViewById<EditText>(R.id.edit_song_title)
        val artistEditText = view.findViewById<EditText>(R.id.edit_song_artist)
        val saveButton = view.findViewById<Button>(R.id.edit_song_save_button)
        val cancelButton = view.findViewById<Button>(R.id.edit_song_cancel_button)

        val songData = readSongDataFromFile(requireContext(),libraryIndex!!)
        titleEditText.setText(songData?.title)
        artistEditText.setText(songData?.artist)
        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val artist = artistEditText.text.toString()
            val updatedSong = SongData(requireContext(), title, artist, libraryIndex!!)
            listener!!.onSongUpdated(updatedSong)
            dismiss()
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

}