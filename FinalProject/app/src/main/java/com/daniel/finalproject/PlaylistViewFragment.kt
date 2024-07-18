package com.daniel.finalproject

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.daniel.finalproject.SongData.Companion.getMp3FilePath
import java.io.File
import java.io.IOException
class PlaylistViewFragment : Fragment()
{
    interface OnSongUpdatedListener {
        fun onSongUpdated(newSong:SongData?, libraryIndex: Int?= null)
    }
    interface OnPlaylistUpdatedListener {
        fun onPlaylistUpdated(newPlaylist:PlaylistData?, fileIndex: Int?= null)
    }
    companion object{
        fun deleteFolder(folder: File):Boolean{
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
    private lateinit var  progressBar: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateProgressBarRunnable: Runnable
    private lateinit var recyclerView: RecyclerView
    private lateinit var songQueue: SongQueue
    private var lastSong:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playlistFileIndex = arguments?.getInt("selected_playlist")!!
        val playlistData = PlaylistData.readPlaylistDataFromFile(requireContext(),playlistFileIndex)!!
        this.songQueue = SongQueue(requireActivity(),playlistData)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.playlist_view_fragment, container, false)
        setCurrentSong(0)
        initSongView(view)
        initTopAndBottomBar(view)
        return view
    }
//
    override fun onDestroy() {
        super.onDestroy()
    handler.removeCallbacks(updateProgressBarRunnable)
    if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }


    fun updateSong(newSong: SongData?, libraryIndex: Int?) {
        if(newSong==null){ // deletion
            val playlistIndex: Int = songQueue.playlistIndexOf(libraryIndex!!) // get before deleting
            songQueue.delete(libraryIndex)
            recyclerView.adapter?.notifyItemRemoved(playlistIndex)
            return
        }
        if(libraryIndex==null){ // replacement
            songQueue.update(newSong)
            recyclerView.adapter?.notifyItemChanged(songQueue.playlistIndexOf(newSong.songIndex))
        }else{ // add
            songQueue.add(libraryIndex,newSong)
            recyclerView.adapter?.notifyItemInserted(songQueue.size()-1)
        }
    }
    private fun updateProgressBar() {
        updateProgressBarRunnable = object : Runnable {
            override fun run() {
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    val progress = ((mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration) * 4096).toInt()
                    progressBar.progress = progress
                    handler.postDelayed(this, 20)
                }
            }
        }
        handler.post(updateProgressBarRunnable)
    }
    private fun onClickSongOptions(playlistIndex: Int) {
        val songOptionsFragment = SongOptionsDialogFragment.newInstance(songQueue.libraryIndexOf(playlistIndex))
        songOptionsFragment.show(parentFragmentManager, "SongOptions")
    }
    private fun onClickPlaySong(playlistIndex: Int){
        if(playlistIndex==lastSong){
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
        lastSong = playlistIndex
    }
    private fun setCurrentSong(playlistIndex: Int ) {
        songQueue.currentSong = playlistIndex
        if(::mediaPlayer.isInitialized) {
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
                val nextSong = songQueue.next()
                if(nextSong!=null) {
                    onClickPlaySong(nextSong)
                }
            }
            if(::updateProgressBarRunnable.isInitialized){
                handler.removeCallbacks(updateProgressBarRunnable)
            }
            updateProgressBar()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun initSongView(view: View){
        recyclerView = view.findViewById(R.id.songView)
        val songViewAdapter = SongViewAdapter(
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
    private fun resetPlaylist(){
        progressBar.progress = 0
        setCurrentSong(0)
        playPauseButton.setImageResource(R.drawable.play_button)
        lastSong = null
    }
    private fun initTopAndBottomBar(view: View){
        // Top Bar
        val playlistName: TextView = view.findViewById(R.id.playlistName)
        val backButton: ImageButton = view.findViewById(R.id.backButton)
        val loopButton: ImageButton = view.findViewById(R.id.loop_playlist_button)
        val shuffleButton: ImageButton = view.findViewById(R.id.shuffle_playlist_button)
        playlistName.text = songQueue.playlistName()
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        loopButton.setOnClickListener{
            songQueue.toggleLoop()
            if(songQueue.looped){
                loopButton.setImageResource(R.drawable.loop_on)
            }else{
                loopButton.setImageResource(R.drawable.loop_off)
            }
        }
        shuffleButton.setOnClickListener{
            songQueue.toggleShuffle()
            if(songQueue.shuffled){
                shuffleButton.setImageResource(R.drawable.shuffle_on)
            }else{
                shuffleButton.setImageResource(R.drawable.shuffle_off)
            }
        }
        // Bottom Bar
        progressBar = view.findViewById(R.id.progressBar)
        playPauseButton = view.findViewById(R.id.master_play_pause_button)
        playPauseButton.setOnClickListener {
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
        val prevSongButton:ImageButton = view.findViewById(R.id.previous_song_button)
        val nextSongButton:ImageButton = view.findViewById(R.id.next_song_button)
        prevSongButton.setOnClickListener{
            val prevSong = songQueue.prev()
            if(prevSong==null) {
                resetPlaylist()
            }else{
                onClickPlaySong(prevSong)
            }
        }
        nextSongButton.setOnClickListener{
            val nextSong = songQueue.next()
            if(nextSong==null) {
                resetPlaylist()
            }else{
                onClickPlaySong(nextSong)
            }
        }

    }
}