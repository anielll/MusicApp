package com.daniel.finalproject

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Serializable

class PlaylistData : Serializable {
    companion object{
        fun readPlaylistDataFromFile(context: Context, songIndex: Int): PlaylistData? {
            val file = File(context.filesDir, "playlists/$songIndex.json")
            try {
                val reader = FileReader(file)
                val playlistData = Gson().fromJson(reader, PlaylistData::class.java)
                reader.close()
                return playlistData
            } catch (e: IOException) {
                return null
            }
        }
    }
    val playlistName : String
    val songList: MutableList<Int>
    constructor(filePath: String, fileContent: String){
        this.playlistName = filePath.substringAfterLast('/')
        val data = fileContent.trim().split(Regex("\\n+"))
        songList = data.mapNotNull {
            try {
                it.trim().toInt()
            } catch (e: Exception) {
                null
            }
        }.toMutableList()
    }


}