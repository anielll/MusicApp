package com.daniel.finalproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.daniel.finalproject.PlaylistData.Companion.readPlaylistDataFromFile
import com.daniel.finalproject.PlaylistViewFragment.OnSongUpdatedListener
import com.daniel.finalproject.PlaylistViewFragment.OnPlaylistUpdatedListener
import com.daniel.finalproject.OnStartAssetManager.Companion.initializeDefaults
class MainActivity : AppCompatActivity(),
        OnSongUpdatedListener,
        OnPlaylistUpdatedListener
{
    private lateinit var playlistObjects : MutableList<PlaylistData>
    private lateinit var recyclerView : RecyclerView

    override fun onSongUpdated(newSong: SongData?, libraryIndex: Int?) {
        val fragment = supportFragmentManager.findFragmentById(R.id.playlist_view_container) as? PlaylistViewFragment
        fragment?.updateSong(newSong, libraryIndex)
    }

    override fun onPlaylistUpdated(newPlaylist: PlaylistData?, fileIndex: Int?) {
        if(newPlaylist==null){ // deletion
            val recyclerIndex = MasterList.recyclerIndexOf(fileIndex!!)
            playlistObjects.removeAt(recyclerIndex-1)
            MasterList.remove(fileIndex)
            MasterList.save(this)
            recyclerView.adapter?.notifyItemRemoved(recyclerIndex)
            return
        }
        if(fileIndex==null){ // replacement
            val recyclerIndex = MasterList.recyclerIndexOf(newPlaylist.fileIndex)
            playlistObjects[recyclerIndex-1] = newPlaylist
            recyclerView.adapter?.notifyItemChanged(recyclerIndex)
        }else{ // add
            playlistObjects.add(newPlaylist)
            MasterList.add(fileIndex)
            MasterList.save(this)
            recyclerView.adapter?.notifyItemInserted(playlistObjects.size)
        }
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
        initializeDefaults(this)
        initPlaylistView()
    }
    private fun initPlaylistView(){
            val  playlistFileIndexes = MasterList.get()
            playlistObjects = playlistFileIndexes.mapNotNull {
                if(it>=0){ // filter out library
                    readPlaylistDataFromFile(this,it)
                }else{
                    null
                }
            }.toMutableList()
                recyclerView = findViewById(R.id.playlistView)
            val mainViewAdapter = MainViewAdapter(
                playlistObjects,
                clickListener = { songIndex ->
                    onClickOpenPlaylist(songIndex)
                },
                optionsClickListener = { songIndex ->
                    onClickPlaylistOptions(songIndex)
                },
                supportFragmentManager
                )
            recyclerView.adapter = mainViewAdapter
    }
    private fun onClickOpenPlaylist(recyclerIndex: Int){
        val fragment = PlaylistViewFragment().apply {
            arguments = Bundle().apply {
                putInt("selected_playlist", MasterList.fileIndexOf(recyclerIndex))
            }
        }
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in,R.anim.fade_out,R.anim.fade_in,R.anim.fade_out  )
            .replace(R.id.playlist_view_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun onClickPlaylistOptions(recyclerIndex: Int){
        val playlistOptionsFragment = PlaylistOptionsFragment.newInstance(MasterList.fileIndexOf(recyclerIndex))
        playlistOptionsFragment.show(supportFragmentManager, "SongOptions")
    }
}