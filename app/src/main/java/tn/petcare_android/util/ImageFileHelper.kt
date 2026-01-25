package tn.petcare_android.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Utility for handling image files
 * Backend handles the actual Cloudinary upload
 */
object ImageFileHelper {

    private const val TAG = "ImageFileHelper"

    /**
     * Convert URI to File for upload
     * The actual upload to Cloudinary is handled by the backend
     * @param context Android context
     * @param imageUri URI of the image
     * @return File ready for upload, or null if conversion fails
     */
    fun uriToFile(context: Context, imageUri: Uri): File? {
        return try {
            Log.d(TAG, "Converting URI to File: $imageUri")

            val contentResolver = context.contentResolver
            val tempFile = File(context.cacheDir, "pet_photo_${System.currentTimeMillis()}.jpg")

            contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "File created: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert URI to file", e)
            null
        }
    }
}

