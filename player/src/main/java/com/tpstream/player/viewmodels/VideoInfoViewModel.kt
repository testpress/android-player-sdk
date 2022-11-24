package com.tpstream.player.viewmodels

import androidx.lifecycle.*
import com.tpstream.player.models.VideoInfo
import com.tpstream.player.repository.VideoInfoRepository

class VideoInfoViewModel(private val videoInfoRepository: VideoInfoRepository):ViewModel() {

    fun getVideoInfoByUrl(url:String):LiveData<VideoInfo?>{
        return videoInfoRepository.getVideoInfoByUrl(url)
    }

    fun getVideoInfoByDashUrl(dashUrl:String):LiveData<VideoInfo?>{
        return videoInfoRepository.getVideoInfoByDashUrl(dashUrl)
    }

    fun getVideoUrlByVideoId(videoID:String):VideoInfo?{
        return videoInfoRepository.getVideoUrlByVideoId(videoID)
    }

    fun addVideoInfo(videoInfo: VideoInfo){
        videoInfoRepository.addVideoInfo(videoInfo)
    }

    fun removeVideoInfo(videoInfo: VideoInfo){
        videoInfoRepository.removeVideoInfo(videoInfo)
    }
}