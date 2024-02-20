package com.tpstream.player.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpstream.player.data.Asset
import com.tpstream.player.data.VideoRepository
import kotlinx.coroutines.launch

internal class VideoViewModel(private val videoRepository: VideoRepository):ViewModel() {

    fun get(videoId: String): LiveData<Asset?> {
        return videoRepository.get(videoId)
    }

    fun insert(asset: Asset){
        viewModelScope.launch{
            videoRepository.insert(asset)
        }
    }

}