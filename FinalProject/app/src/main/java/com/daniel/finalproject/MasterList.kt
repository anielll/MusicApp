package com.daniel.finalproject

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object MasterList {
    private lateinit var list: MutableList<Int>
    fun initialize(context:Context){
        val masterList: MutableList<Int>? = readListFromFile(context)
        if(masterList!= null){
            this.list = masterList
        }else{
            this.list = mutableListOf()
        }
    }

    fun add(fileIndex: Int) {
        list.add(fileIndex)
    }
    fun remove(fileIndex: Int){
        list.remove(fileIndex)
    }
    fun get():MutableList<Int>{
        return list
    }

    private fun readListFromFile(context: Context): MutableList<Int>? {
        val file = File(context.filesDir, "metadata/master_list.json")
        try {
            val masterList = FileReader(file).use { reader ->
                val type =  (object : TypeToken<MutableList<Int>>(){}).type
                Gson().fromJson<MutableList<Int>>(reader,  type)
            }
            return masterList
        } catch (e: IOException) {
            return null
        }
    }

    fun save(context: Context) {
        val metaDataDir = File(context.filesDir, "metadata")
        try {
            if (!metaDataDir.exists()) {
                metaDataDir.mkdirs()
            }
            val outputFile = File(context.filesDir, "metadata/master_list.json")
            FileWriter(outputFile).use { writer ->
                writer.write(Gson().toJson(this.list))
            }
        } catch (e: IOException) {
            Log.e("MusicApp", "Error writing file: ${e.message}")
        }
    }
    fun fileIndexOf(recyclerIndex: Int) :Int{
        if(recyclerIndex==0){ // LIBRARY
            return -1
        }
        return list[recyclerIndex-1]
    }
    fun recyclerIndexOf(fileIndex: Int): Int{
        if(fileIndex==-1){ // LIBRARY
            return 0
        }
        return list.indexOf(fileIndex)+1
    }
    fun last():Int{
        if(list.isEmpty()){
            return 0
        }else {
            return list.last()
        }
    }

}