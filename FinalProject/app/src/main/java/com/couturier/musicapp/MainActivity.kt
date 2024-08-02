package com.couturier.musicapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.couturier.musicapp.PlaylistData.Companion.readPlaylistDataFromFile
import com.couturier.musicapp.PlaylistViewFragment.OnSongUpdatedListener
import com.couturier.musicapp.PlaylistViewFragment.OnPlaylistUpdatedListener
import com.couturier.musicapp.OnStartAssetManager.Companion.initializeDefaults

class MainActivity :
    AppCompatActivity(),
    OnSongUpdatedListener,
    OnPlaylistUpdatedListener {
    private lateinit var playlistObjects: MutableList<PlaylistData>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowProperties() // Makes system UI (at top) look nice
        initializeDefaults(this) // Ensure core app data is initialized properly
        initializeView()
    }

    private fun setWindowProperties() { // Makes system UI (at top) look nice
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    private fun initializeView() {
        playlistObjects = MasterList.get() // Shared pointer, should only be initialized once
            .mapNotNull {
                readPlaylistDataFromFile(this, it)
            }.toMutableList()
        recyclerView = findViewById<RecyclerView>(R.id.playlistView).apply {
            adapter = MainViewAdapter(
                playlistObjects,
                clickListener = { songIndex -> onClickOpenPlaylist(songIndex) },
                optionsClickListener = { songIndex -> onClickPlaylistOptions(songIndex) },
                supportFragmentManager
            )
        }
    }

    private fun onClickOpenPlaylist(recyclerIndex: Int) {
        val fragment = PlaylistViewFragment().apply {
            arguments = Bundle().apply {
                putInt("selected_playlist", MasterList.fileIndexOf(recyclerIndex))
            }
        }
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            replace(R.id.playlist_view_container, fragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun onClickPlaylistOptions(recyclerIndex: Int) {
        PlaylistOptionsFragment.newInstance(MasterList.fileIndexOf(recyclerIndex))
            .show(supportFragmentManager, "SongOptions")
    }


    override fun onSongUpdate(newSong: SongData?, libraryIndex: Int?) {
        (supportFragmentManager.findFragmentById(R.id.playlist_view_container) as PlaylistViewFragment)
            .updateSong(newSong, libraryIndex)
    }

    override fun onPlaylistUpdate(newPlaylist: PlaylistData?, fileIndex: Int?) {
        when { // Switch is easier than three different update interfaces
            newPlaylist != null && fileIndex != null -> playlistAddUpdate(newPlaylist)
            newPlaylist != null -> playlistReplaceUpdate(newPlaylist)
            fileIndex != null -> playlistDeleteUpdate(fileIndex)
            else -> throw Exception("Improper Use: MainActivity.onPlaylistUpdate")
        }
    }

    private fun playlistAddUpdate(newPlaylist: PlaylistData) {
        playlistObjects.add(newPlaylist)
        MasterList.add(newPlaylist.fileIndex)
        MasterList.save(this)
        recyclerView.adapter!!.notifyItemInserted(playlistObjects.size)
    }

    private fun playlistReplaceUpdate(newPlaylist: PlaylistData) {
        val recyclerIndex = MasterList.recyclerIndexOf(newPlaylist.fileIndex)
        playlistObjects[recyclerIndex - 1] = newPlaylist
        recyclerView.adapter!!.notifyItemChanged(recyclerIndex)
    }

    private fun playlistDeleteUpdate(fileIndex: Int) {
        val recyclerIndex = MasterList.recyclerIndexOf(fileIndex)
        playlistObjects.removeAt(recyclerIndex - 1)
        MasterList.remove(fileIndex)
        MasterList.save(this)
        recyclerView.adapter!!.notifyItemRemoved(recyclerIndex)
    }

}