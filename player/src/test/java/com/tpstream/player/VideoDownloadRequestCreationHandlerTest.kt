package com.tpstream.player

import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.tpstream.player.models.VideoInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class VideoDownloadRequestCreationHandlerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var downloadRequest: DownloadRequest

    @Mock
    private lateinit var context: Context

    private var videoInfo: VideoInfo = VideoInfo(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )

    private val accessToken = "c381512b-7337-4d8e-a8cf-880f4f08fd08"
    private val videoId = "C3XLe1CCcOq"
    private val orgCode = "demoveranda"

    private val tpInitParams = TpInitParams.Builder()
        .setAccessToken(accessToken)
        .setVideoId(videoId)
        .setOrgCode(orgCode)
        .build()


    @Before
    fun setup() {
        downloadRequest = DownloadRequest.Builder("demo", Uri.EMPTY)
            .setData(byteArrayOf(100))
            .setMimeType("mimeType")
            .setCustomCacheKey("cacheKey")
            .setKeySetId(byteArrayOf(50))
            .build()
    }

    @Test
    fun test_downloadRequest() {
        val videoDownloadRequestCreationHandler = VideoDownloadRequestCreationHandler(
            context,videoInfo,tpInitParams
        )

        val buildDownloadRequest = videoDownloadRequestCreationHandler.buildDownloadRequest(
            DefaultTrackSelector.Parameters.Builder(context).build().overrides.toMutableMap()
        )

        Assert.assertEquals(downloadRequest,buildDownloadRequest)

    }

}