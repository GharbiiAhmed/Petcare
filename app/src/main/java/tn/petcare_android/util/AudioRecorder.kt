package tn.petcare_android.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Audio Recorder for Voice Messages
 * iOS Reference: AudioRecorder.swift (lines 1-165)
 * 
 * Records audio messages using MediaRecorder
 * Saves to app cache directory in AAC format
 */
class AudioRecorder(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioRecorder"
        private const val AUDIO_FORMAT = ".m4a" // AAC audio format
    }
    
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    
    /**
     * Start recording audio
     * @return File where audio is being recorded, or null if failed
     */
    fun startRecording(): File? {
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return audioFile
        }
        
        try {
            // Create audio file
            audioFile = createAudioFile()
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile?.absolutePath)
                
                prepare()
                start()
            }
            
            isRecording = true
            Log.d(TAG, "🎤 Recording started: ${audioFile?.name}")
            return audioFile
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            cleanup()
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording: ${e.message}")
            cleanup()
            return null
        }
    }
    
    /**
     * Stop recording audio
     * @return File containing the recorded audio, or null if failed
     */
    fun stopRecording(): File? {
        if (!isRecording) {
            Log.w(TAG, "Not currently recording")
            return null
        }
        
        return try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            val file = audioFile
            Log.d(TAG, "🎤 Recording stopped: ${file?.name} (${file?.length() ?: 0} bytes)")
            file
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording: ${e.message}")
            cleanup()
            null
        }
    }
    
    /**
     * Cancel recording and delete file
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            // Delete the audio file
            audioFile?.delete()
            audioFile = null
            
            Log.d(TAG, "🎤 Recording cancelled")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel recording: ${e.message}")
            cleanup()
        }
    }
    
    /**
     * Get current recording duration in seconds
     * Note: MediaRecorder doesn't provide real-time duration
     * Track manually using a timer if needed
     */
    fun getRecordingDuration(): Long {
        return if (isRecording && audioFile != null) {
            // Estimate based on file size (approximate)
            val fileSize = audioFile?.length() ?: 0
            // AAC at 128kbps ≈ 16KB per second
            (fileSize / 16000)
        } else {
            0
        }
    }
    
    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * Create audio file in cache directory
     */
    private fun createAudioFile(): File {
        val timestamp = System.currentTimeMillis()
        val fileName = "audio_$timestamp$AUDIO_FORMAT"
        val cacheDir = context.cacheDir
        return File(cacheDir, fileName)
    }
    
    /**
     * Cleanup resources
     */
    private fun cleanup() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaRecorder = null
        isRecording = false
        audioFile = null
    }
    
    /**
     * Release resources
     * Call this when done with the recorder
     */
    fun release() {
        cleanup()
        Log.d(TAG, "🎤 AudioRecorder released")
    }
}

/**
 * Audio recording state
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    COMPLETED,
    ERROR
}

