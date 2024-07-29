package com.daniel.finalproject

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.daniel.finalproject.SongData.Companion.getMp3FilePath
import java.io.File
import java.io.IOException

class PlaylistViewFragment : Fragment() {
    interface OnSongUpdatedListener {
        fun onSongUpdated(newSong: SongData?, libraryIndex: Int? = null)
    }

    interface OnPlaylistUpdatedListener {
        fun onPlaylistUpdated(newPlaylist: PlaylistData?, fileIndex: Int? = null)
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
    private lateinit var progressBar: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateProgressBarRunnable: Runnable
    private lateinit var recyclerView: RecyclerView
    private lateinit var songQueue: SongQueue
    private lateinit var songViewAdapter: SongViewAdapter
    private var currentSong: Int? = null
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
        val view = inflater.inflate(R.layout.playlist_view_fragment, container, false)
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
                        songViewAdapter.updateSelectedPosition(currentSong!!)
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
                    progressBar.progress = progress
                    handler.postDelayed(this, 20)
                }
            }
        }
        handler.post(updateProgressBarRunnable)
    }

    private fun onClickSongOptions(playlistIndex: Int) {
        val songOptionsFragment =
            SongOptionsDialogFragment.newInstance(songQueue.libraryIndexOf(playlistIndex))
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
        if (songQueue.size() == 0) {
            return
        }
        if (playlistIndex == null) {
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.reset()
            }
            if (::updateProgressBarRunnable.isInitialized) {
                handler.removeCallbacks(updateProgressBarRunnable)
            }
            progressBar.progress = 0
            playPauseButton.setImageResource(R.drawable.play_button)
            currentSong = null
            return
        }
        if (playlistIndex == -1) {
            currentSong = -1
            songQueue.queueIndex = -1
            songViewAdapter.updateSelectedPosition(-1)
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.reset()
            }
            if (::updateProgressBarRunnable.isInitialized) {
                handler.removeCallbacks(updateProgressBarRunnable)
            }
            return
        }
        currentSong = playlistIndex
        songQueue.setQueueCursor(playlistIndex)
        songViewAdapter.updateSelectedPosition(playlistIndex)
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
            updateProgressBar()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun initSongView(view: View) {
        recyclerView = view.findViewById(R.id.songView)
        songViewAdapter = SongViewAdapter(
            songQueue.getSongObjects(),
            clickListener = { playlistIndex ->
                onClickPlaySong(playlistIndex)
            },
            optionsClickListener = { playlistIndex ->
                onClickSongOptions(playlistIndex)
            },
            parentFragmentManager
        )
        recyclerView.adapter = songViewAdapter
    }

    private fun resetPlaylist() {
        progressBar.progress = 0
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
        progressBar = view.findViewById(R.id.progressBar)
        playPauseButton = view.findViewById(R.id.master_play_pause_button)
        playPauseButton.setOnClickListener {
            playCurrent()
        }
        val prevSongButton: ImageButton = view.findViewById(R.id.previous_song_button)
        val nextSongButton: ImageButton = view.findViewById(R.id.next_song_button)
        prevSongButton.setOnClickListener {
            prevSong()
        }
        nextSongButton.setOnClickListener {
            nextSong()
        }
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
}