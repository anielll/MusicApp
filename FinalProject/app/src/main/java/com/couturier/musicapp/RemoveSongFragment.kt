package com.couturier.musicapp
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.couturier.musicapp.PlaylistViewFragment.OnSongUpdatedListener
import com.couturier.musicapp.SongData.Companion.readSongDataFromFile

class RemoveSongFragment : DialogFragment() {

    private var libraryIndex: Int? = null
    private var listener: OnSongUpdatedListener? = null
    companion object {

        fun newInstance(index: Int): RemoveSongFragment {
            val fragment = RemoveSongFragment()
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
        val view = inflater.inflate(R.layout.remove_song, container, false)
        val titleText = view.findViewById<TextView>(R.id.remove_song_text_title)
        val artistText = view.findViewById<TextView>(R.id.remove_song_text_artist)
        val songIcon = view.findViewById<ImageView>(R.id.song_icon)
        val confirmButton= view.findViewById<Button>(R.id.remove_song_confirm_button)
        val cancelButton = view.findViewById<Button>(R.id.remove_song_cancel_button)
        val songData = readSongDataFromFile(requireContext(),libraryIndex!!)!!
        titleText.text = songData.title
        artistText.text = songData.artist
        if(songData.icon!=null){
            songIcon.setImageBitmap(songData.icon)
        }
        cancelButton.setOnClickListener {
            dismiss()
        }
        confirmButton.setOnClickListener {
            listener!!.onSongUpdate(null, libraryIndex)
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