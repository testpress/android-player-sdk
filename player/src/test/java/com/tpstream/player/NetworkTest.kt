package com.tpstream.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tpstream.player.data.source.network.NetworkVideo
import com.tpstream.player.data.source.network.VideoNetworkDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NetworkTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockWebServer: MockWebServer
    private val network: VideoNetworkDataSource<NetworkVideo> = VideoNetworkDataSource(NetworkVideo::class.java)

    private lateinit var callbackResponse: VideoNetworkDataSource.TPResponse<NetworkVideo>
    private var callbackResult: NetworkVideo? = null
    private lateinit var callbackException: TPException

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        runBlocking {
            callbackResponse = object : VideoNetworkDataSource.TPResponse<NetworkVideo> {
                override fun onSuccess(result: NetworkVideo) {
                    callbackResult = result
                }

                override fun onFailure(exception: TPException) {
                    callbackException = exception
                }
            }
        }
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun testTestpressVideoGetSyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getTestpressVideoJSON())
        mockWebServer.enqueue(successResponse)
        val response = network.get(mockWebServer.url("/").toString())
        mockWebServer.takeRequest()
        assertEquals(response?.title, "No Encrypt")
        assertEquals(
            response?.url,
            "https://verandademo-cdn.testpress.in/institute/demoveranda/courses/video-content/videos/transcoded/c3785c76604048f2afd0b382e68f7dd2/video.m3u8"
        )
    }

    @Test
    fun testTpStreamsVideoGetSyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getTpStreamsVideoJSON())
        mockWebServer.enqueue(successResponse)
        val response = network.get(mockWebServer.url("/").toString())
        mockWebServer.takeRequest()
        assertEquals(response?.title, "BigBuckBunny.mp4")
        assertEquals(
            response?.networkVideoContent?.dash_url,
            "https://d3cydmgt9q030i.cloudfront.net/transcoded/73633fa3-61c6-443c-b625-ac4e85b28cfc/video.mpd"
        )
    }

    @Test
    fun testErrorReceiveOnGetSyncRequest() {
        val errorResponse = MockResponse().setResponseCode(400)
        mockWebServer.enqueue(errorResponse)
        try {
            network.get(mockWebServer.url("/").toString())
            mockWebServer.takeRequest()
        } catch (exception: TPException) {
            assertEquals(exception.response?.code, 400)
        }
    }

    @Test
    fun testTestpressVideoGetAsyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getTestpressVideoJSON())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            network.get(mockWebServer.url("/").toString(), callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        assertEquals(callbackResult?.title, "No Encrypt")
        assertEquals(callbackResult?.duration, "0:10:34")
    }

    @Test
    fun testTpStreamsVideoGetAsyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getTpStreamsVideoJSON())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            network.get(mockWebServer.url("/").toString(), callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        assertEquals(callbackResult?.title, "BigBuckBunny.mp4")
        assertEquals(callbackResult?.id, "73633fa3-61c6-443c-b625-ac4e85b28cfc")
    }

    @Test
    fun testErrorReceiveOnGetAsyncRequest() {
        val errorResponse = MockResponse().setResponseCode(400)
        mockWebServer.enqueue(errorResponse)
        runBlocking {
            network.get(mockWebServer.url("/").toString(), callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        assertEquals(callbackException.response?.code, 400)
    }

    @Test
    fun testInvalidOrgCodeThrowException() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getInvalidResponse())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            network.get(mockWebServer.url("/").toString(), callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        assertEquals(callbackException.response?.code, 200)
        assertEquals(callbackResult, null)
    }

    private fun getTestpressVideoJSON(): String {
        return """
            {
                "title": "No Encrypt",
                "thumbnail": null,
                "thumbnail_small": null,
                "thumbnail_medium": null,
                "url": "https://verandademo-cdn.testpress.in/institute/demoveranda/courses/video-content/videos/transcoded/c3785c76604048f2afd0b382e68f7dd2/video.m3u8",
                "dash_url": null,
                "hls_url": null,
                "duration": "0:10:34",
                "description": "",
                "transcoding_status": 4
            }
        """.trimIndent()
    }

    private fun getTpStreamsVideoJSON(): String {
        return """
            {
                "title": "BigBuckBunny.mp4",
                "bytes": null,
                "type": "video",
                "video": {
                            "progress": 0,
                            "thumbnails": [],
                            "status": "Completed",
                            "playback_url": "https://d3cydmgt9q030i.cloudfront.net/transcoded/73633fa3-61c6-443c-b625-ac4e85b28cfc/video.m3u8",
                            "dash_url": "https://d3cydmgt9q030i.cloudfront.net/transcoded/73633fa3-61c6-443c-b625-ac4e85b28cfc/video.mpd",
                            "preview_thumbnail_url": null,
                            "format": "abr",
                            "resolutions": [
                                "480p",
                                "720p"
                            ],
                            "video_codec": "h264",
                            "audio_codec": "aac",
                            "enable_drm": false,
                            "tracks": [],
                            "inputs": [
                                {
                                    "url": "private/ef58aefdf4394661ab032a30c30b83ae.mp4"
                                }
                            ]
                          },
                "id": "73633fa3-61c6-443c-b625-ac4e85b28cfc",
                "live_stream": null
            }
        """.trimIndent()
    }

    private fun getInvalidResponse(): String {
        return """
            <html>
            <head>
                <title>Invalid OrgCode</title>
            </head>
            <body>
            </body>
            </html>
        """.trimIndent()
    }
}