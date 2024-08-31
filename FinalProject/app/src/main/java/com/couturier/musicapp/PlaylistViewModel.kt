package com.couturier.musicapp

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.couturier.musicapp.MainActivity.Companion.appContext
import com.couturier.musicapp.SongData.Companion.getMp3FilePath
import java.util.Locale

class PlaylistViewModel : ViewModel() {
    private val _isPlaying = MutableLiveData<Boolean>()
    private val _currentSong = MutableLiveData<Int?>()
    private val _songUpdate = MutableLiveData<Int?>()
    private val _songProgress = MutableLiveData<Int>()
    private val _songTime = MutableLiveData<String>()
    private val _songDuration = MutableLiveData<String>()
    private val _seekBarEnabled = MutableLiveData<Boolean>()

    val isPlaying: LiveData<Boolean> get() = _isPlaying
    val currentSong: LiveData<Int?> get() = _currentSong
    val songUpdate: LiveData<Int?> get() = _songUpdate
    val songProgress: LiveData<Int> get() = _songProgress
    val songTime: LiveData<String> get() = _songTime
    val songDuration: LiveData<String> get() = _songDuration
    val seekBarEnabled: LiveData<Boolean> get() = _seekBarEnabled

    lateinit var songQueue: SongQueue
    lateinit var updateType: String

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var updateProgressBarRunnable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    fun set(playlistIndex: Int) {
        if (::updateProgressBarRunnable.isInitialized) {
            handler.removeCallbacks(updateProgressBarRunnable)
        }
        if (::mediaPlayer.isInitialized) {
            if(mediaPlayer.isPlaying){
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
        songQueue = SongQueue(playlistIndex)
        _isPlaying.value = false
        _currentSong.value = null
        _seekBarEnabled.value = true
        setCurrentSong(-1)

    }

    fun setSeekbarListeners(seekBar: SeekBar) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var wasPlaying: Boolean = false
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && ::mediaPlayer.isInitialized) {
                    val seekPosition = (progress / 86400.0 * mediaPlayer.duration).toInt()
                    mediaPlayer.seekTo(seekPosition)
                    _songTime.value = formatTime(seekPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (!::mediaPlayer.isInitialized) {
                    return
                }
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    wasPlaying = true
                } else {
                    wasPlaying = false
                }
                handler.removeCallbacks(updateProgressBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (::mediaPlayer.isInitialized && wasPlaying) {
                    mediaPlayer.start()
                }
                updateProgressBar()
            }
        })
    }

    fun setCurrentSong(playlistIndex: Int?) {
        if (songQueue.size() == 0) { // if initializing empty playlist... don't
            return
        }
        if (playlistIndex == null) { // uninitializes playlist
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    _isPlaying.value = false
                }
                mediaPlayer.reset()
            }
            if (::updateProgressBarRunnable.isInitialized) {
                handler.removeCallbacks(updateProgressBarRunnable)
            }
            _currentSong.value = null
            _songTime.value = appContext.getString(R.string.zeroed_time)
            _songDuration.value = appContext.getString(R.string.zeroed_time)
            _seekBarEnabled.value = false
            return
        }
        if (playlistIndex == -1) {
            _currentSong.value = -1
            songQueue.setQueueCursor(-1)
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    _isPlaying.value = false
                }
                mediaPlayer.reset()
            }
            if (::updateProgressBarRunnable.isInitialized) {
                handler.removeCallbacks(updateProgressBarRunnable)
            }
            _songTime.value = appContext.getString(R.string.zeroed_time)
            _songDuration.value = appContext.getString(R.string.zeroed_time)
            _seekBarEnabled.value = false
            return
        }
        if (playlistIndex == currentSong.value) {
            return
        }
        _currentSong.value = playlistIndex
        songQueue.setQueueCursor(playlistIndex)
        _seekBarEnabled.value = true

        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                _isPlaying.value = false
            }
            mediaPlayer.reset()
        }
        val path = getMp3FilePath(songQueue.libraryIndexOf(playlistIndex))
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(path)
        mediaPlayer.prepare()
        mediaPlayer.setOnCompletionListener {
            playNext()
        }
        if (::updateProgressBarRunnable.isInitialized) {
            handler.removeCallbacks(updateProgressBarRunnable)
        }
        _songTime.value = appContext.getString(R.string.zeroed_time)
        _songDuration.value = formatTime(mediaPlayer.duration)
        updateProgressBar()
        return
    }

    fun addSong(newSong: SongData) {
        songQueue.add(newSong.songIndex, newSong)
        updateType = "ADD"
        _songUpdate.value = songQueue.size() - 1
        _songUpdate.value = null
        if (songQueue.size() == 1) { // if first time, initialize
            setCurrentSong(-1)
        }
    }

    fun replaceSong(newSong: SongData) {
        songQueue.replace(newSong)
        updateType = "REPLACE"
        _songUpdate.value = songQueue.playlistIndexOf(newSong.songIndex)
        _songUpdate.value = null
    }

    fun deleteSong(libraryIndex: Int) {
        val playlistIndex: Int = songQueue.playlistIndexOf(libraryIndex) // get before deleting
        if (playlistIndex == currentSong.value) { // catch  edge cases
            if (songQueue.size() == 0) {
                setCurrentSong(null)
            } else {
                val nextSong = songQueue.next()
                if (nextSong == null) {
                    setCurrentSong(-1)
                } else {
                    if (mediaPlayer.isPlaying) {
                        setCurrentSong(nextSong)
                        playCurrent()
                    } else {
                        setCurrentSong(nextSong)
                    }
                    _currentSong.value =
                        if (nextSong > playlistIndex) { // override onClickPlaySong setting currentSong
                            nextSong - 1 // if next song had higher playlistIndex, it will be shifted down one playlist index
                        } else {
                            nextSong
                        }
                    songQueue.delete(libraryIndex)
                    updateType = "DELETE"
                    _songUpdate.value = playlistIndex
                    _songUpdate.value = null
                    songQueue.setQueueCursor(_currentSong.value!!)
                    return
                }
            }
        }
        songQueue.delete(libraryIndex)
        updateType = "DELETE"
        _songUpdate.value = playlistIndex
        _songUpdate.value = null
    }

    fun playCurrent() {
        if (currentSong.value == -1) {
            playNext()
            return
        }
        if (songQueue.size() != 0) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                _isPlaying.value = false
                handler.removeCallbacks(updateProgressBarRunnable)
            } else {
                mediaPlayer.start()
                _isPlaying.value = true
                updateProgressBar()
            }
        }
    }

    fun playNext() {
        if (songQueue.size() != 0) {
            val nextSong = songQueue.next()
            if (nextSong == null) {
                setCurrentSong(-1)
            } else {
                setCurrentSong(nextSong)
                playCurrent()
            }
        }
    }

    fun playPrev() {
        if (songQueue.size() != 0) {
            val prevSong = songQueue.prev()
            if (prevSong == null) {
                setCurrentSong(-1)
            } else {
                setCurrentSong(prevSong)
                playCurrent()
            }
        }
    }

    private fun updateProgressBar() {
        updateProgressBarRunnable = object : Runnable {
            override fun run() {
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    _songProgress.value =
                        ((mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration) * 86400).toInt()
                    _songTime.value = formatTime((mediaPlayer.currentPosition.toFloat()).toInt())
                    handler.postDelayed(this, mediaPlayer.duration.toLong() / 86400)
                }
            }
        }
        handler.post(updateProgressBarRunnable)
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = (milliseconds / 1000)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds)
    }

}
