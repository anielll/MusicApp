package com.daniel.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaylistViewAdapter (
    private val playlistObjects: MutableList<PlaylistData>,
    private val clickListener: (Int) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private val VIEW_TYPE_LIBRARY = 1
    private val VIEW_TYPE_PLAYLIST = 2
    private val VIEW_TYPE_ADD_PLAYLIST = 3
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PLAYLIST ->{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_item, parent, false)
                PlaylistViewHolder(view)
            }
            VIEW_TYPE_LIBRARY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.library_item, parent, false)
                LibraryViewHolder(view)
            }
            VIEW_TYPE_ADD_PLAYLIST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.add_item, parent, false)
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
                val addplaylistViewHolder = holder as AddPlaylistViewHolder
                addplaylistViewHolder.bind()
                addplaylistViewHolder.itemView.findViewById<ImageButton>(R.id.addButton).setOnClickListener {
                    addPlaylist()
                }
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

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playlistNameNameTextView: TextView = itemView.findViewById(R.id.playListName)
        fun bind(playlistObject: PlaylistData,clickListener: (Int) -> Unit) {
            playlistNameNameTextView.text = playlistObject.playlistName
            itemView.setOnClickListener { clickListener(bindingAdapterPosition)}
        }
    }
    class AddPlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
        }
    }
    class LibraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(clickListener: (Int) -> Unit) {
            itemView.setOnClickListener { clickListener(bindingAdapterPosition)}
        }
    }
    private fun addPlaylist() {
    }
}