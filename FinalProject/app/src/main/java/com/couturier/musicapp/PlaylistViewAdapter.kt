package com.couturier.musicapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

class PlaylistViewAdapter(
    private val songObjects: MutableList<SongData>,
    private val clickListener: (Int) -> Unit,
    private val optionsClickListener: (Int) -> Unit,
    private val fragmentManager : FragmentManager)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION
    companion object {
        private const val VIEW_TYPE_SONG = 1
        private const val VIEW_TYPE_ADD_SONG = 2
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SONG -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
                SongViewHolder(view)
            }
            VIEW_TYPE_ADD_SONG -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.add_song_item, parent, false)
                AddSongViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, recyclerPosition: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_SONG -> {
                val songViewHolder = holder as SongViewHolder
                songViewHolder.bind(songObjects[recyclerPosition], clickListener)
                songViewHolder.itemView.isSelected = (selectedPosition == recyclerPosition)
            }
            VIEW_TYPE_ADD_SONG -> {
                val addSongViewHolder = holder as AddSongViewHolder
                addSongViewHolder.bind()
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

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songNameTextView: TextView = itemView.findViewById(R.id.song_name)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name)
        private val songIcon: ImageView = itemView.findViewById(R.id.songIcon)
        private val optionsButton: ImageButton = itemView.findViewById(R.id.song_options_button)
        fun bind(songObject: SongData, clickListener: (Int) -> Unit) {
            songNameTextView.text = songObject.title
            artistNameTextView.text = songObject.artist
            songObject.icon.let{
                if(it!=null) songIcon.setImageBitmap(it)
                else songIcon.setImageResource(R.drawable.blank_song)
            }
            itemView.setOnClickListener {
                clickListener( bindingAdapterPosition)
                updateSelectedPosition(bindingAdapterPosition)
            }
            optionsButton.setOnClickListener{optionsClickListener(bindingAdapterPosition)}
        }
    }
    inner class AddSongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addSongButton: ImageButton = itemView.findViewById(R.id.addButton)
        fun bind() {
            addSongButton.setOnClickListener{
                val addSongFragment = AddSongFragment()
                addSongFragment.show(fragmentManager, "AddSongFragment")
            }
        }
    }
    fun updateSelectedPosition(newPosition: Int) {
        val previousPosition = selectedPosition
        selectedPosition = newPosition
        notifyItemChanged(previousPosition)
        notifyItemChanged(newPosition)
    }


}