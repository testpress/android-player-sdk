package com.tpstream.player.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tpstream.player.data.Asset
import com.tpstream.player.data.AssetRepository
import kotlinx.coroutines.launch

internal class VideoViewModel(private val assetRepository: AssetRepository):ViewModel() {

    fun get(videoId: String): LiveData<Asset?> {
        return assetRepository.get(videoId)
    }

    fun insert(asset: Asset){
        viewModelScope.launch{
            assetRepository.insert(asset)
        }
    }

}