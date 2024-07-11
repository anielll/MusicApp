package com.daniel.finalproject

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.File

val debug = true // Set to false to disable default (playlist and song) configuration for debug purposes
class MainActivity : AppCompatActivity(){
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
        if(debug) {
            initPlaylistData()
        }
        initPlaylistView()
    }
    private fun initPlaylistView(){
            val playlistFolder = File(filesDir, "playlists")
            val  playlistFileNames = playlistFolder.list()?: arrayOf()
            playlistObjects = playlistFileNames.mapNotNull {
                readPlaylistDataFromFile(this,it.substringBeforeLast('.').toInt())
            }.toMutableList()
                recyclerView = findViewById(R.id.playlistView)
            val playlistViewAdapter = PlaylistViewAdapter(playlistObjects)
            recyclerView.adapter = playlistViewAdapter
    }







    private fun initPlaylistData(){
        val playListFiles = assets.list("default_playlists") ?: arrayOf()
        playListFiles.forEachIndexed{index, playlistName ->
            try {
                val destDir = File(filesDir, "playlists")
                if (!destDir.exists()) {
                    destDir.mkdirs()
                }
                copyPlaylistFromAssets("default_playlists/$playlistName", "playlists/$index.json")
            }catch (e : Exception){
                Log.e("MainActivity","Failed to initialize default playlist ${e.message}")
            }
        }

    }
    private fun copyPlaylistFromAssets(assetFilePath: String, outputFilePath: String    ){
        val inputStream = assets.open(assetFilePath)
        val fileContents = inputStream.bufferedReader().readText()
        val playlist = PlaylistData(assetFilePath,fileContents)
        val json = Gson().toJson(playlist)
        val outputFile = File(filesDir, outputFilePath)
        outputFile.writeText(json)
    }


}