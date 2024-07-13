package com.daniel.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class SongViewAdapter(
    private val currentPlaylist: PlaylistData,
    private val songObjects: MutableList<SongData>,
    private val clickListener: (Int) -> Unit,
    private val longClickListener: (Int) -> Boolean)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_SONG = 1
    private val VIEW_TYPE_ADD_SONG = 2


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SONG -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
                SongViewHolder(view)
            }
            VIEW_TYPE_ADD_SONG -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.add_item, parent, false)
                AddSongViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, recyclerPosition: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_SONG -> {
                val songViewHolder = holder as SongViewHolder
                songViewHolder.bind(songObjects[recyclerPosition], clickListener, longClickListener)
            }
            VIEW_TYPE_ADD_SONG -> {
                val addSongViewHolder = holder as AddSongViewHolder
                addSongViewHolder.bind()
                addSongViewHolder.itemView.findViewById<ImageButton>(R.id.addButton).setOnClickListener {
                    addSong()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return songObjects.size + 1
    }
    override fun getItemViewType(recyclerPosition: Int): Int {
        return if (recyclerPosition < songObjects.size) {
            VIEW_TYPE_SONG
        } else {
            VIEW_TYPE_ADD_SONG
        }
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songNameTextView: TextView = itemView.findViewById(R.id.songName)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.artistName)
        fun bind(songObject: SongData, clickListener: (Int) -> Unit, longClickListener: (Int) -> Boolean) {
            songNameTextView.text = songObject.title
            artistNameTextView.text = songObject.artist
            itemView.setOnClickListener { clickListener( bindingAdapterPosition)}
            itemView.setOnLongClickListener { longClickListener(bindingAdapterPosition) }
        }
    }
    class AddSongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
        }
    }
    private fun addSong() {
    }

}