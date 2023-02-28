package com.tpstream.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpstream.player.models.OfflineVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class OfflineVideoInfoViewModel(private val offlineVideoInfoRepository: OfflineVideoInfoRepository):ViewModel() {

    fun get(videoId: String): LiveData<OfflineVideoInfo?> {
        return Transformations.map(offlineVideoInfoRepository.get(videoId)) {
            it
        }
    }

    fun insert(offlineVideoInfo: OfflineVideoInfo){
        viewModelScope.launch{
            offlineVideoInfoRepository.insert(offlineVideoInfo)
        }
    }

    fun delete(videoId: String){
        viewModelScope.launch {
            var offlineVideoInfo : OfflineVideoInfo? = null
            runBlocking(Dispatchers.IO) {
                offlineVideoInfo = offlineVideoInfoRepository.getOfflineVideoInfoByVideoId(videoId)
            }
            offlineVideoInfoRepository.delete(offlineVideoInfo!!)
        }
    }
}