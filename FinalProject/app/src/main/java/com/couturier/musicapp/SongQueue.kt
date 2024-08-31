package com.couturier.musicapp

import android.graphics.Bitmap
import com.couturier.musicapp.MainActivity.Companion.appContext
import com.couturier.musicapp.PlaylistData.Companion.writePlaylistDataToFile
import java.io.File
import kotlin.random.Random

class SongQueue(playlistNumber: Int){
    private var songObjects : MutableList<SongData> // MUST ONLY BE DECLARED ON INIT, NEVER OVERWRITTEN (SHARED POINTER)
    private var currentPlaylist: PlaylistData =
        if (playlistNumber != -1)
            PlaylistData.readPlaylistDataFromFile(playlistNumber)!!
        else {
            MasterList.library
        }
    private var songOrder: MutableList<Int>
    private var queueIndex: Int
    var looped: Boolean = false
    var shuffled: Boolean = false
    init{
        var wasFiltered = false
        val filteredIndexList = currentPlaylist.songList
            .filter { // filter out songs that have been deleted
                val exists = File(appContext.filesDir, "songs/$it").exists()
                if(!exists) wasFiltered = true
                exists
            }.toMutableList()
        songObjects =  filteredIndexList
            .map{SongData(it) }
            .toMutableList()
        if(wasFiltered){ // reflect any null references in file storage
            currentPlaylist = PlaylistData(currentPlaylist.playlistName,filteredIndexList,currentPlaylist.fileIndex,currentPlaylist.icon)
        }
        songOrder = (0 until size()).toMutableList()
        queueIndex = -1
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
    fun icon(): Bitmap?{
        return currentPlaylist.icon
    }
    fun playlistNumber(): Int{
        return currentPlaylist.fileIndex
    }

    fun delete(libraryIndex: Int){
        val playlistIndex = playlistIndexOf(libraryIndex)
        songObjects.removeAt(playlistIndex) // update first
        currentPlaylist.songList.remove(libraryIndex) // update second
        writePlaylistDataToFile(currentPlaylist)
        songOrder.remove(playlistIndex)
        songOrder = songOrder.map{
            if(it<playlistIndex){
                it
            }else{
                it-1
            }
        }.toMutableList()
    }
    fun replace(newSong: SongData){
        songObjects[playlistIndexOf(newSong.songIndex)] = newSong
        // no write or listener necessary because playlist object is unchanged since it stores only indices
    }
    fun add(libraryIndex: Int, newSong: SongData){
        songObjects.add(newSong) // update first
        if(currentPlaylist.fileIndex!=-1){
            currentPlaylist.songList.add(libraryIndex) // update second
            writePlaylistDataToFile(currentPlaylist)
        }
        if(shuffled){
            val newSongIndex = Random.nextInt(songOrder.size)
            songOrder.add(newSongIndex,songOrder.size)
        }else{
            songOrder.add(songOrder.size)
        }
    }
    fun next(): Int?{
        if(queueIndex<0){
            queueIndex = 0
            return songOrder[queueIndex]
        }
        if(queueIndex == size()-1) {
            if (looped) {
                queueIndex = 0
                return songOrder[queueIndex]
            } else {
                return null
            }
        }
        queueIndex++
        return  songOrder[queueIndex]
    }
    fun prev(): Int?{
        if(queueIndex <0){
            queueIndex = size()-1
            return songOrder[queueIndex]
        }
        if(queueIndex == 0) {
            if (looped) {
                queueIndex = size()-1
                return songOrder[queueIndex]
            } else {
                return null
            }
        }
        queueIndex--
        return  songOrder[queueIndex]
    }
    fun toggleLoop(){
        looped = !looped
    }
    fun toggleShuffle(){
        if(shuffled){
            queueIndex = if(queueIndex==-1){
                -1
            }else {
                songOrder[queueIndex]
            }
            songOrder = (0 until size()).toMutableList()
            shuffled = false
        }else{
            songOrder = (0 until size()).shuffled().toMutableList()
            if(queueIndex>=0){
                songOrder.remove(queueIndex)
                songOrder.add(0,queueIndex)
                queueIndex = 0
            }
            shuffled = true
        }
    }
    fun setQueueCursor(playlistIndex: Int){
        queueIndex = if(playlistIndex==-1){
            -1
        }else{
         songOrder.indexOf(playlistIndex)
        }
    }
}