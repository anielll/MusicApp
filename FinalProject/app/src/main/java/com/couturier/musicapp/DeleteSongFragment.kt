package com.couturier.musicapp
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.couturier.musicapp.PlaylistViewFragment.Companion.deleteFolder
import com.couturier.musicapp.PlaylistViewFragment.OnSongUpdatedListener
import com.couturier.musicapp.SongData.Companion.readSongDataFromFile
import com.couturier.musicapp.databinding.DeleteSongBinding
import java.io.File

class DeleteSongFragment : DialogFragment() {

    private var libraryIndex: Int? = null
    private lateinit var songData: SongData
    private var _binding: DeleteSongBinding? = null
    private val binding get() = _binding!!
        companion object { // Get what Song this was fragment was called regarding
            private const val ARG_LIBRARY_INDEX = "library_index"
            fun newInstance(index: Int) = DeleteSongFragment().apply {
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
        _binding = DeleteSongBinding.inflate(inflater, container, false).apply{
            songTitle.text = songData.title
            songArtist.text = songData.artist
            songData.icon?.let{songIcon.setImageBitmap(songData.icon)}
            cancelButton.setOnClickListener {dismiss()}
            confirmButton.setOnClickListener {onDelete();dismiss()}
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
    private fun onDelete(){
        (requireContext() as OnSongUpdatedListener).onSongUpdate(null, libraryIndex)
        deleteFolder(File(requireContext().filesDir,"songs/$libraryIndex"))
    }


}