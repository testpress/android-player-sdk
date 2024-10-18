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
        assertEquals(video?.hasH265Tracks, true)

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
        val playlists = tracks?.filter { it.type == "Playlist" }?.first()?.playlists
        assertEquals(playlists?.size, 8)
        assertEquals(playlists?.filter { it.name?.contains("dash") == true }?.size, 4)
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
                "title": "Raj 5",
                "bytes": null,
                "type": "livestream",
                "video": {
                    "progress": 0,
                    "thumbnails": [
                        "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/thumbnails/thumbnail_4.png",
                        "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/thumbnails/thumbnail_6.png",
                        "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/thumbnails/thumbnail_5.png",
                        "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/thumbnails/thumbnail_2.png",
                        "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/thumbnails/thumbnail_1.png",
                        "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/thumbnails/thumbnail_3.png"
                    ],
                    "status": "Completed",
                    "playback_url": "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/video.m3u8",
                    "dash_url": "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/video.mpd",
                    "preview_thumbnail_url": "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/thumbnails/thumbnail_4.png",
                    "cover_thumbnail_url": "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/thumbnails/thumbnail_4.png",
                    "format": "abr",
                    "resolutions": [
                        "240p",
                        "360p",
                        "480p",
                        "720p"
                    ],
                    "video_codec": "h264",
                    "audio_codec": "aac",
                    "enable_drm": true,
                    "tracks": [
                        {
                            "id": 66268,
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
                                "url": "https://d384padtbeqfgy.cloudfront.net/transcoded/AQ3FFGFKq3g/sprite/sprite_image.png",
                                "interval": 1,
                                "width": 160,
                                "height": 90,
                                "rows": 12,
                                "columns": 12
                            }
                        },
                        {
                            "id": 140814,
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
                                    "name": "720p_dash",
                                    "bytes": 717601,
                                    "width": 1280,
                                    "height": 720
                                },
                                {
                                    "name": "720p_hls",
                                    "bytes": 655730,
                                    "width": 1280,
                                    "height": 720
                                },
                                {
                                    "name": "480p_dash",
                                    "bytes": 520776,
                                    "width": 854,
                                    "height": 480
                                },
                                {
                                    "name": "480p_hls",
                                    "bytes": 458855,
                                    "width": 854,
                                    "height": 480
                                },
                                {
                                    "name": "360p_dash",
                                    "bytes": 445145,
                                    "width": 640,
                                    "height": 360
                                },
                                {
                                    "name": "360p_hls",
                                    "bytes": 383270,
                                    "width": 640,
                                    "height": 360
                                },
                                {
                                    "name": "240p_dash",
                                    "bytes": 360381,
                                    "width": 426,
                                    "height": 240
                                },
                                {
                                    "name": "240p_hls",
                                    "bytes": 298466,
                                    "width": 426,
                                    "height": 240
                                }
                            ],
                            "subtitle_type": "Uploaded",
                            "preview_thumbnail": null
                        }
                    ],
                    "inputs": [
                        {
                            "url": "private/AQ3FFGFKq3g/video.mp4"
                        }
                    ],
                    "transmux_only": null,
                    "duration": 139,
                    "content_protection_type": "drm",
                    "generate_subtitle": false,
                    "output_urls": {
                        "h264": {
                            "hls_url": "https://d28qihy7z761lk.cloudfront.net/transcoded/66RQCnD8u63/video.m3u8",
                            "dash_url": "https://d28qihy7z761lk.cloudfront.net/transcoded/66RQCnD8u63/video.mpd"
                        },
                        "h265": {
                            "hls_url": "https://d28qihy7z761lk.cloudfront.net/transcoded/66RQCnD8u63/video_h265.m3u8",
                            "dash_url": "https://d28qihy7z761lk.cloudfront.net/transcoded/66RQCnD8u63/video_h265.mpd"
                        }
                    }
                },
                "id": "AQ3FFGFKq3g",
                "live_stream": {
                    "rtmp_url": "rtmp://65.2.80.123/live",
                    "stream_key": "org-6eafqn-live-AQ3FFGFKq3g-EDZu",
                    "status": "Completed",
                    "hls_url": "https://d384padtbeqfgy.cloudfront.net/live/6eafqn/AQ3FFGFKq3g/video.m3u8",
                    "start": "2024-07-15 11:53:06",
                    "transcode_recorded_video": true,
                    "enable_drm_for_recording": true,
                    "chat_embed_url": "https://app.tpstreams.com/live-chat/6eafqn/AQ3FFGFKq3g/",
                    "chat_room_id": "321d94c3-9434-4515-8af8-9b003b96501d",
                    "resolutions": [
                        "240p",
                        "480p",
                        "720p"
                    ],
                    "enable_drm": false,
                    "enable_llhls": false,
                    "latency": "Low Latency",
                    "notice_message": "The live stream has come to an end. Stay tuned, we'll have the recording ready for you shortly."
                },
                "parent": null,
                "parent_id": null,
                "views_count": 1,
                "average_watched_time": 0,
                "total_watch_time": 0,
                "unique_viewers_count": 1,
                "download_url": "https://d384padtbeqfgy.cloudfront.net/private/AQ3FFGFKq3g/video.mp4?response-content-disposition=attachment%3B+filename%3DRaj+5.mp4&Expires=1727445631&Signature=ypGs92LcP6x6SLhrgF4mjuSzSiaFvaBKgYKWYalkqVZbovWEetXWl2l3XAlWBk9eMszqHi7Zj7d2-AZdFXWzEaq1waSDSGCqbGapEqfaO5ySgMpZ6HLp0zzso0GgrP8u27NtzOgG2SEMcgmv07gI5J-XmVCixyDl1WWShJ3lkdcp9MEfrWpVlme8JqrKnoaYwuuX6J83VJeFk0NRndemirSsOCFHy2YIOm2VUX4CHVTZ5Y3S0XnW508xScxrK9OLNjoLGbAvuMzW5zo~ewmNhl8NcFWLy7yLJPOfD2KbWv8obbfdNbBfOiaRj~PTrl-sWHF-2b9nzhgiPBaGzPteog__&Key-Pair-Id=K2XWKDWM065EGO",
                "folder_tree": ""
            }
        """.trimIndent()
    }
}