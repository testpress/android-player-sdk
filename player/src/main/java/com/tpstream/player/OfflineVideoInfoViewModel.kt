package com.tpstream.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.tpstream.player.models.OfflineVideoInfo

class OfflineVideoInfoViewModel(private val offlineVideoInfoRepository: OfflineVideoInfoRepository):ViewModel() {

    fun get(videoId: String): LiveData<OfflineVideoInfo?> {
        return Transformations.map(offlineVideoInfoRepository.get(videoId)) {
            it
        }
    }
}