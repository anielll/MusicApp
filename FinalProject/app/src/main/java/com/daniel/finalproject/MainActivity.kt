package com.daniel.finalproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.daniel.finalproject.PlaylistData.Companion.readPlaylistDataFromFile
import java.io.File
import  com.daniel.finalproject.PlaylistViewFragment.OnPlaylistUpdatedListener
import com.daniel.finalproject.PlaylistViewFragment.OnSongUpdatedListener
class MainActivity : AppCompatActivity(),
        OnPlaylistUpdatedListener,
        OnSongUpdatedListener
{
    private lateinit var playlistObjects : MutableList<PlaylistData>
    private lateinit var recyclerView : RecyclerView
    override fun onPlaylistUpdated(newPlaylist: PlaylistData) {
        playlistObjects[newPlaylist.playlistIndex] = newPlaylist
    }

    override fun onSongUpdated(newSong: SongData?, index: Int?) {
        val fragment = supportFragmentManager.findFragmentById(R.id.playlist_view_container) as? PlaylistViewFragment
        fragment?.updateSong(newSong, index)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        //init
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
        initializeDefaults(this)
        initPlaylistView()
    }
    private fun initPlaylistView(){
            val playlistFolder = File(filesDir, "playlists")
            val  playlistFileNames = playlistFolder.list()?: arrayOf()
            playlistObjects = playlistFileNames.mapNotNull {
                readPlaylistDataFromFile(this,it.substringBeforeLast('.').toInt())
            }.toMutableList()
                recyclerView = findViewById(R.id.playlistView)
            val playlistViewAdapter = PlaylistViewAdapter(
                playlistObjects,
                clickListener = { songIndex ->
                    onClickOpenPlaylist(songIndex)
                },)
            recyclerView.adapter = playlistViewAdapter
    }
    private fun onClickOpenPlaylist(playlistIndex: Int){
    val selectedPlaylist = if(playlistIndex==0){
        null
    }else{
        playlistObjects[playlistIndex - 1]
    }
        val fragment = PlaylistViewFragment().apply {
            arguments = Bundle().apply {
                putSerializable("selected_playlist", selectedPlaylist)
            }
        }
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in,R.anim.fade_out,R.anim.fade_in,R.anim.fade_out  )
            .replace(R.id.playlist_view_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}