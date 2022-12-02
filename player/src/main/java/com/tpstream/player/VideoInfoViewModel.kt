package com.tpstream.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.tpstream.player.models.VideoInfo

class VideoInfoViewModel(private val videoInfoRepository: VideoInfoRepository):ViewModel() {

    fun get(videoId: String): LiveData<VideoInfo?> {
        return Transformations.map(videoInfoRepository.get(videoId)) {
            it
        }
    }
}