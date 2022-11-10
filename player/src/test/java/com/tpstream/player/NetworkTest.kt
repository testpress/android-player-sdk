package com.tpstream.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tpstream.player.models.DRMLicenseURL
import com.tpstream.player.models.VideoInfo
import okhttp3.internal.EMPTY_REQUEST
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NetworkTest {
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val networkLicense: Network<DRMLicenseURL> = Network(DRMLicenseURL::class.java, "demo")
    private val networkVideoInfo: Network<VideoInfo> = Network(VideoInfo::class.java, "demo")

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun testGetVideoInfoWithSyncRequestOnSuccess() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getVideoInfoJSON())
        mockWebServer.enqueue(successResponse)
        val response = networkVideoInfo.get(mockWebServer.url("/").toString())
        mockWebServer.takeRequest()
        assertEquals(
            response?.dashUrl,
            "https://dyik8aq8drlno.cloudfront.net/institute/drm/courses/drm-encrypted-video/videos/transcoded/97912f63a2ad4e059bddbe2eb51fe3f6/video.mpd"
        )
        assertEquals(
            response?.hlsUrl,
            "https://dyik8aq8drlno.cloudfront.net/institute/drm/courses/drm-encrypted-video/videos/transcoded/97912f63a2ad4e059bddbe2eb51fe3f6/video.m3u8"
        )
        assertEquals(
            response?.url,
            "https://dyik8aq8drlno.cloudfront.net/institute/drm/courses/drm-encrypted-video/videos/transcoded/97912f63a2ad4e059bddbe2eb51fe3f6/"
        )
    }

    @Test
    fun testGetVideoInfoWithSyncRequestOnFailure() {
        val successResponse = MockResponse().setResponseCode(401)
        mockWebServer.enqueue(successResponse)
        try {
            val response = networkVideoInfo.get(mockWebServer.url("/").toString())
            mockWebServer.takeRequest()
        } catch (exception: TPException) {
            assertEquals(exception.isUnauthenticated(), true)
            assertEquals(exception.response?.code, 401)
        }
    }

    private fun getVideoInfoJSON(): String {
        return """
             {
    "title": "Big bunny video",
    "thumbnail": null,
    "thumbnail_small": null,
    "thumbnail_medium": null,
    "url": "https://dyik8aq8drlno.cloudfront.net/institute/drm/courses/drm-encrypted-video/videos/transcoded/97912f63a2ad4e059bddbe2eb51fe3f6/",
    "dash_url": "https://dyik8aq8drlno.cloudfront.net/institute/drm/courses/drm-encrypted-video/videos/transcoded/97912f63a2ad4e059bddbe2eb51fe3f6/video.mpd",
    "hls_url": "https://dyik8aq8drlno.cloudfront.net/institute/drm/courses/drm-encrypted-video/videos/transcoded/97912f63a2ad4e059bddbe2eb51fe3f6/video.m3u8",
    "duration": "0:10:34",
    "description": "",
    "transcoding_status": 4
}
        """.trimIndent()

    }

    @Test
    fun testPostDRmLicenseWithSyncRequestOnSuccess() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getDRMLicenseJSON())
        mockWebServer.enqueue(successResponse)
        val response = networkLicense.post(mockWebServer.url("/").toString(), EMPTY_REQUEST)
        mockWebServer.takeRequest()
        assertEquals(
            response?.licenseUrl,
            "https://drm.testpress.in/api/v2.5/chapter_contents/drm_license_key/?path=wv/eyJjb250ZW50QXV0aCI6ImV5SmpiMjUwWlc1MFNXUWlPaUkyT1dVeU1qTTRNV00xWXpRMFpqbGxZVGt6WldRNE1qaG1NekZtWWprM01TSXNJbVY0Y0dseVpYTWlPakUyTmpnd056VTVOemg5Iiwic2lnbmF0dXJlIjoiVm9oVWJRR1g2d1p0ZWxhSjoyMDIyMTExMFQwOTI2MTg5ODlaOmh5cV8yNmFRcm13a1lvN3ZJdVJad2M2U2dVSE9OOWFwTzVrTW5KOTR6aTg9In0=&v=1"
        )

    }

    @Test
    fun testPostDRmLicenseWithSyncRequestOnFailure() {
        val successResponse = MockResponse().setResponseCode(401)
        mockWebServer.enqueue(successResponse)
        try {
            val response = networkLicense.post(mockWebServer.url("/").toString(), EMPTY_REQUEST)
            mockWebServer.takeRequest()
        } catch (exception: TPException) {
            assertEquals(exception.isUnauthenticated(), true)
            assertEquals(exception.response?.code, 401)
        }
    }

    private fun getDRMLicenseJSON(): String {
        return """
            {"license_url":"https://drm.testpress.in/api/v2.5/chapter_contents/drm_license_key/?path=wv/eyJjb250ZW50QXV0aCI6ImV5SmpiMjUwWlc1MFNXUWlPaUkyT1dVeU1qTTRNV00xWXpRMFpqbGxZVGt6WldRNE1qaG1NekZtWWprM01TSXNJbVY0Y0dseVpYTWlPakUyTmpnd056VTVOemg5Iiwic2lnbmF0dXJlIjoiVm9oVWJRR1g2d1p0ZWxhSjoyMDIyMTExMFQwOTI2MTg5ODlaOmh5cV8yNmFRcm13a1lvN3ZJdVJad2M2U2dVSE9OOWFwTzVrTW5KOTR6aTg9In0=&v=1"}
        """.trimIndent()
    }

}