package com.daniel.finalproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.daniel.finalproject.PlaylistData.Companion.readPlaylistDataFromFile
import java.io.File
class MainActivity : AppCompatActivity()
{
    private lateinit var playlistObjects : MutableList<PlaylistData>
    private lateinit var recyclerView : RecyclerView
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
        val intent = Intent(this, PlaylistActivity::class.java)

        intent.putExtra("selected_playlist", selectedPlaylist)
        startActivity(intent)
    }
}