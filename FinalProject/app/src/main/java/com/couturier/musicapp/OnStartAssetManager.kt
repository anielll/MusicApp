package com.couturier.musicapp

import android.util.Log
import com.couturier.musicapp.MainActivity.Companion.appContext
import java.io.File
import com.couturier.musicapp.SongData.Companion.importMp3FromInputStream
import com.couturier.musicapp.SongData.Companion.parseMetaData
import java.io.FileInputStream

// This class is responsible for copying debug files from /assets to /filesDir for debug purposes
// For a non-debug build, or if both booleans are false,  all this class does is MasterList.initialize(context)
class OnStartAssetManager {
    companion object {
        private const val DEBUG_FILES =
            true // Set to false to disable default song and playlist configurations
        private const val FORCE_DEBUG =
            true // Set to true to override all app data each time the app launches

        fun initializeDefaults() {
            val metaDataFolder = File(appContext.filesDir, "metadata")
            if (FORCE_DEBUG || (DEBUG_FILES && !metaDataFolder.exists())) {
                deleteFolder(File(appContext.filesDir, "songs"))
                deleteFolder(File(appContext.filesDir, "playlists"))
                deleteFolder(File(appContext.filesDir, "metadata"))
                initSongData()
                initPlaylistData()
                return
            } else {
                MasterList.initialize() // Initialize global shared object
            }
        }

        private fun initSongData() {
            val songFiles = appContext.assets.list("default_songs") ?: return
            songFiles.forEachIndexed { index, songName ->
                val destDir = File(appContext.filesDir, "songs/$index")
                if (!destDir.exists()) {
                    destDir.mkdirs()
                }
                try {
                    appContext.assets.open("default_songs/$songName").use{ inputStream ->
                        importMp3FromInputStream(
                            inputStream,
                            index,
                            songName
                        )
                        }
                    val metadata = parseMetaData(FileInputStream(File(appContext.filesDir, "songs/$index/$songName")).fd, songName)
                    SongData(
                        title = metadata.title,
                        artist = metadata.artist,
                        songIndex = index,
                        songIcon = metadata.icon
                    )
                } catch (e: Exception) {
                    Log.e("OnStartAssetManager", "Failed to copy song $index: $songName")
                }
            }
        }

        private fun initPlaylistData() {
            MasterList.initialize() // Initialize global shared object
            val playListFiles = appContext.assets.list("default_playlists") ?: return
            playListFiles.forEachIndexed { index, playlistName ->
                try {
                    copyPlaylistFromAssets("default_playlists/$playlistName", index)
                    MasterList.addPlaylist(index)
                } catch (e: Exception) {
                    Log.e("OnStartAssetManager", "Failed to copy playlist $index: $playlistName")
                }
            }
            MasterList.savePlaylistList()
        }





        private fun copyPlaylistFromAssets(assetFilePath: String, index: Int) {
            val fileContents = appContext.assets.open(assetFilePath).use { inputStream ->
                inputStream.bufferedReader().readText()
            }
            val playlistName = assetFilePath.substringAfterLast('/').substringBeforeLast(".")
            val songList = fileContents
                .trim()
                .split(Regex("\\n+"))
                .mapNotNull {
                    it.trim().toIntOrNull()
                }.toMutableList()
            PlaylistData(playlistName, songList, index, null)
        }
        fun deleteFolder(folder: File): Boolean {
            if (folder.isDirectory) {
                val children = folder.listFiles()
                if (children != null) {
                    for (child in children) {
                        deleteFolder(child)
                    }
                }
            }
            return folder.delete()
        }
    }
}