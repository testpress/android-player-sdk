package com.tpstream.player

import com.tpstream.player.models.TpStreamVideoInfo
import com.tpstream.player.models.VideoInfo

internal class NetworkClass(private val params: TpInitParams) {

    private var videoInfoCallback: VideoInfoCallback? = null

    fun fetch(videoInfoCallback: VideoInfoCallback) {
        this.videoInfoCallback = videoInfoCallback
        if (params.isTPStreams) {
            fetchTpStreamVideoInfo()
        } else {
            fetchTestPressVideoInfo()
        }
    }

    private fun fetchTestPressVideoInfo() {
        val url =
            "https://${params.orgCode}.testpress.in/api/v2.5/video_info/${params.videoId}/?access_token=${params.accessToken}"
        Network<VideoInfo>().get(url, object : Network.TPResponse<VideoInfo> {
            override fun onSuccess(result: VideoInfo) {
                videoInfoCallback?.onSuccess(result)
            }

            override fun onFailure(exception: TPException) {
                videoInfoCallback?.onFailure(exception)
            }
        })
    }

    private fun fetchTpStreamVideoInfo() {
        val url = "https://app.tpstreams.com/api/v1/${params.orgCode}/assets/${params.videoId}/?access_token=${params.accessToken}"
        Network<TpStreamVideoInfo>().get(
            url,
            object : Network.TPResponse<TpStreamVideoInfo> {
                override fun onSuccess(result: TpStreamVideoInfo) {
                    videoInfoCallback?.onSuccess(result.asVideoInfo())
                }

                override fun onFailure(exception: TPException) {
                    videoInfoCallback?.onFailure(exception)
                }
            })
    }

    interface VideoInfoCallback {
        fun onSuccess(result: VideoInfo)
        fun onFailure(exception: TPException)
    }
}