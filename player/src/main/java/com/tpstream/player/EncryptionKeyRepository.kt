package com.tpstream.player

import android.content.Context
import android.net.Uri
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
    private var encryptionKeyDownloader: EncryptionKeyDownloader = EncryptionKeyDownloader()

    fun put(params: TpInitParams, playbackUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            encryptionKeyDownloader.put(params, playbackUrl)
            saveEncryptionUrl(params)
            saveEncryptionKey()
        }
    }

    private fun saveEncryptionUrl(params: TpInitParams) {
        with(sharedPreference.edit()) {
            putString(params.videoId, encryptionKeyDownloader.encryptionKeyUrl)
            apply()
        }
    }

    private fun saveEncryptionKey() {
        with(sharedPreference.edit()) {
            putString(
                encryptionKeyDownloader.encryptionKeyUrl,
                encryptionKeyDownloader.encryptionKey
            )
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


}