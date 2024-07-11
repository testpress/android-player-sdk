package com.tpstream.player.offline

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import com.tpstream.player.*
import com.tpstream.player.EncryptionKeyRepository
import com.tpstream.player.constants.getErrorMessageForDownload
import com.tpstream.player.util.ImageSaver
import com.tpstream.player.data.Asset
import com.tpstream.player.data.AssetRepository
import com.tpstream.player.ui.DownloadResolutionSelectionSheet
import com.tpstream.player.util.NetworkClient
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

    fun startDownload(fragmentActivity: FragmentActivity, params: TpInitParams) {
        showDownloadSelectionSheet(fragmentActivity, params, null)
    }

    fun startDownload(fragmentActivity: FragmentActivity, player: TpStreamPlayer) {
        val playerImpl = player as TpStreamPlayerImpl
        showDownloadSelectionSheet(fragmentActivity, playerImpl.params, playerImpl.asset)
    }

    private fun showDownloadSelectionSheet(
        fragmentActivity: FragmentActivity,
        params: TpInitParams,
        asset: Asset?
    ) {
        val downloadResolutionSelectionSheet = DownloadResolutionSelectionSheet()
        downloadResolutionSelectionSheet.show(
            fragmentActivity.supportFragmentManager,
            "DownloadSelectionSheet"
        )

        val assetToUse = asset ?: run {
            // Fetch asset if not provided
            var fetchedAsset: Asset? = null
            assetRepository.getAsset(params, object : NetworkClient.TPResponse<Asset> {
                override fun onSuccess(result: Asset) {
                    fetchedAsset = result
                    onFetchAssetSuccess(result, params, downloadResolutionSelectionSheet)
                }

                override fun onFailure(exception: TPException) {
                    onFetchAssetFailure(exception, downloadResolutionSelectionSheet)
                }
            })
            fetchedAsset
        }

        assetToUse?.let {
            onFetchAssetSuccess(it, params, downloadResolutionSelectionSheet)
        }
    }

    private fun onFetchAssetSuccess(
        asset: Asset,
        params: TpInitParams,
        downloadResolutionSelectionSheet: DownloadResolutionSelectionSheet
    ) {
        EncryptionKeyRepository(context).fetchAndStore(
            params,
            asset.video.url
        )
        downloadResolutionSelectionSheet.initializeVideoDownloadRequestCreateHandler(context, asset, params)
        downloadResolutionSelectionSheet.setOnSubmitListener { downloadRequest, asset ->
            DownloadTask(context).start(downloadRequest)
            asset?.id = params.videoId!!
            asset?.downloadStartTimeMs = System.currentTimeMillis()
            ImageSaver(context).save(
                asset?.thumbnail!!,
                asset.id
            )
            CoroutineScope(Dispatchers.IO).launch {
                assetRepository.insert(asset)
            }
        }
    }

    private fun onFetchAssetFailure(
        exception: TPException,
        downloadResolutionSelectionSheet: DownloadResolutionSelectionSheet
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            downloadResolutionSelectionSheet.dismiss()
            Toast.makeText(
                context,
                exception.getErrorMessageForDownload(),
                Toast.LENGTH_SHORT
            ).show()
        }
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