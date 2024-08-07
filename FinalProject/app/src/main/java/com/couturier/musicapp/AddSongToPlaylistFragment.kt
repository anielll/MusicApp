package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.couturier.musicapp.PlaylistData.Companion.readPlaylistDataFromFile
import com.couturier.musicapp.PlaylistData.Companion.writePlaylistDataToFile

class AddSongToPlaylistFragment : DialogFragment() {
    private var libraryIndex: Int? = null
    private lateinit var playlistObjects: MutableList<PlaylistData>

    companion object {
        private const val ARG_LIBRARY_INDEX = "library_index"
        fun newInstance(index: Int): AddSongToPlaylistFragment {
            return AddSongToPlaylistFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LIBRARY_INDEX, index)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        libraryIndex = arguments?.getInt(ARG_LIBRARY_INDEX)
        playlistObjects = MasterList.playlistList
            .mapNotNull { readPlaylistDataFromFile(requireContext(), it) }
            .filter { !it.songList.contains(libraryIndex) }
            .toMutableList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_song_to_playlist, container, false).apply {
            val recyclerView: RecyclerView = findViewById(R.id.playlist_list_recycler_view)
            recyclerView.adapter = PlaylistListAdapter(playlistObjects) { fileIndex ->
                addToPlaylist(fileIndex)
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireDialog().window!!.let { window ->
            window.attributes = window.attributes.apply {
                val displayMetrics = resources.displayMetrics
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = (displayMetrics.heightPixels * 0.5).toInt()
            }
        }
    }

    private fun addToPlaylist(fileIndex: Int) {
        val otherPlaylist = readPlaylistDataFromFile(requireContext(), fileIndex)!!
        otherPlaylist.songList.add(libraryIndex!!)
        writePlaylistDataToFile(requireContext(), otherPlaylist)
        Toast.makeText(
            requireContext(),
            "Successfully Saved Song To: ${otherPlaylist.playlistName}",
            Toast.LENGTH_SHORT
        ).show()
    }

    inner class PlaylistListAdapter(
        private val playlistObjects: MutableList<PlaylistData>,
        private val clickListener: (Int) -> Unit
    ) : RecyclerView.Adapter<PlaylistListAdapter.PlaylistViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
            return PlaylistViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
            holder.bind(playlistObjects[position], clickListener)
        }

        override fun getItemCount(): Int = playlistObjects.size

        inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val playlistNameNameTextView: TextView = itemView.findViewById(R.id.playlist_name)
            private val playlistOptionsButton: ImageButton = itemView.findViewById(R.id.playlist_options_button)
            private val playlistIcon: ImageView = itemView.findViewById(R.id.playlist_icon)
            fun bind(playlistObject: PlaylistData, clickListener: (Int) -> Unit) {
                playlistNameNameTextView.text = playlistObject.playlistName
                (playlistOptionsButton.parent as? ViewGroup)?.removeView(playlistOptionsButton)
                if (playlistObject.icon != null) {
                    playlistIcon.setImageBitmap(playlistObject.icon)
                } else {
                    playlistIcon.setImageResource(R.drawable.blank_playlist)
                }
                itemView.setOnClickListener { clickListener(playlistObject.fileIndex) }
            }
        }
    }


}