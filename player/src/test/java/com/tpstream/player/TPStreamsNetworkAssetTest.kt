package com.tpstream.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tpstream.player.data.source.network.TPStreamsNetworkAsset
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
class TPStreamsNetworkAssetTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockWebServer: MockWebServer
    private val networkClient: NetworkClient<TPStreamsNetworkAsset> = NetworkClient(TPStreamsNetworkAsset::class.java, OkHttpClient())

    private lateinit var callbackResponse: NetworkClient.TPResponse<TPStreamsNetworkAsset>
    private var callbackResult: TPStreamsNetworkAsset? = null
    private lateinit var callbackException: TPException

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        TPStreamsSDK.initialize(TPStreamsSDK.Provider.TPStreams,"demo")
        runBlocking {
            callbackResponse = object : NetworkClient.TPResponse<TPStreamsNetworkAsset> {
                override fun onSuccess(result: TPStreamsNetworkAsset) {
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
        val successResponse = MockResponse().setResponseCode(200).setBody(getTpStreamsAssetJSON())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            networkClient.get(mockWebServer.url("/").toString(), callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        // Assertions for main fields
        assertEquals(callbackResult?.title, "Raj 5")
        assertEquals(callbackResult?.type, "livestream")
        assertEquals(callbackResult?.id, "AQ3FFGFKq3g")

        // Assertions for video fields
        val video = callbackResult?.video
        assertEquals(video?.status, "Completed")
        assertEquals(video?.playbackUrl, "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/video.m3u8")
        assertEquals(video?.dashUrl, "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/video.mpd")
        assertEquals(video?.previewThumbnailUrl, "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/thumbnails/thumbnail_4.png")
        assertEquals(video?.enableDrm, true)
        assertEquals(video?.duration, 139L)

        // Assertions for live stream fields
        val liveStream = callbackResult?.liveStream
        assertEquals(liveStream?.status, "Completed")
        assertEquals(liveStream?.hlsUrl, "https://d384padtbeqfgy.cloudfront.net/live/6eafqn/AQ3FFGFKq3g/video.m3u8")
        assertEquals(liveStream?.start, "2024-07-15 11:53:06")
        assertEquals(liveStream?.transcodeRecordedVideo, true)
        assertEquals(liveStream?.enableDrmForRecording, true)
        assertEquals(liveStream?.chatEmbedUrl, "https://app.tpstreams.com/live-chat/6eafqn/AQ3FFGFKq3g/")
        assertEquals(liveStream?.enableDrm, false)
        assertEquals(liveStream?.noticeMessage, "The live stream has come to an end. Stay tuned, we'll have the recording ready for you shortly.")

        // Assertions for tracks
        val tracks = video?.tracks
        assertEquals(tracks?.size, 2)
        assertEquals(tracks?.filter { it.type == "Preview Thumbnail"}?.size, 1)
        assertEquals(tracks?.filter { it.type == "Playlist"}?.size, 1)

        // Assertions for Playlist
        val playlists = tracks?.filter { it.type == "Playlist" }
        assertEquals(playlists?.size, 6)
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

    private fun getTpStreamsAssetJSON(): String {
        return """
                {
                    "title": "manifest.mp4",
                    "bytes": null,
                    "type": "video",
                    "video": {
                        "progress": 0,
                        "thumbnails": null,
                        "status": "Completed",
                        "playback_url": "https://d384padtbeqfgy.cloudfront.net/transcoded/72c9RRHj3M8/video.m3u8",
                        "dash_url": null,
                        "preview_thumbnail_url": null,
                        "cover_thumbnail_url": null,
                        "format": "abr",
                        "resolutions": [
                            "720p",
                            "480p",
                            "360p",
                            "240p",
                            "1080p",
                            "4k"
                        ],
                        "video_codec": "h264",
                        "audio_codec": "aac",
                        "enable_drm": false,
                        "tracks": [
                            {
                                "id": 134222,
                                "type": "Preview Thumbnail",
                                "name": "",
                                "url": "https://d384padtbeqfgy.cloudfront.net/None",
                                "bytes": null,
                                "language": "en",
                                "width": null,
                                "height": null,
                                "duration": null,
                                "is_active": true,
                                "playlists": [],
                                "subtitle_type": "Uploaded",
                                "preview_thumbnail": {
                                    "url": "https://d384padtbeqfgy.cloudfront.net/transcoded/72c9RRHj3M8/sprite/sprite_image.png",
                                    "interval": 2,
                                    "width": 160,
                                    "height": 90,
                                    "rows": 18,
                                    "columns": 18
                                }
                            },
                            {
                                "id": 134229,
                                "type": "Playlist",
                                "name": "",
                                "url": "https://d384padtbeqfgy.cloudfront.net/None",
                                "bytes": null,
                                "language": "en",
                                "width": null,
                                "height": null,
                                "duration": null,
                                "is_active": true,
                                "playlists": [
                                    {
                                        "name": "4k",
                                        "bytes": 291433715,
                                        "width": 3840,
                                        "height": 2160
                                    },
                                    {
                                        "name": "1080p",
                                        "bytes": 143993774,
                                        "width": 1920,
                                        "height": 1080
                                    },
                                    {
                                        "name": "240p",
                                        "bytes": 19372332,
                                        "width": 426,
                                        "height": 240
                                    },
                                    {
                                        "name": "360p",
                                        "bytes": 29839983,
                                        "width": 640,
                                        "height": 360
                                    },
                                    {
                                        "name": "480p",
                                        "bytes": 42297052,
                                        "width": 854,
                                        "height": 480
                                    },
                                    {
                                        "name": "720p",
                                        "bytes": 70430690,
                                        "width": 1280,
                                        "height": 720
                                    }
                                ],
                                "subtitle_type": "Uploaded",
                                "preview_thumbnail": null
                            }
                        ],
                        "inputs": [
                            {
                                "url": "private/cd928dc3384d4d4db9f32f4597862296.mp4"
                            }
                        ],
                        "transmux_only": null,
                        "duration": 635,
                        "content_protection_type": "disabled",
                        "generate_subtitle": false
                    },
                    "id": "72c9RRHj3M8",
                    "live_stream": null,
                    "parent": {
                        "title": "NON-DRM",
                        "uuid": "5gJhaaBKZBK"
                    },
                    "parent_id": "5gJhaaBKZBK",
                    "views_count": 0,
                    "average_watched_time": 0,
                    "total_watch_time": 0,
                    "unique_viewers_count": 0,
                    "download_url": "https://d384padtbeqfgy.cloudfront.net/private/cd928dc3384d4d4db9f32f4597862296.mp4?response-content-disposition=attachment%3B+filename%3Dmanifest.mp4&Expires=1727379627&Signature=aSkGYh6nB4xHVGpyqQ60262hgE8kwJiNoK64xEOnvhVdWkIygJrgj8uk5AndWwj86ig8Q-SrkOls6PjHQl6nlpPG8stm763ozTRczo2VuLVpQrUF1wD0Y6-Ni2F-5-FEYB1rWYXR9R0WY0Cg8y0m0uhLTiAlKSwgeSAasIeV-5l-qFLxNrcjJR8WqvUQ6z8LiFB51aR2Nr8Nr91LnDYk9bidzw1Xzo9xxsMazUvFoGqRLxZ8TGycIFrK-xPbSqZJIngWNEkT0rqS9WO~QzBgFylKcA7iydOoQhkajtFq1jspVmbNWwc7OB0~RAxT4a6p3gzaPdhJRSjkDyF8j6JTiA__&Key-Pair-Id=K2XWKDWM065EGO",
                    "folder_tree": "NON-DRM"
                }
        """.trimIndent()
    }
}