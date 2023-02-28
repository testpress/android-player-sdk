package com.tpstream.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

internal class ImageSaver(val context: Context) {
    private val TAG = "ImageSaver"

    init {
        val folder = File("${context.filesDir}/thumbnail")
        if (!folder.exists()) {
            folder.mkdir()
        }
    }

    fun save(thumbnailUrl: String, name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap: Bitmap? = getBitmap(thumbnailUrl)
            save(bitmap, name)
        }
    }

    private fun getBitmap(thumbnailUrl: String): Bitmap? {
        return try {
            val url = URL(thumbnailUrl)
            val bitmap: Bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            bitmap
        } catch (exception: Exception) {
            null
        }
    }

    private fun save(bitmapImage: Bitmap?, imageFileName: String) {
        if (bitmapImage != null) {
            try {
                val file = File("${context.filesDir}/thumbnail/", "$imageFileName.png")
                if (file.exists()) {
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

    fun load(name: String): Bitmap? {
        return get(name)
    }

    private fun get(imageFileName: String): Bitmap? {
        return try {
            val directory = context.filesDir
            val file = File(directory, "/thumbnail/$imageFileName.png")
            BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: FileNotFoundException) {
            BitmapFactory.decodeResource(context.resources, R.drawable.tp_video_placeholder)
        }
    }

    fun delete(imageFileName: String) {
        val dir = context.filesDir
        val file = File(dir, "/thumbnail/$imageFileName.png")
        file.delete()
    }
}