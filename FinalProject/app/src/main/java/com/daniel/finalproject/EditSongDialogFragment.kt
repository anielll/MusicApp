package com.daniel.finalproject
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.daniel.finalproject.SongData.Companion.readSongDataFromFile

class EditSongDialogFragment : DialogFragment() {

    private var libraryIndex: Int? = null

    interface OnSongUpdatedListener {
        fun onSongUpdated(newSong:SongData,libraryIndex: Int)

    }
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

        val songData = readSongDataFromFile(requireContext(),libraryIndex)
        titleEditText.setText(songData?.title)
        artistEditText.setText(songData?.artist)
        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val artist = artistEditText.text.toString()
            val updatedSong = SongData(requireContext(), title, artist, libraryIndex!!)
            val listener = requireActivity() as OnSongUpdatedListener
            listener.onSongUpdated(updatedSong, libraryIndex!!)
            dismiss()
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

}