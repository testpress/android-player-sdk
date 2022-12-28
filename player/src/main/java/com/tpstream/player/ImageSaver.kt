package com.tpstream.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.tpstream.player.models.OfflineVideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

class ImageSaver(val context: Context, val offlineVideoInfo: OfflineVideoInfo) {
    private val TAG = "ImageSaver"

    init {
        val folder = File("${context.filesDir}/thumbnail")
        if (!folder.exists()) {
            folder.mkdir()
        }
    }

    fun saveImage() {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap: Bitmap? = urlToBitmap(offlineVideoInfo.thumbnail)
            save(context, bitmap, offlineVideoInfo.videoId)
        }
    }

    fun loadImage(): Bitmap? {
        return getImage(context, offlineVideoInfo.videoId)
    }

    fun deleteImage() {
        deleteImage(context, offlineVideoInfo.videoId)
    }

    private fun urlToBitmap(thumbnail: String): Bitmap? {
        return try {
            val url = URL(thumbnail)
            val bitmap: Bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            bitmap
        } catch (exception: Exception) {
            null
        }
    }

    private fun save(
        context: Context, bitmapImage: Bitmap?, imageFileName: String
    ) {
        if (bitmapImage != null){
            try {
                val file = File("${context.filesDir}/thumbnail/", "$imageFileName.png")
                if (file.exists()){
                    file.delete()
                }
                val fileOutputStream = FileOutputStream(file)
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 50, fileOutputStream)
                fileOutputStream.close()
            } catch (e: Exception) {
                Log.d(TAG, "saveToInternalStorage: Image Not saved")
            }
        }
    }

    private fun getImage(context: Context, imageFileName: String): Bitmap? {
        return try {
            val directory = context.filesDir
            val file = File(directory, "/thumbnail/$imageFileName.png")
            BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: FileNotFoundException){
            BitmapFactory.decodeResource(context.resources,R.drawable.video_placeholder)
        }
    }

    private fun deleteImage(context: Context, imageFileName: String): Boolean {
        val dir = context.filesDir
        val file = File(dir, "/thumbnail/$imageFileName.png")
        return file.delete()
    }
}