package com.tpstream.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.annotations.SerializedName
import com.tpstream.player.models.DRMLicenseURL
import com.tpstream.player.models.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    data class Foo(@SerializedName("data") val data: String)

    private lateinit var mockWebServer: MockWebServer
    private val network: Network<Foo> = Network(Foo::class.java, "demo")

    lateinit var callbackResponse: Network.TPResponse<Foo>
    lateinit var callbackResult: Foo
    lateinit var callbackException: TPException

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        runBlocking {
            callbackResponse = object : Network.TPResponse<Foo> {
                override fun onSuccess(result: Foo) {
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
    fun isDataReceiveOnGetSyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getDataJSON())
        mockWebServer.enqueue(successResponse)
        val response = network.get(mockWebServer.url("/").toString())
        mockWebServer.takeRequest()
        assertEquals(response?.data, "foo")
    }

    @Test
    fun isErrorReceiveOnGetAsyncRequest() {
        val successResponse = MockResponse().setResponseCode(400)
        mockWebServer.enqueue(successResponse)
        try {
            val response = network.get(mockWebServer.url("/").toString())
            mockWebServer.takeRequest()
        } catch (exception: TPException) {
            assertEquals(exception.response?.code, 400)
        }
    }

    @Test
    fun isDataReceiveOnGetAsyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getDataJSON())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            network.get(mockWebServer.url("/").toString(), callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        assertEquals(callbackResult.data, "foo")
    }
//
//    @Test
//    fun testGetVideoInfoWithAsyncRequestOnFailure() {
//        val successResponse = MockResponse().setResponseCode(401)
//        mockWebServer.enqueue(successResponse)
//        var tpException: TPException? = null
//        runBlocking {
//            withContext(Dispatchers.IO) {
//                val response = networkVideoInfo.get(mockWebServer.url("/").toString(),
//                    object : Network.TPResponse<VideoInfo> {
//                        override fun onSuccess(result: VideoInfo) {
//
//                        }
//
//                        override fun onFailure(exception: TPException) {
//                            tpException = exception
//                        }
//
//                    })
//                Thread.sleep(500)
//            }
//            mockWebServer.takeRequest()
//        }
//        assertEquals(tpException?.response?.code, 401)
//        assertEquals(tpException?.isUnauthenticated(), true)
//
//    }
//
//    @Test
//    fun testPostDRmLicenseWithSyncRequestOnSuccess() {
//        val successResponse = MockResponse().setResponseCode(200).setBody(getDRMLicenseJSON())
//        mockWebServer.enqueue(successResponse)
//        val response = networkLicense.post(mockWebServer.url("/").toString(), EMPTY_REQUEST)
//        mockWebServer.takeRequest()
//        assertEquals(
//            response?.licenseUrl,
//            "https://drm.testpress.in/api/v2.5/chapter_contents/drm_license_key/?path=wv/eyJjb250ZW50QXV0aCI6ImV5SmpiMjUwWlc1MFNXUWlPaUkyT1dVeU1qTTRNV00xWXpRMFpqbGxZVGt6WldRNE1qaG1NekZtWWprM01TSXNJbVY0Y0dseVpYTWlPakUyTmpnd056VTVOemg5Iiwic2lnbmF0dXJlIjoiVm9oVWJRR1g2d1p0ZWxhSjoyMDIyMTExMFQwOTI2MTg5ODlaOmh5cV8yNmFRcm13a1lvN3ZJdVJad2M2U2dVSE9OOWFwTzVrTW5KOTR6aTg9In0=&v=1"
//        )
//
//    }
//
//    @Test
//    fun testPostDRmLicenseWithSyncRequestOnFailure() {
//        val successResponse = MockResponse().setResponseCode(401)
//        mockWebServer.enqueue(successResponse)
//        try {
//            val response = networkLicense.post(mockWebServer.url("/").toString(), EMPTY_REQUEST)
//            mockWebServer.takeRequest()
//        } catch (exception: TPException) {
//            assertEquals(exception.isUnauthenticated(), true)
//            assertEquals(exception.response?.code, 401)
//        }
//    }
//
//    @Test
//    fun testPostDRmLicenseWithAsyncRequestOnSuccess() {
//        val successResponse = MockResponse().setResponseCode(200).setBody(getDRMLicenseJSON())
//        mockWebServer.enqueue(successResponse)
//        var response: DRMLicenseURL?
//        runBlocking {
//            withContext(Dispatchers.IO) {
//                response = networkLicense.post(mockWebServer.url("/").toString(), EMPTY_REQUEST,
//                    object : Network.TPResponse<DRMLicenseURL> {
//                        override fun onSuccess(result: DRMLicenseURL) {
//                            response = result
//                        }
//
//                        override fun onFailure(exception: TPException) {
//
//                        }
//
//                    })
//                Thread.sleep(500)
//            }
//            mockWebServer.takeRequest()
//        }
//        assertEquals(
//            response?.licenseUrl,
//            "https://drm.testpress.in/api/v2.5/chapter_contents/drm_license_key/?path=wv/eyJjb250ZW50QXV0aCI6ImV5SmpiMjUwWlc1MFNXUWlPaUkyT1dVeU1qTTRNV00xWXpRMFpqbGxZVGt6WldRNE1qaG1NekZtWWprM01TSXNJbVY0Y0dseVpYTWlPakUyTmpnd056VTVOemg5Iiwic2lnbmF0dXJlIjoiVm9oVWJRR1g2d1p0ZWxhSjoyMDIyMTExMFQwOTI2MTg5ODlaOmh5cV8yNmFRcm13a1lvN3ZJdVJad2M2U2dVSE9OOWFwTzVrTW5KOTR6aTg9In0=&v=1"
//        )
//    }
//
//    @Test
//    fun testPostDRmLicenseWithAsyncRequestOnFailure() {
//        val successResponse = MockResponse().setResponseCode(401)
//        mockWebServer.enqueue(successResponse)
//        var tpException: TPException? = null
//        runBlocking {
//            withContext(Dispatchers.IO) {
//                val response = networkLicense.post(mockWebServer.url("/").toString(), EMPTY_REQUEST,
//                    object : Network.TPResponse<DRMLicenseURL> {
//                        override fun onSuccess(result: DRMLicenseURL) {
//                        }
//
//                        override fun onFailure(exception: TPException) {
//                            tpException = exception
//                        }
//
//                    })
//                Thread.sleep(500)
//            }
//            mockWebServer.takeRequest()
//        }
//        assertEquals(tpException?.isUnauthenticated(), true)
//        assertEquals(tpException?.response?.code, 401)
//    }
//
//    private fun getVideoInfoJSON(): String {
//        return """
//             {
//    "title": "Big bunny video",
//    "thumbnail": null,
//    "thumbnail_small": null,
//    "thumbnail_medium": null,
//    "url": "https://dyik8aq8drlno.cloudfront.net/institute/drm/courses/drm-encrypted-video/videos/transcoded/97912f63a2ad4e059bddbe2eb51fe3f6/",
//    "dash_url": "https://dyik8aq8drlno.cloudfront.net/institute/drm/courses/drm-encrypted-video/videos/transcoded/97912f63a2ad4e059bddbe2eb51fe3f6/video.mpd",
//    "hls_url": "https://dyik8aq8drlno.cloudfront.net/institute/drm/courses/drm-encrypted-video/videos/transcoded/97912f63a2ad4e059bddbe2eb51fe3f6/video.m3u8",
//    "duration": "0:10:34",
//    "description": "",
//    "transcoding_status": 4
//}
//        """.trimIndent()
//
//    }

    private fun getDataJSON(): String {
        return """
            {"data":"foo"}
        """.trimIndent()
    }

}