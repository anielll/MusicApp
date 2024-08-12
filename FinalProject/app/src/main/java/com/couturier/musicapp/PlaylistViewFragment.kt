package com.couturier.musicapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.couturier.musicapp.SongData.Companion.getMp3FilePath
import com.couturier.musicapp.databinding.PlaylistViewBinding
import java.io.File
import java.io.IOException

class PlaylistViewFragment : Fragment() {

    private lateinit var songQueue: SongQueue
    private var _binding: PlaylistViewBinding? = null
    private val binding get() = _binding!!

    //    private lateinit var mediaPlayer: MediaPlayer
//    private lateinit var playPauseButton: ImageButton
//    private lateinit var seekBar: SeekBar
//    private lateinit var updateProgressBarRunnable: Runnable
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var playlistViewAdapter: PlaylistViewAdapter
//    private lateinit var currentSongTime: TextView
//    private lateinit var songDuration: TextView
//    private var currentSong: Int? = null
//    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playlistData = requireArguments().getInt("selected_playlist").let { selectedPlaylist ->
            if (selectedPlaylist != -1)
                PlaylistData.readPlaylistDataFromFile(requireContext(), selectedPlaylist)!!
            else {
                MasterList.library
            }
        }
        this.songQueue = SongQueue(requireActivity(), playlistData)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlaylistViewBinding.inflate(inflater, container, false).apply{
            //Create Main RecyclerView
            songView.adapter = PlaylistViewAdapter(
            songQueue.getSongObjects(),
                ::onClickPlaySong,
                ::onClickSongOptions,
            parentFragmentManager
        )
            // Create Top Bar
        playlistName.text = songQueue.playlistName()
        songQueue.icon()?.let{playlistIcon.setImageBitmap(it)}
        if(songQueue.playlistNumber()==-1){
            playlistIcon.setImageResource(R.drawable.library_icon)
        }

        }

//        initTopAndBottomBar(view)
//        setCurrentSong(-1)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
//    private fun onShuffle(){
//        songQueue.toggleShuffle()
//        if (songQueue.shuffled) {
//            binding.shufflePlaylistButton.setImageResource(R.drawable.shuffle_on)
//        } else {
//            binding.shufflePlaylistButton.setImageResource(R.drawable.shuffle_off)
//        }
//    }
//    private fun onLoop(){
//        songQueue.toggleLoop()
//        if (songQueue.looped) {
//            binding.loopPlaylistButton.setImageResource(R.drawable.loop_on)
//        } else {
//            binding.loopPlaylistButton.setImageResource(R.drawable.loop_off)
//        }
//    }
    //
//    override fun onDestroy() {
//        super.onDestroy()
//        if (::updateProgressBarRunnable.isInitialized) {
//            handler.removeCallbacks(updateProgressBarRunnable)
//        }
//        if (::mediaPlayer.isInitialized) {
//            mediaPlayer.release()
//        }
//    }
//
//
    fun updateSong(newSong: SongData?, libraryIndex: Int?) {
        return
//        if (newSong == null) { // deletion
//            val playlistIndex: Int =
//                songQueue.playlistIndexOf(libraryIndex!!) // get before deleting
//            if (playlistIndex == currentSong) { // update media player
//                if (songQueue.size() == 0) {
//                    setCurrentSong(null)
//                } else {
//                    val nextSong = songQueue.next()
//                    if (nextSong == null) {
//                        resetPlaylist()
//                    } else {
//                        if (mediaPlayer.isPlaying) {
//                            setCurrentSong(nextSong)
//                            mediaPlayer.start()
//                            playPauseButton.setImageResource(R.drawable.pause_button)
//                        } else {
//                            setCurrentSong(nextSong)
//                        }
//                        currentSong =
//                            if (nextSong > playlistIndex) { // override onClickPlaySong setting currentSong
//                                nextSong - 1 // if next song had higher playlistIndex, it will be shifted down one playlist index
//                            } else {
//                                nextSong
//                            }
//                        songQueue.delete(libraryIndex)
//                        recyclerView.adapter?.notifyItemRemoved(playlistIndex)
//                        playlistViewAdapter.updateSelectedPosition(currentSong!!)
//                        songQueue.setQueueCursor(currentSong!!)
//                        return
//                    }
//                }
//            }
//            songQueue.delete(libraryIndex)
//            recyclerView.adapter?.notifyItemRemoved(playlistIndex)
//            return
//        }
//        if (libraryIndex == null) { // replacement
//            songQueue.update(newSong)
//            recyclerView.adapter?.notifyItemChanged(songQueue.playlistIndexOf(newSong.songIndex))
//        } else { // add
//            songQueue.add(libraryIndex, newSong)
//            recyclerView.adapter?.notifyItemInserted(songQueue.size() - 1)
//            if (songQueue.size() == 1) { // if first time, initialize
//                setCurrentSong(-1)
//            }
//        }
    }
//
//    private fun updateProgressBar() {
//        updateProgressBarRunnable = object : Runnable {
//            override fun run() {
//                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
//                    val progress =
//                        ((mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration) * 4096).toInt()
//                    seekBar.progress = progress
//                    val currentTime = (mediaPlayer.currentPosition.toFloat()).toInt()
//                    currentSongTime.text = formatTime(currentTime)
//                    handler.postDelayed(this, mediaPlayer.duration.toLong()/4096)
//                }
//            }
//        }
//        handler.post(updateProgressBarRunnable)
//    }
//
    private fun onClickSongOptions(playlistIndex: Int) {
        SongOptionsFragment.newInstance(songQueue.libraryIndexOf(playlistIndex), songQueue.playlistNumber())
            .show(parentFragmentManager, "SongOptions")
    }

    private fun onClickPlaySong(playlistIndex: Int) {
//        if (playlistIndex == currentSong) {
//            if (mediaPlayer.isPlaying) {
//                mediaPlayer.pause()
//                playPauseButton.setImageResource(R.drawable.play_button)
//                handler.removeCallbacks(updateProgressBarRunnable)
//            } else {
//                mediaPlayer.start()
//                playPauseButton.setImageResource(R.drawable.pause_button)
//                updateProgressBar()
//            }
//            return
//        }
//        setCurrentSong(playlistIndex)
//        mediaPlayer.start()
//        playPauseButton.setImageResource(R.drawable.pause_button)
    }
//
//    private fun setCurrentSong(playlistIndex: Int?) {
//        if (songQueue.size() == 0) { // if initializing empty playlist... don't
//            return
//        }
//        if (playlistIndex == null) { // uninitializes playlist
//            if (::mediaPlayer.isInitialized) {
//                if (mediaPlayer.isPlaying) {
//                    mediaPlayer.stop()
//                }
//                mediaPlayer.reset()
//            }
//            if (::updateProgressBarRunnable.isInitialized) {
//                handler.removeCallbacks(updateProgressBarRunnable)
//            }
//            seekBar.progress = 0
//            playPauseButton.setImageResource(R.drawable.play_button)
//            currentSong = null
//            currentSongTime.text = getString(R.string.zeroed_time)
//            songDuration.text = getString(R.string.zeroed_time)
//            seekBar.isEnabled=false
//            return
//        }
//        if (playlistIndex == -1) {
//            currentSong = -1
//            songQueue.queueIndex = -1
//            playlistViewAdapter.updateSelectedPosition(-1)
//            if (::mediaPlayer.isInitialized) {
//                if (mediaPlayer.isPlaying) {
//                    mediaPlayer.stop()
//                }
//                mediaPlayer.reset()
//            }
//            if (::updateProgressBarRunnable.isInitialized) {
//                handler.removeCallbacks(updateProgressBarRunnable)
//            }
//            currentSongTime.text = getString(R.string.zeroed_time)
//            songDuration.text = getString(R.string.zeroed_time)
//            seekBar.isEnabled=false
//            return
//        }
//        currentSong = playlistIndex
//        songQueue.setQueueCursor(playlistIndex)
//        playlistViewAdapter.updateSelectedPosition(playlistIndex)
//        seekBar.isEnabled=true
//        if (::mediaPlayer.isInitialized) {
//            if (mediaPlayer.isPlaying) {
//                mediaPlayer.stop()
//            }
//            mediaPlayer.reset()
//        }
//        try {
//            val path = getMp3FilePath(requireContext(), songQueue.libraryIndexOf(playlistIndex))
//            mediaPlayer = MediaPlayer()
//            mediaPlayer.setDataSource(path)
//            mediaPlayer.prepare()
//            mediaPlayer.setOnCompletionListener {
//                nextSong()
//            }
//            if (::updateProgressBarRunnable.isInitialized) {
//                handler.removeCallbacks(updateProgressBarRunnable)
//            }
//            currentSongTime.text = getString(R.string.zeroed_time)
//            songDuration.text = formatTime(mediaPlayer.duration)
//            updateProgressBar()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//

//
//    private fun resetPlaylist() {
//        seekBar.progress = 0
//        setCurrentSong(-1)
//        playPauseButton.setImageResource(R.drawable.play_button)
//        currentSong = null
//    }
//
//    private fun initTopAndBottomBar(view: View) {

//        // Bottom Bar
//    loopPlaylistButton.setOnClickListener {
//        onLoop()
//    }
//    shufflePlaylistButton.setOnClickListener {
//        onShuffle()
//    }
//        seekBar = view.findViewById(R.id.progressBar)
//        playPauseButton = view.findViewById(R.id.master_play_pause_button)
//        playPauseButton.setOnClickListener {
//            playCurrent()
//        }
//        val prevSongButton: ImageButton = view.findViewById(R.id.previous_song_button)
//        val nextSongButton: ImageButton = view.findViewById(R.id.next_song_button)
//        currentSongTime = view.findViewById(R.id.duration_start_text)
//        songDuration = view.findViewById(R.id.duration_end_text)
//        prevSongButton.setOnClickListener {
//            prevSong()
//        }
//        nextSongButton.setOnClickListener {
//            nextSong()
//        }
//        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            var wasPlaying: Boolean = false
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (fromUser && ::mediaPlayer.isInitialized) {
//                    val seekPosition = (progress / 4096.0 * mediaPlayer.duration).toInt()
//                    mediaPlayer.seekTo(seekPosition)
//                    currentSongTime.text = formatTime(seekPosition)
//                }
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                if(!::mediaPlayer.isInitialized ){
//                    return
//                }
//                if (mediaPlayer.isPlaying) {
//                    mediaPlayer.pause()
//                    wasPlaying = true
//                }else{
//                    wasPlaying= false
//                }
//                handler.removeCallbacks(updateProgressBarRunnable)
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                if (::mediaPlayer.isInitialized && wasPlaying) {
//                    mediaPlayer.start()
//                }
//                updateProgressBar()
//            }
//        })
//
//
//    }
//
//    private fun nextSong() {
//        if (songQueue.size() != 0) {
//            val nextSong = songQueue.next()
//            if (nextSong == null) {
//                resetPlaylist()
//            } else {
//                onClickPlaySong(nextSong)
//            }
//        }
//    }
//
//    private fun prevSong() {
//        if (songQueue.size() != 0) {
//            val prevSong = songQueue.prev()
//            if (prevSong == null) {
//                resetPlaylist()
//            } else {
//                onClickPlaySong(prevSong)
//            }
//        }
//    }
//
//    private fun playCurrent() {
//        if (currentSong==-1) {
//            nextSong()
//            return
//        }
//        if (songQueue.size() != 0) {
//            if (mediaPlayer.isPlaying) {
//                mediaPlayer.pause()
//                playPauseButton.setImageResource(R.drawable.play_button)
//                handler.removeCallbacks(updateProgressBarRunnable)
//            } else {
//                mediaPlayer.start()
//                playPauseButton.setImageResource(R.drawable.pause_button)
//                updateProgressBar()
//            }
//        }
//    }
//    private fun formatTime(milliseconds: Int): String {
//        val totalSeconds = (milliseconds / 1000)
//        val minutes =  totalSeconds / 60
//        val seconds = totalSeconds % 60
//        return String.format(Locale.getDefault(),"%01d:%02d", minutes, seconds)
//    }
}