package com.couturier.musicapp

import android.content.Context
import android.util.Log
import java.io.File
import com.couturier.musicapp.SongData.Companion.importMp3FromInputStream

// This class is responsible for copying debug files from /assets to /filesDir for debug purposes
// For a non-debug build, or if both booleans are false,  all this class does is MasterList.initialize(context)
class OnStartAssetManager {
    companion object {
        private const val DEBUG_FILES =
            true // Set to false to disable default song and playlist configurations
        private const val FORCE_DEBUG =
            true // Set to true to override all app data each time the app launches

        fun initializeDefaults(context: Context) {
            val metaDataFolder = File(context.filesDir, "metadata")
            if (FORCE_DEBUG || (DEBUG_FILES && !metaDataFolder.exists())) {
                deleteFolder(File(context.filesDir, "songs"))
                deleteFolder(File(context.filesDir, "playlists"))
                deleteFolder(File(context.filesDir, "metadata"))
                initSongData(context)
                initPlaylistData(context)
                return
            } else {
                MasterList.initialize(context) // Initialize global shared object
            }
        }

        private fun initSongData(context: Context) {
            val songFiles = context.assets.list("default_songs") ?: return
            songFiles.forEachIndexed { index, songName ->
                val destDir = File(context.filesDir, "songs/$index")
                if (!destDir.exists()) {
                    destDir.mkdirs()
                }
                try {
                    context.assets.open("default_songs/$songName").use{ inputStream ->
                        importMp3FromInputStream(
                            context,
                            inputStream,
                            index,
                            songName
                        )
                    }
                } catch (e: Exception) {
                    Log.e("OnStartAssetManager", "Failed to copy song $index: $songName")
                }
            }
        }

        private fun initPlaylistData(context: Context) {
            MasterList.initialize(context) // Initialize global shared object
            val playListFiles = context.assets.list("default_playlists") ?: return
            playListFiles.forEachIndexed { index, playlistName ->
                try {
                    copyPlaylistFromAssets(context, "default_playlists/$playlistName", index)
                    MasterList.addPlaylist(index)
                } catch (e: Exception) {
                    Log.e("OnStartAssetManager", "Failed to copy playlist $index: $playlistName")
                }
            }
            MasterList.savePlaylistList(context)
        }





        private fun copyPlaylistFromAssets(context: Context, assetFilePath: String, index: Int) {
            val fileContents = context.assets.open(assetFilePath).use { inputStream ->
                inputStream.bufferedReader().readText()
            }
            val playlistName = assetFilePath.substringAfterLast('/').substringBeforeLast(".")
            val songList = fileContents
                .trim()
                .split(Regex("\\n+"))
                .mapNotNull {
                    it.trim().toIntOrNull()
                }.toMutableList()
            PlaylistData(context, playlistName, songList, index, null)
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