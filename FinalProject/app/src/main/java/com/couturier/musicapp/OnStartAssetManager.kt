package com.couturier.musicapp

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream


// This class is responsible for copying debug files from /assets to /filesDir for debug purposes
// For a non-debug build, or if both booleans are false,  all this class does is MasterList.initialize(context)
class OnStartAssetManager {
    companion object {
        private const val DEBUG_FILES =
            true // Set to false to disable default song and playlist configurations
        private const val FORCE_DEBUG =
            false // Set to true to override all app data each time the app launches

        fun initializeDefaults(context: Context) {
            val metaDataFolder = File(context.filesDir, "metadata")
            if (FORCE_DEBUG || (DEBUG_FILES && !metaDataFolder.exists())) {
                PlaylistViewFragment.deleteFolder(File(context.filesDir, "songs"))
                PlaylistViewFragment.deleteFolder(File(context.filesDir, "playlists"))
                PlaylistViewFragment.deleteFolder(File(context.filesDir, "metadata"))
                MasterList.initialize(context) // Initialize global shared object
                initSongData(context)
                initPlaylistData(context)
                initLibrary(context)
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
                    copyMp3FromAssets(
                        context,
                        "default_songs/$songName",
                        "songs/$index/$songName"
                    )
                } catch (e: Exception) {
                    Log.e("OnStartAssetManager", "Failed to copy song $index: $songName")
                }
            }
        }

        private fun initPlaylistData(context: Context) {
            val playListFiles = context.assets.list("default_playlists") ?: return
            playListFiles.forEachIndexed { index, playlistName ->
                try {
                    copyPlaylistFromAssets(context, "default_playlists/$playlistName", index)
                    MasterList.add(index)
                } catch (e: Exception) {
                    Log.e("OnStartAssetManager", "Failed to copy playlist $index: $playlistName")
                }
            }
            MasterList.save(context)
        }

        private fun initLibrary(context: Context) {
            val songsFolder = File(context.filesDir, "songs")
            val allSongs: Array<String> = songsFolder.list() ?: arrayOf()
            val songMap = allSongs.map { it.toInt() }.toMutableList()
            PlaylistData(context, "My Library", songMap, -1, null)
        }

        private fun copyMp3FromAssets(
            context: Context,
            assetFileName: String,
            outputFilePath: String
        ) {
            context.assets.open(assetFileName).use { inputStream ->
                FileOutputStream(File(context.filesDir, outputFilePath)).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var numBytes: Int
                    while (inputStream.read(buffer).also { numBytes = it } != -1) {
                        outputStream.write(buffer, 0, numBytes)
                    }
                    outputStream.flush()
                }
            }
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
    }
}