package com.tpstream.app

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tpstream.player.TpStreamDownloadManager
import com.tpstream.player.models.OfflineVideoInfo

class DownloadListViewModel(context: Context): ViewModel() {

    private var tpStreamDownloadManager: TpStreamDownloadManager = TpStreamDownloadManager(context)

    fun getDownloadData(): LiveData<List<OfflineVideoInfo>?> {
        return tpStreamDownloadManager.getAllDownloads()
    }

    fun pauseDownload(offlineVideoInfo: OfflineVideoInfo) {
        tpStreamDownloadManager.pauseDownload(offlineVideoInfo)
    }

    fun resumeDownload(offlineVideoInfo: OfflineVideoInfo) {
        tpStreamDownloadManager.resumeDownload(offlineVideoInfo)
    }

    fun cancelDownload(offlineVideoInfo: OfflineVideoInfo) {
        tpStreamDownloadManager.cancelDownload(offlineVideoInfo)
    }

    fun deleteDownload(offlineVideoInfo: OfflineVideoInfo) {
        tpStreamDownloadManager.deleteDownload(offlineVideoInfo)
    }
}