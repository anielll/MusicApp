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
import com.couturier.musicapp.PlaylistViewFragment.Companion.deleteFolder
import com.couturier.musicapp.PlaylistViewFragment.OnPlaylistUpdatedListener
import com.couturier.musicapp.PlaylistData.Companion.readPlaylistDataFromFile
import java.io.File

class DeletePlaylistFragment : DialogFragment() {

    private var playlistIndex: Int? = null
    private var listener: OnPlaylistUpdatedListener? = null
    companion object {

        fun newInstance(index: Int): DeletePlaylistFragment {
            val fragment = DeletePlaylistFragment()
            val args = Bundle()
            args.putInt("playlist_index", index)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistIndex = arguments?.getInt("playlist_index")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.delete_playlist, container, false)
        val playlistNameText= view.findViewById<TextView>(R.id.delete_playlist_name)
        val playlistIcon = view.findViewById<ImageView>(R.id.playlist_icon)
        val confirmButton= view.findViewById<Button>(R.id.delete_playlist_confirm_button)
        val cancelButton = view.findViewById<Button>(R.id.delete_playlist_cancel_button)
        val playlistData = readPlaylistDataFromFile(requireContext(),playlistIndex!!)!!
        playlistNameText.text = playlistData.playlistName
        if(playlistData.icon!=null){
            playlistIcon.setImageBitmap(playlistData.icon)
        }
        cancelButton.setOnClickListener {
            dismiss()
        }
        confirmButton.setOnClickListener {
            listener!!.onPlaylistUpdate(null, playlistIndex)
            deleteFolder(File(requireContext().filesDir,"playlists/$playlistIndex"))
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