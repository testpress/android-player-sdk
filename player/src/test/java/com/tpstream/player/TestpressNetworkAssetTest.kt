package com.tpstream.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tpstream.player.data.source.network.TestpressNetworkAsset
import com.tpstream.player.util.NetworkClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TestpressNetworkAssetTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockWebServer: MockWebServer
    private val networkClient: NetworkClient<TestpressNetworkAsset> = NetworkClient(TestpressNetworkAsset::class.java, OkHttpClient())

    private lateinit var callbackResponse: NetworkClient.TPResponse<TestpressNetworkAsset>
    private var callbackResult: TestpressNetworkAsset? = null
    private lateinit var callbackException: TPException

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        TPStreamsSDK.initialize(TPStreamsSDK.Provider.TestPress,"demo")
        runBlocking {
            callbackResponse = object : NetworkClient.TPResponse<TestpressNetworkAsset> {
                override fun onSuccess(result: TestpressNetworkAsset) {
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
    fun testGetAsyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getTestpressVideoJSON())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            networkClient.get(mockWebServer.url("/").toString(), callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        // Assertions for main fields
        assertEquals(callbackResult?.id, "lY2UKQk1Lyf")
        assertEquals(callbackResult?.title, "Test")
        assertEquals(callbackResult?.contentType, "Live Stream")

        // Assertions for video fields
        val video = callbackResult?.video
        assertEquals(video?.id, "lY2UKQk1Lyf")
        assertEquals(video?.title, "Test")
        assertEquals(video?.url, "")
        assertEquals(video?.dashUrl, "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/courses/grace-ias-mains-course/videos/transcoded/56f3701b230b4f38a80ef44676df1771/video.mpd")
        assertEquals(video?.hlsUrl, "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/courses/grace-ias-mains-course/videos/transcoded/56f3701b230b4f38a80ef44676df1771/video.m3u8")
        assertEquals(video?.duration, 600L)
        assertEquals(video?.description, "600")
        assertEquals(video?.transcodingStatus, "Completed")
        assertEquals(video?.drmEnabled, true)

        // Assertions for live_stream fields
        val liveStream = callbackResult?.liveStream
        assertEquals(liveStream?.id, 296L)
        assertEquals(liveStream?.title, "Test")
        assertEquals(liveStream?.streamUrl, "https://d36vpug2b5drql.cloudfront.net/live/hfdr5f/9HUmcuS77fp/video.m3u8")
        assertEquals(liveStream?.duration, 600L)
        assertEquals(liveStream?.showRecordedVideo, true)
        assertEquals(liveStream?.status, "Completed")
        assertEquals(liveStream?.chatEmbedUrl, "https://lmsdemo.testpress.in/live-chat/lY2UKQk1Lyf/")
        assertEquals(liveStream?.noticeMessage, "The live stream has come to an end. Stay tuned, we'll have the recording readyfor you shortly.")
    }

    @Test
    fun testErrorReceiveOnGetAsyncRequest() {
        val errorResponse = MockResponse().setResponseCode(400)
        mockWebServer.enqueue(errorResponse)
        runBlocking {
            networkClient.get(mockWebServer.url("/").toString(), callbackResponse)
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
            networkClient.get(mockWebServer.url("/").toString(), callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        assertEquals(callbackException.response?.code, 200)
        assertEquals(callbackResult, null)
    }

    private fun getTestpressVideoJSON(): String {
        return """
                {
                  "id": "lY2UKQk1Lyf",
                  "title": "Test",
                  "content_type": "Live Stream",
                  "video": {
                    "id": "lY2UKQk1Lyf",
                    "title": "Test",
                    "thumbnail": null,
                    "thumbnail_small": null,
                    "thumbnail_medium": null,
                    "url": "",
                    "dash_url": "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/courses/grace-ias-mains-course/videos/transcoded/56f3701b230b4f38a80ef44676df1771/video.mpd",
                    "hls_url": "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/courses/grace-ias-mains-course/videos/transcoded/56f3701b230b4f38a80ef44676df1771/video.m3u8",
                    "duration": 600,
                    "description": "600",
                    "transcoding_status": "Completed",
                    "drm_enabled": true
                  },
                  "live_stream": {
                    "id": 296,
                    "title": "Test",
                    "stream_url": "https://d36vpug2b5drql.cloudfront.net/live/hfdr5f/9HUmcuS77fp/video.m3u8",
                    "duration": 600,
                    "show_recorded_video": true,
                    "status": "Completed",
                    "chat_embed_url": "https://lmsdemo.testpress.in/live-chat/lY2UKQk1Lyf/",
                    "notice_message": "The live stream has come to an end. Stay tuned, we'll have the recording readyfor you shortly."
                  }
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