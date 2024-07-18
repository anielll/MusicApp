package com.daniel.finalproject
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.daniel.finalproject.PlaylistViewFragment.OnPlaylistUpdatedListener
import com.daniel.finalproject.PlaylistData.Companion.readPlaylistDataFromFile
class EditPlaylistDialogFragment : DialogFragment() {
    private var playlistIndex: Int? = null
    private var listener: OnPlaylistUpdatedListener? = null
    companion object {
        fun newInstance(index: Int): EditPlaylistDialogFragment {
            val fragment = EditPlaylistDialogFragment()
            val args = Bundle()
            args.putInt("playlist_index", index)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistIndex= arguments?.getInt("playlist_index")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.edit_playlist, container, false)

        val playlistNameEditText= view.findViewById<EditText>(R.id.edit_playlist_name)
        val saveButton = view.findViewById<Button>(R.id.edit_playlist_save_button)
        val cancelButton = view.findViewById<Button>(R.id.edit_playlist_cancel_button)
        val playlistData = readPlaylistDataFromFile(requireContext(),playlistIndex!!)!!
        playlistNameEditText.setText(playlistData.playlistName)
        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
            val playlistName = playlistNameEditText.text.toString()
            val updatedPlaylist = PlaylistData(requireContext(),playlistName,playlistData.songList,playlistData.fileIndex)
            listener!!.onPlaylistUpdated(updatedPlaylist)
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
        listener = context as OnPlaylistUpdatedListener
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}