package com.tpstream.player.offline

import android.content.Context
import androidx.lifecycle.LiveData
import com.tpstream.player.util.ImageSaver
import com.tpstream.player.data.Video
import com.tpstream.player.data.VideoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

interface OfflineDRMLicenseFetchCallback {
    fun onLicenseFetchSuccess(keySetId: ByteArray) {}
    fun onLicenseFetchFailure() {}
    fun onOfflineLicenseExpire(videoID: String): String
}