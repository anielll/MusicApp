package com.couturier.musicapp

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.couturier.musicapp.SongData.Companion.SongMetadata
import com.couturier.musicapp.SongData.Companion.titleAndArtistFromFileName
import com.couturier.musicapp.SongData.Companion.toBitMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class UriManager {
    companion object{


        fun parseMetaData(context: Context, uri: Uri): SongMetadata {
            val retriever = MediaMetadataRetriever()
            var title = ""
            var artist = ""
            var icon: Bitmap? = null
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { uriFile ->
                    retriever.setDataSource(uriFile.fileDescriptor)
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                    val artByteArray = retriever.embeddedPicture
                    icon = toBitMap(artByteArray)
                }
            } catch (e: Exception) {
                // use default values of "", "", empty
            } finally {
                retriever.release()
            }
            val (inferredTitle: String, inferredArtist: String) = titleAndArtistFromFileName(
                getFileNameFromUri(context,uri)!!.substringAfterLast('/')
            )
            if (title == "" || artist == "") {
                title = inferredTitle
                artist = inferredArtist

            }
            return SongMetadata(title, artist, icon)
        }

        fun getFileNameFromUri(context:Context, uri: Uri): String? {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    cursor.moveToFirst()
                    val colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    return cursor.getString(colIndex)
                }
            } catch (e: Exception) {
                return null
            }
            return null
        }
        fun copyMp3ToInternalStorage(context: Context, uri: Uri, filename:String, newSongIndex: Int) {
            val file = File(context.filesDir, "songs/$newSongIndex")
            if (!file.exists()) {
                file.mkdirs()
            }
            val destinationFile = File(file, filename)
            val inputStream: InputStream =
                context.contentResolver.openInputStream(uri)!!
            val outputStream = FileOutputStream(destinationFile)
            val buffer = ByteArray(4 * 1024)
            while (true) {
                val temp = inputStream.read(buffer)
                if (temp == -1) {
                    break
                }
                outputStream.write(buffer, 0, temp)
            }
            inputStream.close()
            outputStream.close()
            return
        }
    }

}