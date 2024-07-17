package com.daniel.finalproject

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

val debug_files =
    true // Set to false to disable default (playlist and song) configuration for debug purposes

fun initializeDefaults(context: Context) {
    MasterList.initialize(context)
    if (debug_files) {
        PlaylistViewFragment.deleteFolder(File(context.filesDir,"songs"))
        PlaylistViewFragment.deleteFolder(File(context.filesDir,"playlists"))
        PlaylistViewFragment.deleteFolder(File(context.filesDir,"metadata"))
        initSongData(context)
        initPlaylistData(context)
    }
    initLibrary(context)
}

private fun initLibrary(context: Context) {
    val songsFolder = File(context.filesDir, "songs")
    val allSongs = songsFolder.list()!!.map { it.toInt() }.toMutableList()
    PlaylistData(context, "My Library", allSongs, -1)
}

private fun initPlaylistData(context: Context) {
    val playListFiles = context.assets.list("default_playlists") ?: arrayOf()
    playListFiles.forEachIndexed { index, playlistName ->
        try {
            copyPlaylistFromAssets(context, "default_playlists/$playlistName", index)
            MasterList.add(index)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize default playlist ${e.message}")
        }
    }
    MasterList.save(context)
}


private fun copyPlaylistFromAssets(context: Context, assetFilePath: String, index: Int) {
    val inputStream = context.assets.open(assetFilePath)
    val fileContents = inputStream.bufferedReader().readText()
    val playlistName = assetFilePath.substringAfterLast('/')
    val playlistData = fileContents.trim().split(Regex("\\n+"))
    val songList = playlistData.mapNotNull {
        try {
            it.trim().toInt()
        } catch (e: Exception) {
            null
        }
    }.toMutableList()
    PlaylistData(context, playlistName, songList, index)
}

private fun initSongData(context: Context) {
    val songFiles = context.assets.list("default_songs") ?: arrayOf()
    songFiles.forEachIndexed { index, songName ->
        try {
            val destDir = File(context.filesDir, "songs/$index")
            if (!destDir.exists()) {
                destDir.mkdirs()
                copymp3FromAssets(context, "default_songs/$songName", "songs/$index/$songName")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize songs ${e.message}")
        }
    }
}


private fun copymp3FromAssets(context: Context, assetFileName: String, outputFilePath: String) {
    val inputStream: InputStream = context.assets.open(assetFileName)
    val outputStream: OutputStream = FileOutputStream(File(context.filesDir, outputFilePath))
    val buffer = ByteArray(1024)
    while (true) {
        val temp = inputStream.read(buffer)
        if (temp == -1) {
            break
        }
        outputStream.write(buffer, 0, temp)
    }

    outputStream.flush()
    inputStream.close()
    outputStream.close()
}