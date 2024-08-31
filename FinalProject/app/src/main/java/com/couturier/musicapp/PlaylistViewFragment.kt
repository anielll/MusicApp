package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.couturier.musicapp.databinding.PlaylistViewBinding

class PlaylistViewFragment : Fragment() {
    private var _binding: PlaylistViewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistViewModel by activityViewModels()
    private val songQueue get() = viewModel.songQueue
    private lateinit var adapter: PlaylistViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlaylistViewBinding.inflate(inflater, container, false).apply {
            //Create Main RecyclerView
            adapter = PlaylistViewAdapter(
                songQueue.getSongObjects(),
                ::onClickPlaySong,
                ::onClickSongOptions,
                parentFragmentManager
            )
            songView.adapter = adapter
            // Create Top Bar
            playlistName.text = songQueue.playlistName()
            songQueue.icon()?.let { playlistIcon.setImageBitmap(it) }
            if (songQueue.playlistNumber() == -1) {
                playlistIcon.setImageResource(R.drawable.library_icon)
            }
            viewModel.currentSong.observe(viewLifecycleOwner) { newValue ->
                if (newValue == null) return@observe
                adapter.updateSelectedPosition(newValue)
            }
            viewModel.songUpdate.observe(viewLifecycleOwner){ recyclerPosition ->
                if(recyclerPosition == null) {
                    println("test")
                    return@observe
                }
                when(viewModel.updateType){
                    "ADD" -> adapter.notifyItemInserted(recyclerPosition)
                    "DELETE" -> adapter.notifyItemRemoved(recyclerPosition)
                    "REPLACE" -> adapter.notifyItemChanged(recyclerPosition)
                    else -> throw IllegalArgumentException("Invalid Update Type")
                }

            }

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickSongOptions(playlistIndex: Int) {
        SongOptionsFragment.newInstance(
            songQueue.libraryIndexOf(playlistIndex),
            songQueue.playlistNumber()
        )
            .show(parentFragmentManager, "SongOptions")
    }

    private fun onClickPlaySong(playlistIndex: Int) {
        viewModel.setCurrentSong(playlistIndex)
        viewModel.playCurrent()
    }

}