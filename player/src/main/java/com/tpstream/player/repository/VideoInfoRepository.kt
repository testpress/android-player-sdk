package com.tpstream.player.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.VideoInfo

class VideoInfoRepository(val context: Context) {

    private val videoInfoDao = TPStreamsDatabase(context).videoInfoDao()

    fun getVideoInfoByUrl(url:String) : LiveData<VideoInfo?>{
        return videoInfoDao.getByUrl(url)
    }

    fun getVideoInfoByDashUrl(dashUrl:String):LiveData<VideoInfo?>{
        return videoInfoDao.getByDashUrl(dashUrl)
    }

    fun getVideoUrlByVideoId(videoID:String):VideoInfo?{
        return videoInfoDao.getVideoUrlByVideoId(videoID)
    }

    fun addVideoInfo(videoInfo: VideoInfo){
        videoInfoDao.insert(videoInfo)
    }

    fun removeVideoInfo(videoInfo: VideoInfo){
        videoInfoDao.insert(videoInfo)
    }
}