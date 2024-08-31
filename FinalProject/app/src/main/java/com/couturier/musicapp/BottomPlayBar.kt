package com.couturier.musicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.couturier.musicapp.databinding.BottomBarBinding
class BottomPlayBar : Fragment() {
    private var _binding: BottomBarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistViewModel by activityViewModels()
    private val songQueue get() = viewModel.songQueue
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomBarBinding.inflate(inflater, container, false).apply {
            // Bottom Bar
            loopButton.setOnClickListener { onLoop() }
            shuffleButton.setOnClickListener { onShuffle() }
            masterPlayPauseButton.setOnClickListener {
                playPause()
            }
            viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
                if (isPlaying) {
                    masterPlayPauseButton.setImageResource(R.drawable.pause_button)
                } else {
                    masterPlayPauseButton.setImageResource(R.drawable.play_button)
                }
            }
            previousSongButton.setOnClickListener {
                prevSong()
            }
            nextSongButton.setOnClickListener {
                nextSong()
            }
            viewModel.songTime.observe(viewLifecycleOwner){ currentTime ->
                durationStartText.text = currentTime
            }
            viewModel.songDuration.observe(viewLifecycleOwner){ currentTime ->
                durationEndText.text = currentTime
            }
            viewModel.songProgress.observe(viewLifecycleOwner){ currentProgress ->
                progressBar.progress = currentProgress
            }
            viewModel.seekBarEnabled.observe(viewLifecycleOwner){ enabled ->
                progressBar.isEnabled = enabled
                progressBar.progress = 0
            }
            viewModel.setSeekbarListeners(progressBar)
        }
        return binding.root
    }

    private fun onShuffle() {
        songQueue.toggleShuffle()
        if (songQueue.shuffled) {
            binding.shuffleButton.setImageResource(R.drawable.shuffle_on)
        } else {
            binding.shuffleButton.setImageResource(R.drawable.shuffle_off)
        }
    }

    private fun onLoop() {
        songQueue.toggleLoop()
        if (songQueue.looped) {
            binding.loopButton.setImageResource(R.drawable.loop_on)
        } else {
            binding.loopButton.setImageResource(R.drawable.loop_off)
        }
    }

    private fun playPause() {
        viewModel.playCurrent()
    }

    private fun nextSong() {
        viewModel.playNext()
    }

    private fun prevSong() {
        viewModel.playPrev()
    }
}