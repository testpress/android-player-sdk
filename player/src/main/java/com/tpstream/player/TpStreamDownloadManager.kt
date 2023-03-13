package com.tpstream.player

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.tpstream.player.models.DomainVideo
import com.tpstream.player.models.asDomainVideos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TpStreamDownloadManager(val context: Context) {

    private val videoRepository = VideoRepository(context)

    fun getAllDownloads(): LiveData<List<DomainVideo>?> {
        return Transformations.map(videoRepository.getAllDownloadsInLiveData()) {
            it?.asDomainVideos()
        }
    }

    fun pauseDownload(video: DomainVideo) {
        DownloadTask(context).pause(video)
    }

    fun resumeDownload(video: DomainVideo) {
        DownloadTask(context).resume(video)
    }

    fun cancelDownload(video: DomainVideo) {
        deleteDownload(video)
    }

    fun deleteDownload(video: DomainVideo) {
        CoroutineScope(Dispatchers.IO).launch {
            DownloadTask(context).delete(video)
            ImageSaver(context).delete(video.videoId)
            videoRepository.delete(video.asDatabaseVideo())
        }
    }
}