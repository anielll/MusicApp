package com.couturier.musicapp

import android.content.Context
import android.util.Log
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
    private lateinit var list: MutableList<Int>
    fun initialize(context: Context) {
        this.list = readFromFile(context) ?: mutableListOf()
    }

    fun add(fileIndex: Int) {
        list.add(fileIndex)
    }

    fun remove(fileIndex: Int) {
        list.remove(fileIndex)
    }

    fun get(): MutableList<Int> {
        return list
    }

    private fun readFromFile(context: Context): MutableList<Int>? {
        val file = File(context.filesDir, "metadata/master_list.json")
        return try {
            FileReader(file).use { reader ->
                val type = (object : TypeToken<MutableList<Int>>() {}).type
                Gson().fromJson(reader, type)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun save(context: Context) {
        val metaDataDir = File(context.filesDir, "metadata")
        if (!metaDataDir.exists()) {
            metaDataDir.mkdirs()
        }
        try {
            val outputFile = File(context.filesDir, "metadata/master_list.json")
            FileWriter(outputFile).use { writer ->
                writer.write(Gson().toJson(this.list))
            }
        } catch (e: IOException) {
            Log.e("MasterList", "Failed to save master list")
        }
    }

    fun fileIndexOf(recyclerIndex: Int): Int {
        return if (recyclerIndex == 0) -1
        else list[recyclerIndex - 1]

    }

    fun recyclerIndexOf(fileIndex: Int): Int {
        return if (fileIndex == -1) 0
        else list.indexOf(fileIndex) + 1
    }

    fun nextAvailableIndex(): Int {
        return if (list.isEmpty()) 0
        else list.last()
    }

}