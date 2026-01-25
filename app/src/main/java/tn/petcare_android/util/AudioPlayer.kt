package tn.petcare_android.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Audio Player for Voice Messages
 * iOS Reference: AudioRecorder.swift (playback functionality)
 * 
 * Plays audio messages using MediaPlayer
 * Supports both local files and remote URLs
 */
class AudioPlayer(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioPlayer"
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioId: String? = null
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition
    
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration
    
    /**
     * Play audio from file
     * @param file Audio file to play
     * @param audioId Unique identifier for this audio (e.g., message ID)
     */
    fun playFromFile(file: File, audioId: String) {
        try {
            // Stop current playback if different audio
            if (currentAudioId != audioId) {
                stop()
            }
            
            // If same audio, toggle play/pause
            if (currentAudioId == audioId && mediaPlayer != null) {
                if (_isPlaying.value) {
                    pause()
                } else {
                    resume()
                }
                return
            }
            
            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    Log.d(TAG, "▶️ Playback completed")
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    true
                }
                
                start()
            }
            
            currentAudioId = audioId
            _isPlaying.value = true
            _duration.value = mediaPlayer?.duration ?: 0
            
            Log.d(TAG, "▶️ Playing audio: $audioId from file")
            
            // Update position periodically
            updatePosition()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio: ${e.message}")
            _isPlaying.value = false
        }
    }
    
    /**
     * Play audio from URL
     * @param url Audio URL
     * @param audioId Unique identifier for this audio
     */
    fun playFromUrl(url: String, audioId: String) {
        try {
            // Stop current playback if different audio
            if (currentAudioId != audioId) {
                stop()
            }
            
            // If same audio, toggle play/pause
            if (currentAudioId == audioId && mediaPlayer != null) {
                if (_isPlaying.value) {
                    pause()
                } else {
                    resume()
                }
                return
            }
            
            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(url))
                prepareAsync() // Use async for network streams
                
                setOnPreparedListener {
                    it.start()
                    _isPlaying.value = true
                    _duration.value = it.duration
                    updatePosition()
                }
                
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    Log.d(TAG, "▶️ Playback completed")
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    true
                }
            }
            
            currentAudioId = audioId
            Log.d(TAG, "▶️ Loading audio: $audioId from URL")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio from URL: ${e.message}")
            _isPlaying.value = false
        }
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
            Log.d(TAG, "⏸️ Playback paused")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause: ${e.message}")
        }
    }
    
    /**
     * Resume playback
     */
    fun resume() {
        try {
            mediaPlayer?.start()
            _isPlaying.value = true
            updatePosition()
            Log.d(TAG, "▶️ Playback resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume: ${e.message}")
        }
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        try {
            mediaPlayer?.apply {
                stop()
                reset()
                release()
            }
            mediaPlayer = null
            currentAudioId = null
            _isPlaying.value = false
            _currentPosition.value = 0
            _duration.value = 0
            Log.d(TAG, "⏹️ Playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop: ${e.message}")
        }
    }
    
    /**
     * Seek to position
     * @param position Position in milliseconds
     */
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
            _currentPosition.value = position
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek: ${e.message}")
        }
    }
    
    /**
     * Get current playback position
     */
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Get duration
     */
    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Update position periodically
     */
    private fun updatePosition() {
        if (_isPlaying.value) {
            _currentPosition.value = getCurrentPosition()
            
            // Schedule next update (every 100ms)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (_isPlaying.value) {
                    updatePosition()
                }
            }, 100)
        }
    }
    
    /**
     * Check if currently playing specific audio
     */
    fun isPlayingAudio(audioId: String): Boolean {
        return currentAudioId == audioId && _isPlaying.value
    }
    
    /**
     * Release resources
     * Call this when done with the player
     */
    fun release() {
        stop()
        Log.d(TAG, "🔊 AudioPlayer released")
    }
}

