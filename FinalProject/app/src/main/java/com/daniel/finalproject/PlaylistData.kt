package com.daniel.finalproject

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class PlaylistData{
    val playlistName : String
    val songList: MutableList<Int>
    val fileIndex: Int
    constructor(context: Context,playlistName:String, songList: MutableList<Int>,playlistIndex: Int){
        this.playlistName=playlistName
        this.songList=songList
        this.fileIndex = playlistIndex
        writePlaylistDataToFile(context,this)
    }
    companion object{
        fun readPlaylistDataFromFile(context: Context, songIndex: Int): PlaylistData? {
            val file = File(context.filesDir, "playlists/$songIndex.json")
            try {
                val playlistData = FileReader(file).use { reader ->
                    Gson().fromJson(reader, PlaylistData::class.java)
                }
                return playlistData
            } catch (e: IOException) {
                return null
            }
        }
        fun writePlaylistDataToFile(context: Context,playlist:PlaylistData){
            val playlistDir= File(context.filesDir, "playlists")
            val playlistJson = Gson().toJson(playlist)
            try {
                if (!playlistDir.exists()) {
                    playlistDir.mkdirs()
                }
                val outputFile = File(context.filesDir, "playlists/${playlist.fileIndex}.json" )
                FileWriter(outputFile).use { writer ->
                    writer.write(playlistJson)
                }
            } catch (e: IOException) {
                Log.e("MusicApp", "Error writing file: ${e.message}")
            }
        }
    }

}