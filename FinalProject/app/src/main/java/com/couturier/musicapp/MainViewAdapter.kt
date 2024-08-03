package com.couturier.musicapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

class MainViewAdapter (
    private val playlistObjects: MutableList<PlaylistData>,
    private val clickListener: (Int) -> Unit,
    private val optionsClickListener: (Int) -> Unit,
    private val parentFragmentManager: FragmentManager
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    companion object {
        private const val VIEW_TYPE_ADD_PLAYLIST = 3
        private const val VIEW_TYPE_LIBRARY = 1
        private const val VIEW_TYPE_PLAYLIST = 2
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PLAYLIST ->{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
                PlaylistViewHolder(view)
            }
            VIEW_TYPE_LIBRARY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
                LibraryViewHolder(view)
            }
            VIEW_TYPE_ADD_PLAYLIST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.add_playlist_item, parent, false)
                AddPlaylistViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_LIBRARY -> {
                val libraryViewHolder = holder as LibraryViewHolder
                libraryViewHolder.bind(clickListener)
            }
            VIEW_TYPE_PLAYLIST -> {
                val playlistViewHolder = holder as PlaylistViewHolder
                playlistViewHolder.bind(playlistObjects[position-1],clickListener)
            }
            VIEW_TYPE_ADD_PLAYLIST -> {
                val addPlaylistViewHolder = holder as AddPlaylistViewHolder
                addPlaylistViewHolder.bind()
            }
        }
    }

    override fun getItemCount(): Int {
        return playlistObjects.size + 2
    }
    override fun getItemViewType(position: Int): Int {
        if(position==0){
            return VIEW_TYPE_LIBRARY
        }
        if(position-1 < playlistObjects.size){
            return VIEW_TYPE_PLAYLIST
        }
        return VIEW_TYPE_ADD_PLAYLIST
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistNameNameTextView: TextView = itemView.findViewById(R.id.playlist_name)
        private val playlistOptionsButton: ImageButton = itemView.findViewById(R.id.playlist_options_button)
        private val playlistIcon: ImageView = itemView.findViewById(R.id.playlist_icon)
        fun bind(playlistObject: PlaylistData,clickListener: (Int) -> Unit) {
            playlistNameNameTextView.text = playlistObject.playlistName
            itemView.setOnClickListener { clickListener(bindingAdapterPosition)}
            playlistOptionsButton.setOnClickListener {optionsClickListener(bindingAdapterPosition)}
            playlistObject.icon.let{
                if(it!=null) playlistIcon.setImageBitmap(playlistObject.icon)
                else playlistIcon.setImageResource(R.drawable.blank_playlist)
            }
        }
    }
    inner class AddPlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addPlaylistButton: ImageButton = itemView.findViewById(R.id.addButton)
        fun bind() {
            addPlaylistButton.setOnClickListener{
                val addPlaylistFragment = AddPlaylistFragment()
                addPlaylistFragment.show(parentFragmentManager, "AddPlaylistFragment")
            }
        }
    }
    inner class LibraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(clickListener: (Int) -> Unit) {
            itemView.setOnClickListener { clickListener(bindingAdapterPosition)}
            itemView.findViewById<ImageView>(R.id.playlist_icon).setImageResource(R.drawable.library_icon)
            itemView.findViewById<ImageButton>(R.id.playlist_options_button).let{
                (it.parent as? ViewGroup?)?.removeView(it)
            }
            itemView.findViewById<TextView>(R.id.playlist_name).setText(R.string.my_library)
        }
    }

}