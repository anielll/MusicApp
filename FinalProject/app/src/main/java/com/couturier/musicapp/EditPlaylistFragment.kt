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
import com.couturier.musicapp.PlaylistViewFragment.OnPlaylistUpdatedListener
import com.couturier.musicapp.PlaylistData.Companion.readPlaylistDataFromFile
class EditPlaylistFragment : DialogFragment() {
    private var playlistIndex: Int? = null
    private var listener: OnPlaylistUpdatedListener? = null
    private lateinit var photoPicker: PhotoPicker
    private var photoSelected = false
    companion object {
        fun newInstance(index: Int): EditPlaylistFragment {
            val fragment = EditPlaylistFragment()
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
        val selectBackground = view.findViewById<ImageView>(R.id.select_image_background)
        val selectButton = view.findViewById<Button>(R.id.select_image_button)
        playlistNameEditText.setText(playlistData.playlistName)
        cancelButton.setOnClickListener {
            dismiss()
        }
        saveButton.setOnClickListener {
            val playlistName = playlistNameEditText.text.toString()
            val art = if(photoSelected){
                (selectBackground.drawable as BitmapDrawable).bitmap
            }else{
                null
            }
            val updatedPlaylist = PlaylistData(requireContext(),playlistName,playlistData.songList,playlistData.fileIndex,art)
            listener!!.onPlaylistUpdate(updatedPlaylist)
            dismiss()
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
        listener = context as OnPlaylistUpdatedListener
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}