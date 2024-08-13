package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.couturier.musicapp.PlaylistData.Companion.readPlaylistDataFromFile
import com.couturier.musicapp.PlaylistData.Companion.writePlaylistDataToFile
import com.couturier.musicapp.databinding.AddSongToPlaylistBinding
import com.couturier.musicapp.databinding.PlaylistItemBinding

class AddSongToPlaylistFragment : DialogFragment() {
    private var _binding: AddSongToPlaylistBinding? = null
    private val binding get() =_binding!!
    private var libraryIndex: Int? = null
    private lateinit var playlistObjects: MutableList<PlaylistData>
    companion object { // Get what song this was fragment was called regarding
        private const val ARG_LIBRARY_INDEX = "library_index"
        fun newInstance(index: Int) = AddSongToPlaylistFragment().apply {
            arguments = Bundle().apply { putInt(ARG_LIBRARY_INDEX, index) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryIndex = requireArguments().getInt(ARG_LIBRARY_INDEX)
        playlistObjects = MasterList.playlistList
            .mapNotNull { readPlaylistDataFromFile(it) }
            .filterNot { it.songList.contains(libraryIndex) }
            .toMutableList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = AddSongToPlaylistBinding.inflate(inflater, container, false).apply {
            recyclerView.adapter = PlaylistListAdapter(playlistObjects) { fileIndex ->
                addToPlaylist(fileIndex)
                dismiss()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window?.attributes = requireDialog().window?.attributes?.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = (resources.displayMetrics.heightPixels * 0.5).toInt()
        }
    }

    private fun addToPlaylist(fileIndex: Int) {
        readPlaylistDataFromFile(fileIndex)?.let { otherPlaylist ->
            otherPlaylist.songList.add(libraryIndex!!)
            writePlaylistDataToFile(otherPlaylist)
            Toast.makeText(
                requireContext(),
                "Successfully Saved Song To: ${otherPlaylist.playlistName}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    inner class PlaylistListAdapter(
        private val playlistObjects: MutableList<PlaylistData>,
        private val clickListener: (Int) -> Unit
    ) : RecyclerView.Adapter<PlaylistListAdapter.PlaylistViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):PlaylistViewHolder {
            val binding = PlaylistItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return PlaylistViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
            holder.bind(playlistObjects[position])
        }

        override fun getItemCount(): Int = playlistObjects.size

        inner class PlaylistViewHolder(private val binding:PlaylistItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(playlistObject: PlaylistData) {
                binding.apply {
                    playlistName.text = playlistObject.playlistName
                    itemView.setOnClickListener { clickListener(playlistObject.fileIndex) }
                    (playlistOptionsButton.parent as? ViewGroup)?.removeView(playlistOptionsButton)
                    if (playlistObject.icon != null) {
                        playlistIcon.setImageBitmap(playlistObject.icon)
                    } else {
                        playlistIcon.setImageResource(R.drawable.blank_playlist)
                    }

                }
            }
        }
    }


}