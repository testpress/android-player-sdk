package com.tpstream.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tpstream.player.models.DRMLicenseURL
import com.tpstream.player.models.VideoInfo
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
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
import java.net.URL

@RunWith(JUnit4::class)
class NetworkTest{
    lateinit var mockWebServer: MockWebServer
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val networkLicense:Network<DRMLicenseURL> = Network(DRMLicenseURL::class.java,"demo")
    private val networkVideoInfo:Network<VideoInfo> = Network(VideoInfo::class.java,"demo")

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun testGetVideoInfoWithSyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getContentJSON())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            val response = networkVideoInfo.get(mockWebServer.url("/").host)
            mockWebServer.takeRequest()
            assertEquals(response?.dashUrl, "Cohesion1 Example")
        }


    }

    @Test
    fun post() {
        val request = Request.Builder().url(mockWebServer.url("/")).build()
    }


}