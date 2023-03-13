package com.tpstream.app

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tpstream.player.TpStreamDownloadManager
import com.tpstream.player.models.DomainVideo

class DownloadListViewModel(context: Context): ViewModel() {

    private var tpStreamDownloadManager: TpStreamDownloadManager = TpStreamDownloadManager(context)

    fun getDownloadData(): LiveData<List<DomainVideo>?> {
        return tpStreamDownloadManager.getAllDownloads()
    }

    fun pauseDownload(video: DomainVideo) {
        tpStreamDownloadManager.pauseDownload(video)
    }

    fun resumeDownload(video: DomainVideo) {
        tpStreamDownloadManager.resumeDownload(video)
    }

    fun cancelDownload(video: DomainVideo) {
        tpStreamDownloadManager.cancelDownload(video)
    }

    fun deleteDownload(video: DomainVideo) {
        tpStreamDownloadManager.deleteDownload(video)
    }
}