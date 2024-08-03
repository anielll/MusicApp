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
import java.io.File
import java.io.IOException
import java.util.Locale

class PlaylistViewFragment : Fragment() {
    interface OnSongUpdatedListener {
        fun onSongUpdate(newSong: SongData?, libraryIndex: Int? = null)
    }

    interface OnPlaylistUpdatedListener {
        fun onPlaylistUpdate(newPlaylist: PlaylistData?, fileIndex: Int? = null)
    }

    companion object {
        fun deleteFolder(folder: File): Boolean {
            if (folder.isDirectory) {
                val children = folder.listFiles()
                if (children != null) {
                    for (child in children) {
                        deleteFolder(child)
                    }
                }
            }
            return folder.delete()
        }
    }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var updateProgressBarRunnable: Runnable
    private lateinit var recyclerView: RecyclerView
    private lateinit var songQueue: SongQueue
    private lateinit var playlistViewAdapter: PlaylistViewAdapter
    private lateinit var currentSongTime: TextView
    private lateinit var songDuration: TextView
    private var currentSong: Int? = null
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playlistFileIndex = arguments?.getInt("selected_playlist")!!
        val playlistData =
            PlaylistData.readPlaylistDataFromFile(requireContext(), playlistFileIndex)!!
        this.songQueue = SongQueue(requireActivity(), playlistData)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.playlist_view, container, false)
        initSongView(view)
        initTopAndBottomBar(view)
        setCurrentSong(-1)
        return view
    }

    //
    override fun onDestroy() {
        super.onDestroy()
        if (::updateProgressBarRunnable.isInitialized) {
            handler.removeCallbacks(updateProgressBarRunnable)
        }
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }


    fun updateSong(newSong: SongData?, libraryIndex: Int?) {
        if (newSong == null) { // deletion
            val playlistIndex: Int =
                songQueue.playlistIndexOf(libraryIndex!!) // get before deleting
            if (playlistIndex == currentSong) { // update media player
                if (songQueue.size() == 0) {
                    setCurrentSong(null)
                } else {
                    val nextSong = songQueue.next()
                    if (nextSong == null) {
                        resetPlaylist()
                    } else {
                        if (mediaPlayer.isPlaying) {
                            setCurrentSong(nextSong)
                            mediaPlayer.start()
                            playPauseButton.setImageResource(R.drawable.pause_button)
                        } else {
                            setCurrentSong(nextSong)
                        }
                        currentSong =
                            if (nextSong > playlistIndex) { // override onClickPlaySong setting currentSong
                                nextSong - 1 // if next song had higher playlistIndex, it will be shifted down one playlist index
                            } else {
                                nextSong
                            }
                        songQueue.delete(libraryIndex)
                        recyclerView.adapter?.notifyItemRemoved(playlistIndex)
                        playlistViewAdapter.updateSelectedPosition(currentSong!!)
                        songQueue.setQueueCursor(currentSong!!)
                        return
                    }
                }
            }
            songQueue.delete(libraryIndex)
            recyclerView.adapter?.notifyItemRemoved(playlistIndex)
            return
        }
        if (libraryIndex == null) { // replacement
            songQueue.update(newSong)
            recyclerView.adapter?.notifyItemChanged(songQueue.playlistIndexOf(newSong.songIndex))
        } else { // add
            songQueue.add(libraryIndex, newSong)
            recyclerView.adapter?.notifyItemInserted(songQueue.size() - 1)
            if (songQueue.size() == 1) { // if first time, initialize
                setCurrentSong(-1)
            }
        }
    }

    private fun updateProgressBar() {
        updateProgressBarRunnable = object : Runnable {
            override fun run() {
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    val progress =
                        ((mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration) * 4096).toInt()
                    seekBar.progress = progress
                    val currentTime = (mediaPlayer.currentPosition.toFloat()).toInt()
                    currentSongTime.text = formatTime(currentTime)
                    handler.postDelayed(this, mediaPlayer.duration.toLong()/4096)
                }
            }
        }
        handler.post(updateProgressBarRunnable)
    }

    private fun onClickSongOptions(playlistIndex: Int) {
        val songOptionsFragment =
            SongOptionsFragment.newInstance(songQueue.libraryIndexOf(playlistIndex), songQueue.playlistNumber())
        songOptionsFragment.show(parentFragmentManager, "SongOptions")
    }

    private fun onClickPlaySong(playlistIndex: Int) {
        if (playlistIndex == currentSong) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.play_button)
                handler.removeCallbacks(updateProgressBarRunnable)
            } else {
                mediaPlayer.start()
                playPauseButton.setImageResource(R.drawable.pause_button)
                updateProgressBar()
            }
            return
        }
        setCurrentSong(playlistIndex)
        mediaPlayer.start()
        playPauseButton.setImageResource(R.drawable.pause_button)
    }

    private fun setCurrentSong(playlistIndex: Int?) {
        if (songQueue.size() == 0) { // if initializing empty playlist... don't
            return
        }
        if (playlistIndex == null) { // uninitializes playlist
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.reset()
            }
            if (::updateProgressBarRunnable.isInitialized) {
                handler.removeCallbacks(updateProgressBarRunnable)
            }
            seekBar.progress = 0
            playPauseButton.setImageResource(R.drawable.play_button)
            currentSong = null
            currentSongTime.text = getString(R.string.zeroed_time)
            songDuration.text = getString(R.string.zeroed_time)
            seekBar.isEnabled=false
            return
        }
        if (playlistIndex == -1) {
            currentSong = -1
            songQueue.queueIndex = -1
            playlistViewAdapter.updateSelectedPosition(-1)
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.reset()
            }
            if (::updateProgressBarRunnable.isInitialized) {
                handler.removeCallbacks(updateProgressBarRunnable)
            }
            currentSongTime.text = getString(R.string.zeroed_time)
            songDuration.text = getString(R.string.zeroed_time)
            seekBar.isEnabled=false
            return
        }
        currentSong = playlistIndex
        songQueue.setQueueCursor(playlistIndex)
        playlistViewAdapter.updateSelectedPosition(playlistIndex)
        seekBar.isEnabled=true
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
        }
        try {
            val path = getMp3FilePath(requireContext(), songQueue.libraryIndexOf(playlistIndex))
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
            mediaPlayer.setOnCompletionListener {
                nextSong()
            }
            if (::updateProgressBarRunnable.isInitialized) {
                handler.removeCallbacks(updateProgressBarRunnable)
            }
            currentSongTime.text = getString(R.string.zeroed_time)
            songDuration.text = formatTime(mediaPlayer.duration)
            updateProgressBar()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun initSongView(view: View) {
        recyclerView = view.findViewById(R.id.songView)
        playlistViewAdapter = PlaylistViewAdapter(
            songQueue.getSongObjects(),
            clickListener = { playlistIndex ->
                onClickPlaySong(playlistIndex)
            },
            optionsClickListener = { playlistIndex ->
                onClickSongOptions(playlistIndex)
            },
            parentFragmentManager
        )
        recyclerView.adapter = playlistViewAdapter
    }

    private fun resetPlaylist() {
        seekBar.progress = 0
        setCurrentSong(-1)
        playPauseButton.setImageResource(R.drawable.play_button)
        currentSong = null
    }

    private fun initTopAndBottomBar(view: View) {
        // Top Bar
        val playlistName: TextView = view.findViewById(R.id.playlistName)
        val loopButton: ImageButton = view.findViewById(R.id.loop_playlist_button)
        val shuffleButton: ImageButton = view.findViewById(R.id.shuffle_playlist_button)
        val playlistIcon: ImageView = view.findViewById(R.id.playlist_icon)
        playlistName.text = songQueue.playlistName()
        if(songQueue.icon()!=null){
            playlistIcon.setImageBitmap(songQueue.icon())
        }
        if(songQueue.playlistNumber()==-1){
            playlistIcon.setImageResource(R.drawable.library_icon)
        }
        loopButton.setOnClickListener {
            songQueue.toggleLoop()
            if (songQueue.looped) {
                loopButton.setImageResource(R.drawable.loop_on)
            } else {
                loopButton.setImageResource(R.drawable.loop_off)
            }
        }
        shuffleButton.setOnClickListener {
            songQueue.toggleShuffle()
            if (songQueue.shuffled) {
                shuffleButton.setImageResource(R.drawable.shuffle_on)
            } else {
                shuffleButton.setImageResource(R.drawable.shuffle_off)
            }
        }
        // Bottom Bar
        seekBar = view.findViewById(R.id.progressBar)
        playPauseButton = view.findViewById(R.id.master_play_pause_button)
        playPauseButton.setOnClickListener {
            playCurrent()
        }
        val prevSongButton: ImageButton = view.findViewById(R.id.previous_song_button)
        val nextSongButton: ImageButton = view.findViewById(R.id.next_song_button)
        currentSongTime = view.findViewById(R.id.duration_start_text)
        songDuration = view.findViewById(R.id.duration_end_text)
        prevSongButton.setOnClickListener {
            prevSong()
        }
        nextSongButton.setOnClickListener {
            nextSong()
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var wasPlaying: Boolean = false
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && ::mediaPlayer.isInitialized) {
                    val seekPosition = (progress / 4096.0 * mediaPlayer.duration).toInt()
                    mediaPlayer.seekTo(seekPosition)
                    currentSongTime.text = formatTime(seekPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if(!::mediaPlayer.isInitialized ){
                    return
                }
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    wasPlaying = true
                }else{
                    wasPlaying= false
                }
                handler.removeCallbacks(updateProgressBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (::mediaPlayer.isInitialized && wasPlaying) {
                    mediaPlayer.start()
                }
                updateProgressBar()
            }
        })


    }

    private fun nextSong() {
        if (songQueue.size() != 0) {
            val nextSong = songQueue.next()
            if (nextSong == null) {
                resetPlaylist()
            } else {
                onClickPlaySong(nextSong)
            }
        }
    }

    private fun prevSong() {
        if (songQueue.size() != 0) {
            val prevSong = songQueue.prev()
            if (prevSong == null) {
                resetPlaylist()
            } else {
                onClickPlaySong(prevSong)
            }
        }
    }

    private fun playCurrent() {
        if (currentSong==-1) {
            nextSong()
            return
        }
        if (songQueue.size() != 0) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.play_button)
                handler.removeCallbacks(updateProgressBarRunnable)
            } else {
                mediaPlayer.start()
                playPauseButton.setImageResource(R.drawable.pause_button)
                updateProgressBar()
            }
        }
    }
    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = (milliseconds / 1000)
        val minutes =  totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(),"%01d:%02d", minutes, seconds)
    }
}