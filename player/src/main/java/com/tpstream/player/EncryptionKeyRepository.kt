package com.tpstream.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody

class EncryptionKeyRepository(context: Context) {
    private val sharedPreference = context.getSharedPreferences(
        "VIDEO_ENCRYPTION_KEY",
        Context.MODE_PRIVATE
    )

    fun fetchAndStore(params: TpInitParams, playbackUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (playbackUrl.contains(".m3u8")) {
                val encryptionKeyUrl = EncryptionKeyDownloader().getEncryptionKeyUrl(playbackUrl)
                val encryptionKey = EncryptionKeyDownloader().getEncryptionKey(params)
                save(encryptionKey, encryptionKeyUrl)
            }
        }
    }

    private fun save(encryptionKey: String, encryptionKeyUrl: String) {
        with(sharedPreference.edit()) {
            putString(
                encryptionKeyUrl,
                encryptionKey
            )
            apply()
        }
    }

    fun delete(encryptionKeyUrl: String){
        with(sharedPreference.edit()) {
            remove(encryptionKeyUrl)
            apply()
        }
    }

    fun get(encryptionKeyUrl: String): ByteArray? {
        val encryptionKey = sharedPreference.getString(encryptionKeyUrl, null)
        if (encryptionKey != null) {
            val split =
                encryptionKey.substring(1, encryptionKey.length - 1).split(", ").toTypedArray()
            val array = ByteArray(split.size)
            for (i in split.indices) {
                array[i] = split[i].toByte()
            }
            return array
        }
        return null
    }

    fun hasEncryptionKey(encryptionKeyUrl: String): Boolean {
        val encryptionKey = sharedPreference.getString(encryptionKeyUrl, null)
        return encryptionKey != null && encryptionKey.isNotEmpty()
    }
}