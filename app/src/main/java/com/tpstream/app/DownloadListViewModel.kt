package com.tpstream.app

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tpstream.player.TpStreamDownloadManager
import com.tpstream.player.models.Video

class DownloadListViewModel(context: Context): ViewModel() {

    private var tpStreamDownloadManager: TpStreamDownloadManager = TpStreamDownloadManager(context)

    fun getDownloadData(): LiveData<List<Video>?> {
        return tpStreamDownloadManager.getAllDownloads()
    }

    fun pauseDownload(video: Video) {
        tpStreamDownloadManager.pauseDownload(video)
    }

    fun resumeDownload(video: Video) {
        tpStreamDownloadManager.resumeDownload(video)
    }

    fun cancelDownload(video: Video) {
        tpStreamDownloadManager.cancelDownload(video)
    }

    fun deleteDownload(video: Video) {
        tpStreamDownloadManager.deleteDownload(video)
    }
}