package com.tpstream.player.offline

import android.content.Context
import androidx.lifecycle.LiveData
import com.tpstream.player.util.ImageSaver
import com.tpstream.player.data.Asset
import com.tpstream.player.data.VideoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TpStreamDownloadManager(val context: Context) {

    private val videoRepository = VideoRepository(context)

    fun getAllDownloads(): LiveData<List<Asset>?> {
        return videoRepository.getAllDownloadsInLiveData()
    }

    fun pauseDownload(asset: Asset) {
        DownloadTask(context).pause(asset)
    }

    fun resumeDownload(asset: Asset) {
        DownloadTask(context).resume(asset)
    }

    fun cancelDownload(asset: Asset) {
        deleteDownload(asset)
    }

    fun deleteDownload(asset: Asset) {
        CoroutineScope(Dispatchers.IO).launch {
            DownloadTask(context).delete(asset)
            ImageSaver(context).delete(asset.id)
            videoRepository.delete(asset)
        }
    }

    fun deleteAllDownloads() {
        CoroutineScope(Dispatchers.IO).launch {
            videoRepository.deleteAll()
            DownloadTask(context).deleteAll()
            ImageSaver(context).deleteAll()
        }
    }
}