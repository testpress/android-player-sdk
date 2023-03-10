package com.tpstream.player

import android.content.Context
import androidx.lifecycle.LiveData
import com.tpstream.player.models.Video
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TpStreamDownloadManager(val context: Context) {

    private val videoRepository = VideoRepository(context)

    fun getAllDownloads(): LiveData<List<Video>?> {
        return videoRepository.getAllDownloadsInLiveData()
    }

    fun pauseDownload(video: Video) {
        DownloadTask(context).pause(video)
    }

    fun resumeDownload(video: Video) {
        DownloadTask(context).resume(video)
    }

    fun cancelDownload(video: Video) {
        deleteDownload(video)
    }

    fun deleteDownload(video: Video) {
        CoroutineScope(Dispatchers.IO).launch {
            DownloadTask(context).delete(video)
            ImageSaver(context).delete(video.videoId)
            videoRepository.delete(video)
        }
    }
}