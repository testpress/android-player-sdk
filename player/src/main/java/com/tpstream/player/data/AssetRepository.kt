package com.tpstream.player.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.tpstream.player.TPException
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import com.tpstream.player.data.source.local.TPStreamsDatabase
import com.tpstream.player.data.source.network.TPStreamsNetworkAsset
import com.tpstream.player.data.source.network.TestpressNetworkAsset
import com.tpstream.player.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

internal class AssetRepository(val context: Context) {

    private val assetDao = TPStreamsDatabase(context).assetDao()

    suspend fun update(asset: Asset){
        assetDao.update(asset.asLocalAsset())
    }

    fun get(videoId: String): LiveData<Asset?> {
        return Transformations.map(assetDao.getAssetById(videoId)) {
            it?.asDomainAsset()
        }
    }

    fun getAssetByUrl(url:String):Asset? {
        return assetDao.getAssetByUrl(url)?.asDomainAsset()
    }

    suspend fun insert(asset: Asset){
        assetDao.insert(asset.asLocalAsset())
    }

    suspend fun delete(asset: Asset){
        assetDao.delete(asset.id)
    }

    suspend fun deleteAll(){
        assetDao.deleteAll()
    }

    fun getAllDownloadsInLiveData():LiveData<List<Asset>?>{
        return Transformations.map(assetDao.getAllDownloadInLiveData()) {
            it?.asDomainAssets()
        }
    }

    fun getAssetInLiveData(assetId: String):LiveData<Asset?>{
        return Transformations.map(assetDao.getAssetById(assetId)) {
            it?.asDomainAsset()
        }
    }

    fun getAssetsByMetadata(metadata: Map<String, String>): LiveData<List<Asset>?> {
        return Transformations.map(assetDao.getAllDownloadInLiveData()) { assets ->
            assets?.filter { asset ->
                // Check if asset's metadata contains all key-value pairs from the input metadata
                metadata.all { (key, value) ->
                    asset.metadata?.get(key) == value
                }
            }?.asDomainAssets()
        }
    }

    fun getAsset(
        params: TpInitParams,
        callback : NetworkClient.TPResponse<Asset>
    ){
        val video = getAssetFromDB(params)
        if (video != null) {
            callback.onSuccess(video)
        } else {
            fetchAsset(params, callback)
        }
    }

    private fun getAssetFromDB(params: TpInitParams): Asset?{
        var asset : Asset? = null
        runBlocking(Dispatchers.IO) {
            asset = assetDao.getAssetByVideoId(params.videoId!!)?.asDomainAsset()
        }
        return asset
    }

    private fun fetchAsset(
        params: TpInitParams,
        callback : NetworkClient.TPResponse<Asset>
    ) {
        val url = TPStreamsSDK.constructVideoInfoUrl(params.videoId, params.accessToken)
        if (TPStreamsSDK.provider == TPStreamsSDK.Provider.TPStreams){
            NetworkClient<TPStreamsNetworkAsset>(NetworkClient.getOkHttpClient(context)).get(url, object : NetworkClient.TPResponse<TPStreamsNetworkAsset> {
                override fun onSuccess(result: TPStreamsNetworkAsset) {
                    val asset = result.asDomainAsset()
                    asset.id = params.videoId!!
                    callback.onSuccess(asset)
                }

                override fun onFailure(exception: TPException) {
                    callback.onFailure(exception)
                }
            })
        } else {
            NetworkClient<TestpressNetworkAsset>(NetworkClient.getOkHttpClient(context)).get(url, object : NetworkClient.TPResponse<TestpressNetworkAsset> {
                override fun onSuccess(result: TestpressNetworkAsset) {
                    val asset = result.asDomainAsset()
                    asset.id = params.videoId!!
                    callback.onSuccess(asset)
                }

                override fun onFailure(exception: TPException) {
                    callback.onFailure(exception)
                }
            })
        }
    }

}