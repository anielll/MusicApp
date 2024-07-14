package com.daniel.finalproject

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import com.daniel.finalproject.SongData.Companion.getMp3FilePath
import java.io.File

class PlaylistActivity : AppCompatActivity(),
    EditSongDialogFragment.OnSongUpdatedListener
{

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var filteredSongList : MutableList<SongData>
    private var currentPlaylist: PlaylistData? = null
    private var lastSong:Int? = null
    override fun onSongUpdated(newSong: SongData) {
        val playlistIndex = currentPlaylist!!.songList.indexOf(newSong.songIndex)
        filteredSongList[playlistIndex] = newSong
        recyclerView.adapter?.notifyItemChanged(playlistIndex)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_activity)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
        // else is deprecated in favor of if in SDK 33
        currentPlaylist= if(Build.VERSION.SDK_INT >= 33){
            intent.getSerializableExtra("selected_playlist", PlaylistData::class.java)
        }else{
            intent.getSerializableExtra("selected_playlist") as PlaylistData
        }
        var wasFiltered = false
        val filteredIndexList = currentPlaylist!!.songList
            .filter {
                val exists = File(filesDir, "songs/$it").exists()
                if(!exists) wasFiltered = true
                exists
            }.toMutableList()
        filteredSongList =  filteredIndexList
            .map{SongData(this,it) }
            .toMutableList()
        if(wasFiltered){
            println("Error404")
            currentPlaylist = PlaylistData(this,currentPlaylist!!.playlistName,filteredIndexList,currentPlaylist!!.playlistIndex)
        }
        setCurrentSong(0)
        initSongView()
        initBottomBar()
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
    private fun onClickSongOptions(playlistIndex: Int) {
        val libraryIndex :Int = currentPlaylist!!.songList[playlistIndex]
        val songOptionsFragment = SongOptionsDialogFragment.newInstance(libraryIndex)
        songOptionsFragment.show(supportFragmentManager, "SongOptions")
    }
    private fun onClickPlaySong(playlistIndex: Int){
        if(playlistIndex==lastSong){
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.play_button)
            } else {
                mediaPlayer.start()
                playPauseButton.setImageResource(R.drawable.pause_button)
            }
            return
        }
        setCurrentSong(playlistIndex)
        mediaPlayer.start()
        playPauseButton.setImageResource(R.drawable.pause_button)
        lastSong = playlistIndex
    }

    private fun setCurrentSong(playlistIndex: Int= 0 ) {
        // preconditions valid index, data structure is correct, any and all exceptions are ignored
        if(::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
        }
        try {
            val libraryIndex:Int = currentPlaylist!!.songList[playlistIndex]
            val path = getMp3FilePath(this, libraryIndex)
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun initSongView(){
        recyclerView = findViewById(R.id.songView)
        val songViewAdapter = SongViewAdapter(
            filteredSongList,
            clickListener = { playlistIndex ->
                onClickPlaySong(playlistIndex)
            },
            longClickListener = { playlistIndex ->
                onClickSongOptions(playlistIndex)
                true
            }
        )
        recyclerView.adapter = songViewAdapter
    }
    private fun initBottomBar(){
        playPauseButton = findViewById(R.id.MasterPlayPauseButton)
        playPauseButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.play_button)
            } else {
                mediaPlayer.start()
                playPauseButton.setImageResource(R.drawable.pause_button)
            }
        }
    }
}