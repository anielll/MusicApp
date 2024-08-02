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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class AddPlaylistFragment : DialogFragment() {

    private var listener: PlaylistViewFragment.OnPlaylistUpdatedListener? = null
    private lateinit var photoPicker: PhotoPicker
    private var photoSelected = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_playlist, container, false)
        val saveButton = view.findViewById<Button>(R.id.add_playlist_save_button)
        val cancelButton = view.findViewById<Button>(R.id.add_playlist_cancel_button)
        val playlistNameEditText = view.findViewById<EditText>(R.id.add_playlist_name)
        val selectBackground = view.findViewById<ImageView>(R.id.select_image_background)
        val selectButton = view.findViewById<Button>(R.id.select_image_button)
        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
             dismiss()
                val newPlaylistIndex = MasterList.nextAvailableIndex() + 1
                val playlistNameText = requireView().findViewById<EditText>(R.id.add_playlist_name)
                val art = if(photoSelected){
                    (selectBackground.drawable as BitmapDrawable).bitmap
                }else{
                    null
                }
                val newPlaylist = PlaylistData(requireContext(),playlistNameText.text.toString(), mutableListOf(),newPlaylistIndex,art)
                listener!!.onPlaylistUpdate(newPlaylist,newPlaylistIndex)
        }

        playlistNameEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val imm = playlistNameEditText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(playlistNameEditText.windowToken, 0)
                playlistNameEditText.clearFocus()
                return@OnEditorActionListener true
            }
            false
        })
        photoPicker = PhotoPicker(this) { photoUri ->
            selectBackground.setImageURI(photoUri)
            photoSelected = true
        }
        selectButton.setOnClickListener{
            photoPicker.openPhotoPicker()
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
