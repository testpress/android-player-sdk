package com.tpstream.player.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.tpstream.player.TPException
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import com.tpstream.player.data.source.local.TPStreamsDatabase
import com.tpstream.player.data.source.network.NetworkAsset
import com.tpstream.player.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

internal class VideoRepository(context: Context) {

    private val assetDao = TPStreamsDatabase(context).assetDao()

    suspend fun update(asset: Asset){
        assetDao.insert(asset.asLocalAsset())
    }

    fun get(videoId: String): LiveData<Asset?> {
        return Transformations.map(assetDao.getVideoById(videoId)) {
            it?.asDomainAsset()
        }
    }

    fun getVideoByUrl(url:String):Asset? {
        return assetDao.getVideoByUrl(url)?.asDomainAsset()
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

    fun getVideo(
        params: TpInitParams,
        callback : NetworkClient.TPResponse<Asset>
    ){
        val video = getVideoFromDB(params)
        if (video != null) {
            callback.onSuccess(video)
        } else {
            fetchVideo(params, callback)
        }
    }

    private fun getVideoFromDB(params: TpInitParams): Asset?{
        var asset : Asset? = null
        runBlocking(Dispatchers.IO) {
            asset = assetDao.getVideoByVideoId(params.videoId!!)?.asDomainAsset()
        }
        return asset
    }

    private fun fetchVideo(
        params: TpInitParams,
        callback : NetworkClient.TPResponse<Asset>
    ) {
        val url = TPStreamsSDK.constructVideoInfoUrl(params.videoId, params.accessToken)
        NetworkClient<NetworkAsset>().get(url, object : NetworkClient.TPResponse<NetworkAsset> {
            override fun onSuccess(result: NetworkAsset) {
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