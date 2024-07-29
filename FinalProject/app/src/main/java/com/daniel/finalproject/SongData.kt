package com.daniel.finalproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class SongData
{
    val title: String
    val artist: String
    val songIndex: Int
    val art: Bitmap?
    constructor(context: Context,title: String,artist: String,songIndex: Int,art: Bitmap?){ // Edit Constructor
        this.title= title
        this.artist= artist
        this.songIndex = songIndex
        this.art = art
        saveSongDataToFile(context,this)
    }
    constructor(context: Context, libraryIndex: Int) { // Init Constructor
        val songData = readSongDataFromFile(context, libraryIndex) // Read save file
        if (songData != null) { // If save exists, set this = save then return
            this.title = songData.title
            this.artist = songData.artist
            this.songIndex = songData.songIndex
            this.art = songData.art
            return
        }
        // Else, parse mp3file for data
        val mp3FilePath = getMp3FilePath(context,libraryIndex)
        val metaData = parseMetaData(mp3FilePath)
        title = metaData.title
        artist = metaData.artist
        songIndex = libraryIndex
        art = metaData.art
        saveSongDataToFile(context,this)
    }
companion object {
    data class SongMetadata(
        val title: String,
        val artist: String,
        val art: Bitmap?
    )
    fun getMp3FilePath(context: Context,libraryIndex: Int):String{
        val rootDir = File(context.filesDir, "songs/$libraryIndex")
        val songFiles = rootDir.list() ?: arrayOf()
        val mp3File = songFiles.firstOrNull { it.endsWith(".mp3") }
        return File(rootDir, mp3File!!).absolutePath
    }
    private fun parseMetaData(mp3FilePath: String): SongMetadata{
        val retriever = MediaMetadataRetriever()
        var title = ""
        var artist = ""
        var art: Bitmap? = null
        try {
            retriever.setDataSource(mp3FilePath)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
            val artByteArray= retriever.embeddedPicture
            art = toBitMap(artByteArray)
        } catch (e: Exception) {
            // use default values of "", "", empty
        } finally {
            retriever.release()
        }
        if(title == ""){
            title = mp3FilePath.substringAfterLast('/')
        }
        return SongMetadata(title, artist, art)
    }
    data class SongProperties(
        val title: String,
        val artist: String,
        val songIndex: Int
    )
    fun readSongDataFromFile(context: Context, songIndex: Int): SongData? {
        val songDir = File(context.filesDir, "songs/$songIndex")
        val propertiesFile = File(songDir, "properties.json")
        val properties :SongProperties?
        try {
            properties= FileReader(propertiesFile).use { reader ->
                Gson().fromJson(reader, SongProperties::class.java)
            }
        } catch (e: IOException) {
            return null
        }
        val pngFilePath = File(songDir, "icon.png")
        var pngFile: ByteArray? = null
        try {
            pngFile = pngFilePath.readBytes()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("SongData", "Failed to read song icon.", e)
        }
        val pngBitmap = toBitMap(pngFile)
        return SongData(context, properties.title, properties.artist, properties.songIndex, pngBitmap)
    }
    fun saveSongDataToFile(context: Context, song:SongData) {
        val songProperties = SongProperties(song.title,song.artist,song.songIndex)
        val songJson = Gson().toJson(songProperties)
        val fileIndex = songProperties.songIndex
        val songDir = File(context.filesDir, "songs/$fileIndex")
        if (!songDir.exists()) {
            songDir.mkdirs()
        }
        try {
            val file = File(songDir, "properties.json")
            FileWriter(file).use {
                it.write(songJson)
            }
        } catch (e: IOException) {
            Log.e("SongData", "Failed to save song properties.", e)
        }
        if(song.art!=null){
            val file = File(songDir, "icon.png")
            try {
                file.writeBytes(toByteArray(song.art))
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("SongData", "Failed to save song icon.", e)
            }
        }
    }
    private fun toBitMap(byteArray: ByteArray?): Bitmap?{
        return if(byteArray!=null) {
            BitmapFactory.decodeByteArray(byteArray, 0,byteArray.size)
        }else{
            null
        }
    }
    private fun toByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }
}
}