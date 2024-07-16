package com.daniel.finalproject

import android.app.Activity
import android.content.Context
import com.daniel.finalproject.PlaylistData.Companion.writePlaylistDataToFile
import com.daniel.finalproject.PlaylistViewFragment.OnPlaylistUpdatedListener
import java.io.File

class SongQueue{
    private var songObjects : MutableList<SongData> // MUST ONLY BE DECLARED ON INIT, NEVER OVERWRITTEN (SHARED POINTER)
    private var currentPlaylist: PlaylistData
    private var  listener: OnPlaylistUpdatedListener
    private var parentActivity: Context
    private var songOrder: MutableList<Int>
    var currentSong: Int
    var looped: Boolean = false
    var shuffled: Boolean = false
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
        songOrder = (0 until size()).toMutableList()
        currentSong = 0
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
    fun next(): Int?{
        if(currentSong == size()-1) {
            if (looped) {
                currentSong = 0
                return songOrder[currentSong]
            } else {
                return null
            }
        }
        currentSong++
        return  songOrder[currentSong]
    }
    fun prev(): Int?{
        if(currentSong == 0) {
            if (looped) {
                currentSong = size()-1
                return songOrder[currentSong]
            } else {
                return null
            }
        }
        currentSong--
        return  songOrder[currentSong]
    }
    fun toggleLoop(){
        looped = !looped
    }
    fun toggleShuffle(){
        if(shuffled){
            currentSong = songOrder[currentSong]
            songOrder = (0 until size()).toMutableList()
            shuffled = false
        }else{
            songOrder = (0 until size()).shuffled().toMutableList()
            songOrder.remove(currentSong)
            songOrder.add(0,currentSong)
            shuffled = true
        }
    }
}