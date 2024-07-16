package com.daniel.finalproject

import android.app.Activity
import android.content.Context
import com.daniel.finalproject.PlaylistData.Companion.writePlaylistDataToFile
import com.daniel.finalproject.PlaylistViewFragment.OnPlaylistUpdatedListener
import java.io.File

class SongQueue{
    private lateinit var songObjects : MutableList<SongData> // MUST ONLY BE DECLARED ON INIT, NEVER OVERWRITTEN (SHARED POINTER)
    private lateinit var currentPlaylist: PlaylistData
    private lateinit var  listener: OnPlaylistUpdatedListener
    private lateinit var parentActivity: Context
    constructor(activity: Activity,playlist: PlaylistData){
        this.currentPlaylist = playlist
        this.listener = activity as OnPlaylistUpdatedListener
        this.parentActivity = activity
        var wasFiltered = false
        val filteredIndexList = currentPlaylist.songList
            .filter { // filter out songs that have been deleted
                val exists = File(activity.filesDir, "songs/$it").exists()
                if(!exists) wasFiltered = true
                exists
            }.toMutableList()
        songObjects =  filteredIndexList
            .map{SongData(activity,it) }
            .toMutableList()
        if(wasFiltered){ // reflect any null references in file storage
            currentPlaylist = PlaylistData(activity,currentPlaylist.playlistName,filteredIndexList,currentPlaylist.playlistIndex)
            println("PlaylistUpdated")
            listener.onPlaylistUpdated(currentPlaylist)
        }
    }
    fun libraryIndexOf(playlistIndex: Int) :Int{
        return currentPlaylist.songList[playlistIndex]
    }
    fun playlistIndexOf(libraryIndex: Int): Int{
        return currentPlaylist.songList.indexOf(libraryIndex)
    }
    fun playlistName():String{
        return currentPlaylist.playlistName
    }
    fun getSongObjects(): MutableList<SongData>{
        return songObjects
    }
    fun size(): Int{
        return songObjects.size
    }
    fun delete(libraryIndex: Int){
        songObjects.removeAt(playlistIndexOf(libraryIndex)) // update first
        currentPlaylist.songList.remove(libraryIndex) // update second
        writePlaylistDataToFile(parentActivity,currentPlaylist)
        listener.onPlaylistUpdated(currentPlaylist)
    }
    fun update(newSong: SongData){
        songObjects[playlistIndexOf(newSong.songIndex)] = newSong
        // no write or listener necessary because playlist object is unchanged since it stores only indices
    }
    fun add(libraryIndex: Int, newSong: SongData){
        songObjects.add(newSong) // update first
        currentPlaylist.songList.add(libraryIndex) // update second
        writePlaylistDataToFile(parentActivity,currentPlaylist)
        listener.onPlaylistUpdated(currentPlaylist)
    }
}