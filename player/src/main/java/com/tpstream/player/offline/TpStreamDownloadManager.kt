package com.tpstream.player.offline

import android.content.Context
import androidx.lifecycle.LiveData
import com.tpstream.player.util.ImageSaver
import com.tpstream.player.data.Asset
import com.tpstream.player.data.AssetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TpStreamDownloadManager(val context: Context) {

    private val assetRepository = AssetRepository(context)

    fun getAllDownloads(): LiveData<List<Asset>?> {
        return assetRepository.getAllDownloadsInLiveData()
    }

    fun getAssetsByMetadata(metadata: Map<String, String>): LiveData<List<Asset>?> {
        return assetRepository.getAssetsByMetadata(metadata)
    }

    fun getDownloadAsset(assetId: String): LiveData<Asset?> {
        return assetRepository.getAssetInLiveData(assetId)
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
            assetRepository.delete(asset)
        }
    }

    fun deleteAllDownloads() {
        CoroutineScope(Dispatchers.IO).launch {
            assetRepository.deleteAll()
            DownloadTask(context).deleteAll()
            ImageSaver(context).deleteAll()
        }
    }
}