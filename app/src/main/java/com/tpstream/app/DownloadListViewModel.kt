package com.tpstream.app

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tpstream.player.offline.TpStreamDownloadManager
import com.tpstream.player.data.Asset

class DownloadListViewModel(context: Context): ViewModel() {

    private var tpStreamDownloadManager: TpStreamDownloadManager = TpStreamDownloadManager(context)

    fun getDownloadData(): LiveData<List<Asset>?> {
        return tpStreamDownloadManager.getAllDownloads()
    }

    fun getAssetsByMetadata(metadata: Map<String, String>): LiveData<List<Asset>?> {
        return tpStreamDownloadManager.getAssetsByMetadata(metadata)
    }

    fun getDownloadAsset(assetId: String): LiveData<Asset?> {
        return tpStreamDownloadManager.getDownloadAsset(assetId)
    }

    fun pauseDownload(asset: Asset) {
        tpStreamDownloadManager.pauseDownload(asset)
    }

    fun resumeDownload(asset: Asset) {
        tpStreamDownloadManager.resumeDownload(asset)
    }

    fun cancelDownload(asset: Asset) {
        tpStreamDownloadManager.cancelDownload(asset)
    }

    fun deleteDownload(asset: Asset) {
        tpStreamDownloadManager.deleteDownload(asset)
    }

    fun deleteAllDownload() {
        tpStreamDownloadManager.deleteAllDownloads()
    }
}