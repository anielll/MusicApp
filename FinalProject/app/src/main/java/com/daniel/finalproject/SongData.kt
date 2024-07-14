package com.daniel.finalproject

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class SongData
{
    val title: String
    val artist: String
    val songIndex: Int
    constructor(context: Context,title: String,artist: String,songIndex: Int){
        this.title= title
        this.artist= artist
        this.songIndex = songIndex
        saveSongDataToFile(context,this)
    }
    constructor(context: Context, libraryIndex: Int) {
        val songData = readSongDataFromFile(context, libraryIndex)
        if (songData != null) {
            this.title = songData.title
            this.artist = songData.artist
            this.songIndex = songData.songIndex
        } else {
            val metaData = parseMetaData(getMp3FilePath(context,libraryIndex))
            if(metaData[0]==""){
                val filePath = getMp3FilePath(context,libraryIndex)
                this.title = filePath.substringAfterLast('/')
            }else{
                this.title = metaData[0]
            }
            this.artist = metaData[1]
            this.songIndex = libraryIndex
            saveSongDataToFile(context,this)
        }
    }
companion object {
    fun getMp3FilePath(context: Context,libraryIndex: Int):String{
        val rootDir = File(context.filesDir, "songs/$libraryIndex")
        val songFiles = rootDir.list() ?: arrayOf()
        val mp3File = songFiles.firstOrNull { it.endsWith(".mp3") }
        return File(rootDir, mp3File!!).absolutePath
    }
    private fun parseMetaData(mp3FilePath: String): Array<String> {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(mp3FilePath)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) ?: "0"
            retriever.release()
            return arrayOf(title, artist, duration)
        } catch (e: Exception) {
            Log.e("SongData", "Error retrieving metadata", e)
            return arrayOf()
        } finally {
            retriever.release()
        }
    }

    fun readSongDataFromFile(context: Context, songIndex: Int): SongData? {
        val gson = Gson()
        val songDir = File(context.filesDir, "songs/$songIndex")
        val file = File(songDir, "properties.json")

        try {
            val songData = FileReader(file).use { reader ->
                gson.fromJson(reader, SongData::class.java)
            }
            return songData
        } catch (e: IOException) {
            Log.e("EditSong", "Error reading file: ${e.message}")
            return null
        }
    }

    fun saveSongDataToFile(context: Context, song:SongData) {
        val gson = Gson()
        val songJson = gson.toJson(song)

        val fileIndex = song.songIndex
        val songDir = File(context.filesDir, "songs/$fileIndex")

        try {
            if (!songDir.exists()) {
                songDir.mkdirs()
            }
            val file = File(songDir, "properties.json")
            FileWriter(file).use {
                it.write(songJson)
            }
        } catch (e: IOException) {
            Log.e("EditSong", "Error writing file: ${e.message}")
        }
    }
}
}