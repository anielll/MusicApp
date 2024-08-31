package com.couturier.musicapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.couturier.musicapp.MainActivity.Companion.appContext
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class PlaylistData(
    val playlistName: String,
    val songList: MutableList<Int>,
    playlistIndex: Int,
    val icon: Bitmap?
) {
    val fileIndex: Int = playlistIndex

    init {
            writePlaylistDataToFile(this)
    }
    interface OnPlaylistUpdatedListener {
        fun onPlaylistUpdate(newPlaylist: PlaylistData?, fileIndex: Int? = null)
    }
    companion object{
        data class PlaylistProperties(
            val playlistName : String,
            val songList: MutableList<Int>,
            val fileIndex: Int
        )
        fun readPlaylistDataFromFile(playlistIndex: Int): PlaylistData? {
            val playlistDir = File(appContext.filesDir, "playlists/$playlistIndex")
            val propertiesDir = File(playlistDir, "properties.json")
            val properties: PlaylistProperties?
            try {
                properties = FileReader(propertiesDir).use { reader ->
                    Gson().fromJson(reader, PlaylistProperties::class.java)
                }
            } catch (e: IOException) {
                return  null
            }
            val pngFilePath = File(playlistDir, "icon.png")
            var pngFile: ByteArray? = null
            try {
                pngFile = pngFilePath.readBytes()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("SongData", "Failed to read song icon.")
            }
            val pngBitmap = toBitMap(pngFile)
            return PlaylistData(properties.playlistName,properties.songList,properties.fileIndex,pngBitmap)
        }
        fun writePlaylistDataToFile(playlist:PlaylistData){
            if(playlist.fileIndex==-1) {
                return
            }
            val playlistDir= File(appContext.filesDir, "playlists/${playlist.fileIndex}")
            val properties = PlaylistProperties(playlist.playlistName,playlist.songList,playlist.fileIndex)
            val playlistJson = Gson().toJson(properties)
            if (!playlistDir.exists()) {
                playlistDir.mkdirs()
            }
            try {
                val outputFile = File(playlistDir, "properties.json" )
                FileWriter(outputFile).use { writer ->
                    writer.write(playlistJson)
                }
            } catch (e: IOException) {
                Log.e("MusicApp", "Error writing file: ${e.message}")
            }
            if(playlist.icon!=null){
                val file = File(playlistDir, "icon.png")
                try {
                    file.writeBytes(toByteArray(playlist.icon))
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