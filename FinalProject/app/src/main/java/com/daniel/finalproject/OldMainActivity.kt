package com.daniel.finalproject

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class OldMainActivity : AppCompatActivity(), EditSongDialogFragment.OnSongUpdatedListener {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var songObjects: MutableList<SongData>
    private var lastSong:Int? = null

    override fun onSongUpdated(newSong: SongData, songIndex: Int) {
        songObjects[songIndex] = newSong
        recyclerView.adapter?.notifyItemChanged(songIndex)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        //init
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false
        initSongData()
        initSongView()
        initBottomBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }

    private fun onClickSongOptions(songIndex: Int) {
        val songOptionsFragment = SongOptionsDialogFragment.newInstance(songIndex)
        songOptionsFragment.show(supportFragmentManager, "SongOptions")
    }
    private fun onClickPlaySong(songIndex: Int){
        if(songIndex==lastSong){
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.play_button)
            } else {
                mediaPlayer.start()
                playPauseButton.setImageResource(R.drawable.pause_button)
            }
            return
        }
        setCurrentSong(songIndex)
        mediaPlayer.start()
        playPauseButton.setImageResource(R.drawable.pause_button)
        lastSong = songIndex
        Log.i("test",songObjects[songIndex].artist)
    }
    private fun initSongData(){
        val songFiles = assets.list("default_songs") ?: arrayOf()

        songFiles.forEachIndexed{index, songName ->
            try {
                val destDir = File(filesDir, "songs/$index")
                if (!destDir.exists()) {
                    destDir.mkdirs()
                    copymp3FromAssets("default_songs/$songName", "songs/$index/$songName")
                }
            }catch (e : Exception){
                Log.e("MainActivity","Failed to initialize songs ${e.message}")
            }
        }
    }


    private fun copymp3FromAssets(assetFileName: String, outputFileName: String) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = assets.open(assetFileName)
            outputStream = FileOutputStream(File(filesDir, outputFileName))
            val buffer = ByteArray(1024)

            while (true) {
                val temp = inputStream.read(buffer)
                if(temp== -1){
                    break
                }
                outputStream.write(buffer, 0, temp)
            }

            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    private fun setCurrentSong(songIndex: Int= 0 ) {
        // preconditions valid index, data structure is correct, any and all exceptions are ignored
        if(::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
        }
        try {
            val path: String = songObjects[songIndex].getMp3FilePath(this,songIndex)
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun initSongView(){
        val songFolder = File(filesDir, "songs")
        val songFileNames = songFolder.list()?: arrayOf()
        songObjects = mutableListOf()
        songFileNames.forEach {
            songObjects.add(SongData(this, it.toInt()))
        }
        recyclerView = findViewById(R.id.songView)

        val songViewAdapter = SongViewAdapter(songObjects,
            clickListener = { songIndex ->
                onClickPlaySong(songIndex)
            },
            longClickListener = { songIndex ->
                onClickSongOptions(songIndex)
                true
            }
        )
        recyclerView.adapter = songViewAdapter
    }
    private fun initBottomBar(){
        setCurrentSong(0)
        playPauseButton = findViewById(R.id.MasterPlayPauseButton)
        playPauseButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.play_button)
            } else {
                mediaPlayer.start()
                playPauseButton.setImageResource(R.drawable.pause_button)
            }
        }
    }
}