package com.tpstream.player

import com.tpstream.player.data.LiveStream
import com.tpstream.player.data.asDomainAsset
import com.tpstream.player.data.source.network.TPStreamsNetworkAsset
import com.tpstream.player.data.source.network.TestpressNetworkAsset
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date

class AssetModelMappingTest {

    @Test
    fun `test TPStreamsNetworkAsset To Asset Mapping`() {
        // Prepare the input data
        val networkAsset = TPStreamsNetworkAsset(
            title = "Sample Video",
            type = "video",
            video = TPStreamsNetworkAsset.Video(
                status = "Completed",
                playbackUrl = "http://example.com/playback",
                dashUrl = "http://example.com/dash",
                previewThumbnailUrl = "http://example.com/thumbnail.jpg",
                enableDrm = true,
                tracks = listOf(
                    TPStreamsNetworkAsset.Video.Track(
                        id = 1,
                        type = "subtitles",
                        name = "English",
                        url = "http://example.com/track_en.vtt",
                        language = "en",
                        duration = 300L,
                        playlists = listOf()
                    ),
                    TPStreamsNetworkAsset.Video.Track(
                        id = 1,
                        type = "Playlist",
                        name = null,
                        url = null,
                        language = null,
                        duration = null,
                        playlists = getPlaylist()
                    )
                ),
                duration = 3600L,
                outputURLs = mapOf(
                    "h264" to TPStreamsNetworkAsset.Video.OutputUrl(
                        hlsUrl = "https://d28qihy7z761lk.cloudfront.net/transcoded/66RQCnD8u63/video.m3u8",
                        dashUrl = "https://d28qihy7z761lk.cloudfront.net/transcoded/66RQCnD8u63/video.mpd"
                    ),
                    "h265" to TPStreamsNetworkAsset.Video.OutputUrl(
                        hlsUrl = "https://d28qihy7z761lk.cloudfront.net/transcoded/66RQCnD8u63/video_h265.m3u8",
                        dashUrl = "https://d28qihy7z761lk.cloudfront.net/transcoded/66RQCnD8u63/video_h265.mpd"
                    )
                )
            ),
            id = "12345",
            liveStream = TPStreamsNetworkAsset.LiveStream(
                status = "Streaming",
                hlsUrl = "http://example.com/live.m3u8",
                dashUrl = "http://example.com/live.mpd",
                start = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                transcodeRecordedVideo = true,
                enableDrmForRecording = false,
                chatEmbedUrl = null,
                enableDrm = true,
                noticeMessage = "Live stream is on"
            ),
            folderTree = "folder1/folder2"
        )

        // Call the method under test
        val domainAsset = networkAsset.asDomainAsset()

        // Validate the result
        assertEquals("Sample Video", domainAsset.title)
        assertEquals("video", domainAsset.type)
        assertEquals("https://d28qihy7z761lk.cloudfront.net/transcoded/66RQCnD8u63/video_h265.mpd", domainAsset.video.url)
        assertEquals(3600L, domainAsset.video.duration)
        assertEquals("Completed", domainAsset.video.transcodingStatus)
        assertEquals(2, domainAsset.video.tracks?.size)
        assertEquals("subtitles", domainAsset.video.tracks?.get(0)?.type)
        assertEquals("http://example.com/thumbnail.jpg", domainAsset.thumbnail)
        assertEquals("http://example.com/live.m3u8", domainAsset.liveStream?.hlsUrl)
        assertEquals("http://example.com/live.mpd", domainAsset.liveStream?.dashUrl)
        assertEquals("Streaming", domainAsset.liveStream?.status)
        assertEquals("folder1/folder2", domainAsset.folderTree)
        assertEquals(6,domainAsset.video.tracks?.first { it.type == "Playlist" }?.playlists?.size)
        assertEquals(true, domainAsset.video.isH265VideoCodec)
    }

    private fun getPlaylist() = listOf(
        TPStreamsNetworkAsset.Video.Track.Playlist(
            name = "4k",
            bytes = 291433715,
            width = 3840,
            height = 2160
        ),
        TPStreamsNetworkAsset.Video.Track.Playlist(
            name = "1080p",
            bytes = 143993774,
            width = 1920,
            height = 1080
        ),
        TPStreamsNetworkAsset.Video.Track.Playlist(
            name = "240p",
            bytes = 19372332,
            width = 426,
            height = 240
        ),
        TPStreamsNetworkAsset.Video.Track.Playlist(
            name = "360p",
            bytes = 29839983,
            width = 640,
            height = 360
        ),
        TPStreamsNetworkAsset.Video.Track.Playlist(
            name = "480p",
            bytes = 42297052,
            width = 854,
            height = 480
        ),
        TPStreamsNetworkAsset.Video.Track.Playlist(
            name = "720p",
            bytes = 70430690,
            width = 1280,
            height = 720
        )
    )

    @Test
    fun `test TestpressNetworkAsset to Asset Mapping`() {
        // Arrange
        val networkAsset = TestpressNetworkAsset(
            id = "lY2UKQk1Lyf",
            title = "Test",
            contentType = "Live Stream",
            video = TestpressNetworkAsset.Video(
                id = "lY2UKQk1Lyf",
                title = "Test",
                thumbnail = null,
                thumbnailSmall = null,
                thumbnailMedium = null,
                url = "",
                dashUrl = "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/courses/grace-ias-mains-course/videos/transcoded/56f3701b230b4f38a80ef44676df1771/video.mpd",
                hlsUrl = "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/courses/grace-ias-mains-course/videos/transcoded/56f3701b230b4f38a80ef44676df1771/video.m3u8",
                duration = null,
                description = "",
                transcodingStatus = "Completed",
                drmEnabled = true
            ),
            liveStream = TestpressNetworkAsset.LiveStream(
                id = 296,
                title = "Test",
                streamUrl = "https://d36vpug2b5drql.cloudfront.net/live/hfdr5f/9HUmcuS77fp/video.m3u8",
                duration = null,
                showRecordedVideo = true,
                status = "Completed",
                chatEmbedUrl = "https://lmsdemo.testpress.in/live-chat/lY2UKQk1Lyf/",
                noticeMessage = "The live stream has come to an end. Stay tuned, we'll have the recording ready for you shortly."
            )
        )

        // Act
        val asset = networkAsset.asDomainAsset()

        // Assert
        assertEquals("lY2UKQk1Lyf", asset.id)
        assertEquals("Test", asset.title)
        assertEquals("Live Stream", asset.type)
        assertEquals("", asset.thumbnail)
        assertEquals("", asset.description)
        assertEquals(
            "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/courses/grace-ias-mains-course/videos/transcoded/56f3701b230b4f38a80ef44676df1771/video.mpd",
            asset.video.url
        ) // URL is empty in the JSON
        assertEquals("Completed", asset.video.transcodingStatus)
        assertEquals(true, asset.video.isDrmProtected)

        // Verify LiveStream data
        assertEquals(
            "https://d36vpug2b5drql.cloudfront.net/live/hfdr5f/9HUmcuS77fp/video.m3u8",
            asset.liveStream?.getUrl()
        )
        assertEquals("Completed", asset.liveStream?.status)
        assertEquals(true, asset.liveStream?.recordingEnabled)
        assertEquals(
            "The live stream has come to an end. Stay tuned, we'll have the recording ready for you shortly.",
            asset.liveStream?.noticeMessage
        )
    }

    @Test
    fun `getUrl returns DASH URL when DRM is enabled`() {
        val liveStream = LiveStream(
            hlsUrl = "https://example.com/live/video.m3u8",
            dashUrl = "https://example.com/live/video.mpd",
            status = "Streaming",
            startTime = Date(),
            recordingEnabled = false,
            enabledDRMForRecording = true,
            enabledDRMForLive = true,
            noticeMessage = ""
        )
        assertEquals("https://example.com/live/video.mpd", liveStream.getUrl())
    }
}