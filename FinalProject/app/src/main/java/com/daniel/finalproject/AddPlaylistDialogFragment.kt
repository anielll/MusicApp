package com.daniel.finalproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class AddPlaylistDialogFragment : DialogFragment() {

    private var listener: PlaylistViewFragment.OnPlaylistUpdatedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_playlist, container, false)
        val saveButton = view.findViewById<Button>(R.id.add_playlist_save_button)
        val cancelButton = view.findViewById<Button>(R.id.add_playlist_cancel_button)
        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
             dismiss()
                val newPlaylistIndex = MasterList.last() + 1
                val playlistNameText = requireView().findViewById<EditText>(R.id.add_playlist_name)
                val newPlaylist = PlaylistData(requireContext(),playlistNameText.text.toString(), mutableListOf(),newPlaylistIndex)
                listener!!.onPlaylistUpdated(newPlaylist,newPlaylistIndex)
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
        listener = context as PlaylistViewFragment.OnPlaylistUpdatedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


}
