package com.tpstream.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpstream.player.models.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class VideoViewModel(private val videoRepository: VideoRepository):ViewModel() {

    fun get(videoId: String): LiveData<Video?> {
        return Transformations.map(videoRepository.get(videoId)) {
            it
        }
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