package com.tpstream.player

import androidx.media3.common.util.Util
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class EncryptionKeyDownloaderTest {

    private lateinit var encryptionKeyDownloader: EncryptionKeyDownloader

    @Before
    fun createService() {
        encryptionKeyDownloader = EncryptionKeyDownloader()
    }

    @Test
    fun testGetMediaPlayListUrl() {
        val result = encryptionKeyDownloader.getSingleResolutionTrackUrl(
            "123456/video.m3u8",
            getMainResponse()
        )
        assertEquals(result, "123456/720p/video.m3u8")
    }

    @Test
    fun getEncryptionKeyUrlUsingMediaPlaylistUrl() {
        val result = encryptionKeyDownloader.getEncryptionKeyUrlUsingSingleResolutionTrackUrl(
            "123456/video.m3u8",
            getPathResponse()
        )
        assertEquals(
            result,
            "https://demo.verandalearning.com/api/v2.4/encryption_key/c15ed6167035471995a4af7213b898a4/"
        )
    }

    private fun getMainResponse(): Response {
        val playlistString = "#EXTM3U\n" +
                "#EXT-X-VERSION:3\n" +
                "#EXT-X-STREAM-INF:BANDWIDTH=1000000,RESOLUTION=1280x720\n" +
                "720p/video.m3u8\n" +
                "\n" +
                "#EXT-X-STREAM-INF:BANDWIDTH=500000,RESOLUTION=854x480\n" +
                "480p/video.m3u8\n" +
                "\n" +
                "#EXT-X-STREAM-INF:BANDWIDTH=192000,RESOLUTION=426x240\n" +
                "240p/video.m3u8"
        val responseBody = Util.getUtf8Bytes(playlistString).toResponseBody()
        return Response.Builder()
            .code(200)
            .request(Request.Builder().url("https://123").build())
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .body(responseBody)
            .build()
    }

    private fun getPathResponse(): Response {
        val playlistString = "#EXTM3U\n" +
                "#EXT-X-VERSION:3\n" +
                "#EXT-X-TARGETDURATION:17\n" +
                "#EXT-X-MEDIA-SEQUENCE:0\n" +
                "#EXT-X-KEY:METHOD=AES-128,URI=\"https://demo.verandalearning.com/api/v2.4/encryption_key/c15ed6167035471995a4af7213b898a4/\",IV=0x00000000000000000000000000000000\n" +
                "#EXTINF:12.766667,\n" +
                "video_0.ts\n" +
                "#EXTINF:7.366667,\n" +
                "#EXT-X-ENDLIST\n"
        val responseBody = Util.getUtf8Bytes(playlistString).toResponseBody()
        return Response.Builder()
            .code(200)
            .request(Request.Builder().url("https://123").build())
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .body(responseBody)
            .build()
    }
}