package com.couturier.musicapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import com.couturier.musicapp.MainActivity.Companion.appContext
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream

class SongData
{
    val title: String
    val artist: String
    val songIndex: Int
    val icon: Bitmap?
    constructor(title: String,artist: String,songIndex: Int,songIcon: Bitmap?){ // Edit Constructor
        this.title= title
        this.artist= artist
        this.songIndex = songIndex
        this.icon = songIcon
        saveSongDataToFile(this)
    }
    constructor(libraryIndex: Int) { // Init Constructor
        val songData = readSongDataFromFile(libraryIndex) // Read save file
        if (songData != null) { // If save exists, set this = save then return
            this.title = songData.title
            this.artist = songData.artist
            this.songIndex = songData.songIndex
            this.icon = songData.icon
            return
        }
        // Else, parse mp3file for data
        val mp3FilePath = getMp3FilePath(libraryIndex)
        val metaData = parseMetaData(
            fileDescriptor = FileOutputStream(mp3FilePath).fd,
            fileName = mp3FilePath.substringAfterLast("/")
        )
        title = metaData.title
        artist = metaData.artist
        songIndex = libraryIndex
        icon = metaData.icon
        saveSongDataToFile(this)
    }
companion object {
    data class SongMetadata(
        val title: String,
        val artist: String,
        val icon: Bitmap?
    )
    fun getMp3FilePath(libraryIndex: Int):String{
        val rootDir = File(appContext.filesDir, "songs/$libraryIndex")
        val songFiles = rootDir.list() ?: arrayOf()
        val mp3File = songFiles.firstOrNull { it.endsWith(".mp3") }
        return File(rootDir, mp3File!!).absolutePath
    }
    fun parseMetaData(fileDescriptor: FileDescriptor, fileName: String): SongMetadata{
        var title = ""
        var artist = ""
        var icon: Bitmap? = null
        try{
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(fileDescriptor)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
            icon = toBitMap(retriever.embeddedPicture)
        }
        }catch (e: Exception){
            // no change in data
        }

        val (inferredTitle:String , inferredArtist:String) = titleAndArtistFromFileName(fileName)
        if(title == "" || artist ==""){
            title = inferredTitle
            artist= inferredArtist

        }
        return SongMetadata(title, artist, icon)
    }

    data class SongProperties(
        val title: String,
        val artist: String,
        val songIndex: Int
    )
    fun readSongDataFromFile(songIndex: Int): SongData? {
        val songDir = File(appContext.filesDir, "songs/$songIndex")
        val propertiesFile = File(songDir, "properties.json")
        val properties= FileReader(propertiesFile).use { reader ->
                Gson().fromJson(reader, SongProperties::class.java)
            }

        val pngFilePath = File(songDir, "icon.png")
        var pngFile: ByteArray? = null
        try {
            pngFile = pngFilePath.readBytes()
        } catch (e: IOException) {
            Log.e("SongData", "Failed to read song icon.")
        }
        val pngBitmap = toBitMap(pngFile)
        return SongData(properties.title, properties.artist, properties.songIndex, pngBitmap)
    }
    private fun saveSongDataToFile(song:SongData) {
        val songProperties = SongProperties(song.title,song.artist,song.songIndex)
        val songJson = Gson().toJson(songProperties)
        val fileIndex = songProperties.songIndex
        val songDir = File(appContext.filesDir, "songs/$fileIndex")
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
        if(song.icon!=null){
            val file = File(songDir, "icon.png")
            try {
                file.writeBytes(toByteArray(song.icon))
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
    private fun titleAndArtistFromFileName(file: String): Pair<String, String> {
        val fileName= file.substringBeforeLast('.')
        val dashIndex = fileName.indexOf("-")
        var title: String
        var artist: String
        if (dashIndex != -1) {
            artist = fileName.substring(0, dashIndex).trim()
            title = fileName.substring(dashIndex + 1).trim()
            Pair(artist, title)
        } else {
            title = fileName.trim()
            artist =""
        }
        val parenthesesPattern = "\\(([^)]+)\\)".toRegex()
        val parenMatches = parenthesesPattern.findAll(title)
        for (match in parenMatches) {
            val textInParentheses = match.groupValues[1]
            artist = if(artist==""){
                textInParentheses.trim()
            } else{
                "$artist, $textInParentheses".trim()
            }
        }
        title = title.replace(parenthesesPattern, "").trim()

        val bracketsPattern = "\\[([^)]+)]".toRegex()
        val bracketMatches = bracketsPattern.findAll(title)
        for (match in bracketMatches) {
            val textInBrackets = match.groupValues[1]
            artist = if(artist==""){
                textInBrackets.trim()
            } else{
                "$artist, $textInBrackets".trim()
            }
        }
        title = title.replace(bracketsPattern, "").trim()

        return  Pair(title,artist)
    }

    fun importMp3FromInputStream(
        inputStream: InputStream,
        destinationIndex: Int,
        fileName: String
    ) {
            FileOutputStream(File(appContext.filesDir, "songs/$destinationIndex/$fileName")).use { outputStream ->
                val buffer = ByteArray(1024)
                var numBytes: Int
                while (inputStream.read(buffer).also { numBytes = it } != -1) {
                    outputStream.write(buffer, 0, numBytes)
                }
                outputStream.flush()
            }
    }



}
}