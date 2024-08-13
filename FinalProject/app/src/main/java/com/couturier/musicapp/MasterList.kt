package com.couturier.musicapp

import android.content.Context
import android.util.Log
import com.couturier.musicapp.MainActivity.Companion.appContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

// A Data Structure Storing all playlists the user has on their device
// Playlists are stored in this.list such that list[recyclerIndex] = fileIndex
// If the user does not delete any playlists, the typical data would look like:
// [0,1,2,3,...,n] which does not accomplish much
// But when the user deletes or moves playlist items in the MainActivity,
// The file and recycler indexes de-sync, which this data structure aims to solve
// Currently maintains the invariant: list[i] < list[i+1],
// Since moving items is not yet implemented
object MasterList {
    lateinit var playlistList: MutableList<Int>
    lateinit var library: PlaylistData
    fun initialize() {
        this.playlistList = readFromFile() ?: mutableListOf()
        val songList = (
                File(appContext.filesDir, "songs").list() ?: arrayOf()
                ).map { it.toInt() }.toMutableList()
        library = PlaylistData("My Library", songList, -1, null)
    }

    private fun readFromFile(): MutableList<Int>? {
        val file = File(appContext.filesDir, "metadata/master_list.json")
        return try {
            FileReader(file).use { reader ->
                val type = (object : TypeToken<MutableList<Int>>() {}).type
                Gson().fromJson(reader, type)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun savePlaylistList() {
        val metaDataDir = File(appContext.filesDir, "metadata")
        if (!metaDataDir.exists()) {
            metaDataDir.mkdirs()
        }
        try {
            val outputFile = File(appContext.filesDir, "metadata/master_list.json")
            FileWriter(outputFile).use { writer ->
                writer.write(Gson().toJson(this.playlistList))
            }
        } catch (e: IOException) {
            Log.e("MasterList", "Failed to save master list")
        }
    }

    fun addPlaylist(fileIndex: Int) {
        playlistList.add(fileIndex)
    }

    fun removePlaylist(fileIndex: Int) {
        playlistList.remove(fileIndex)
    }

    fun fileIndexOf(recyclerIndex: Int): Int {
        return if (recyclerIndex == 0) -1
        else playlistList[recyclerIndex - 1]

    }

    fun recyclerIndexOf(fileIndex: Int): Int {
        return if (fileIndex == -1) 0
        else playlistList.indexOf(fileIndex) + 1
    }

    fun nextAvailablePlaylistIndex(): Int {
        return if (playlistList.isEmpty()) 0
        else playlistList.last()+1
    }
    fun addSong(): Int {
        val newIndex = if (library.songList.isEmpty()) 0
        else library.songList.last()+1
        library.songList.add(newIndex)
        return  newIndex
    }

}