package tn.petcare_android.util

/**
 * Utility extension functions and helpers
 */

/**
 * Format duration in milliseconds to MM:SS format
 * Used for audio playback and calendar events
 */
fun formatDuration(durationMs: Int): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
