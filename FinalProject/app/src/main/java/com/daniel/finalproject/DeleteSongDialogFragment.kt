package com.daniel.finalproject
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.daniel.finalproject.PlaylistViewFragment.OnSongUpdatedListener
import com.daniel.finalproject.SongData.Companion.readSongDataFromFile

class DeleteSongDialogFragment : DialogFragment() {

    private var libraryIndex: Int? = null
    private var listener: OnSongUpdatedListener? = null
    companion object {

        fun newInstance(index: Int): DeleteSongDialogFragment {
            val fragment = DeleteSongDialogFragment()
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
        val view = inflater.inflate(R.layout.delete_song, container, false)

        val titleEditText = view.findViewById<TextView>(R.id.delete_song_text_title)
        val artistEditText = view.findViewById<TextView>(R.id.delete_song_text_artist)
        val confirmButton= view.findViewById<Button>(R.id.delete_song_confirm_button)
        val cancelButton = view.findViewById<Button>(R.id.delete_song_cancel_button)

        val songData = readSongDataFromFile(requireContext(),libraryIndex!!)
        titleEditText.setText(songData?.title)
        artistEditText.setText(songData?.artist)
        cancelButton.setOnClickListener {
            dismiss()
        }
        confirmButton.setOnClickListener {
            listener!!.onSongUpdated(null, libraryIndex)
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
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as OnSongUpdatedListener
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}