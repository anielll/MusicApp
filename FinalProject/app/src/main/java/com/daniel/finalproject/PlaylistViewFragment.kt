package com.daniel.finalproject

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.daniel.finalproject.SongData.Companion.getMp3FilePath
import java.io.File
import java.io.IOException
import com.daniel.finalproject.PlaylistData.Companion.writePlaylistDataToFile
class PlaylistViewFragment : Fragment()
{
    interface OnSongUpdatedListener {
        fun onSongUpdated(newSong:SongData?, index: Int?= null)
    }
    interface OnPlaylistUpdatedListener {
        fun onPlaylistUpdated(newPlaylist: PlaylistData)
    }
    companion object{
        fun deleteFolder(folder: File):Boolean{
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
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var filteredSongList : MutableList<SongData>
    private lateinit var currentPlaylist: PlaylistData
    private var lastSong:Int? = null


    fun updateSong(newSong: SongData?, libraryIndex: Int?) {
        if(newSong==null){ // deletion
            val playlistIndex = currentPlaylist.songList.indexOf(libraryIndex)
            filteredSongList.removeAt(playlistIndex)
            currentPlaylist.songList.remove(libraryIndex)
            recyclerView.adapter?.notifyItemRemoved(playlistIndex)
            writePlaylistDataToFile(requireContext(),currentPlaylist)
            deleteFolder(File(requireContext().filesDir,"songs/$libraryIndex"))
            val listener = requireActivity() as OnPlaylistUpdatedListener
            listener.onPlaylistUpdated(currentPlaylist)
            return
        }
        if(libraryIndex==null){ // replacement
            val playlistIndex = currentPlaylist.songList.indexOf(newSong.songIndex)
            filteredSongList[playlistIndex] = newSong
            recyclerView.adapter?.notifyItemChanged(playlistIndex)
            return
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        // else is deprecated in favor of if in SDK 33
        @Suppress("DEPRECATION")
        currentPlaylist= if(Build.VERSION.SDK_INT >= 33){
            arguments?.getSerializable("selected_playlist", PlaylistData::class.java)!!
        }else{
            arguments?.getSerializable("selected_playlist") as PlaylistData
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.playlist_view_fragment, container, false)
        setPlaylistData()
        setCurrentSong(0)
        initSongView(view)
        initBottomBar(view)
        val backButton: ImageButton = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        val playlistName: TextView = view.findViewById(R.id.playlistName)
        playlistName.text = currentPlaylist.playlistName

        return view
    }
//
    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
    private fun onClickSongOptions(playlistIndex: Int) {
        val libraryIndex :Int = currentPlaylist.songList[playlistIndex]
        val songOptionsFragment = SongOptionsDialogFragment.newInstance(libraryIndex)
        songOptionsFragment.show(parentFragmentManager, "SongOptions")
    }
    private fun onClickPlaySong(playlistIndex: Int){
        if(playlistIndex==lastSong){
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playPauseButton.setImageResource(R.drawable.play_button)
            } else {
                mediaPlayer.start()
                playPauseButton.setImageResource(R.drawable.pause_button)
            }
            return
        }
        setCurrentSong(playlistIndex)
        mediaPlayer.start()
        playPauseButton.setImageResource(R.drawable.pause_button)
        lastSong = playlistIndex
    }

    private fun setPlaylistData(){
        var wasFiltered = false
        val filteredIndexList = currentPlaylist.songList
            .filter {
                val exists = File(requireContext().filesDir, "songs/$it").exists()
                if(!exists) wasFiltered = true
                exists
            }.toMutableList()
        filteredSongList =  filteredIndexList
            .map{SongData(requireContext(),it) }
            .toMutableList()
        if(wasFiltered){
            currentPlaylist = PlaylistData(requireContext(),currentPlaylist.playlistName,filteredIndexList,currentPlaylist.playlistIndex)
            println("PlaylistUpdated")
            val listener = requireActivity() as OnPlaylistUpdatedListener
            listener.onPlaylistUpdated(currentPlaylist)
        }
    }
    private fun setCurrentSong(playlistIndex: Int= 0 ) {
        // preconditions valid index, data structure is correct, any and all exceptions are ignored
        if(::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
        }
        try {
            val libraryIndex:Int = currentPlaylist.songList[playlistIndex]
            val path = getMp3FilePath(requireContext(), libraryIndex)
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun initSongView(view: View){
        recyclerView = view.findViewById(R.id.songView)
        val songViewAdapter = SongViewAdapter(
            filteredSongList,
            clickListener = { playlistIndex ->
                onClickPlaySong(playlistIndex)
            },
            longClickListener = { playlistIndex ->
                onClickSongOptions(playlistIndex)
                true
            }
        )
        recyclerView.adapter = songViewAdapter
    }
    private fun initBottomBar(view: View){
        playPauseButton = view.findViewById(R.id.MasterPlayPauseButton)
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