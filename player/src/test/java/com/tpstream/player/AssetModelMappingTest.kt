package com.tpstream.player

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
                        duration = 300L
                    )
                ),
                duration = 3600L
            ),
            id = "12345",
            liveStream = TPStreamsNetworkAsset.LiveStream(
                status = "Streaming",
                hlsUrl = "http://example.com/live",
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
        assertEquals("http://example.com/dash", domainAsset.video.url)
        assertEquals(3600L, domainAsset.video.duration)
        assertEquals("Completed", domainAsset.video.transcodingStatus)
        assertEquals(1, domainAsset.video.tracks?.size)
        assertEquals("subtitles", domainAsset.video.tracks?.get(0)?.type)
        assertEquals("http://example.com/thumbnail.jpg", domainAsset.thumbnail)
        assertEquals("http://example.com/live", domainAsset.liveStream?.url)
        assertEquals("Streaming", domainAsset.liveStream?.status)
        assertEquals("folder1/folder2", domainAsset.folderTree)
    }

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
            asset.liveStream?.url
        )
        assertEquals("Completed", asset.liveStream?.status)
        assertEquals(true, asset.liveStream?.recordingEnabled)
        assertEquals(
            "The live stream has come to an end. Stay tuned, we'll have the recording ready for you shortly.",
            asset.liveStream?.noticeMessage
        )
    }
}