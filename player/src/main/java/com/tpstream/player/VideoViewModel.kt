package com.tpstream.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpstream.player.data.Video
import com.tpstream.player.data.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class VideoViewModel(private val videoRepository: VideoRepository):ViewModel() {

    fun get(videoId: String): LiveData<Video?> {
        return videoRepository.get(videoId)
    }

    fun insert(video: Video){
        viewModelScope.launch{
            videoRepository.insert(video)
        }
    }

    fun delete(videoId: String){
        viewModelScope.launch {
            var video : Video? = null
            runBlocking(Dispatchers.IO) {
                video = videoRepository.getVideoByVideoId(videoId)
            }
            videoRepository.delete(video!!)
        }
    }
}