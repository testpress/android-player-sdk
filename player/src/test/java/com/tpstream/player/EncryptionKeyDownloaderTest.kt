package com.tpstream.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class EncryptionKeyDownloaderTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var encryptionKeyDownloader: EncryptionKeyDownloader

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        encryptionKeyDownloader = EncryptionKeyDownloader()

    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun getResponse() {
        val successResponse = MockResponse().setResponseCode(200).setBody("foo")
        mockWebServer.enqueue(successResponse)
        val response = encryptionKeyDownloader.getResponse(mockWebServer.url("/").toString())
        mockWebServer.takeRequest()
        assertEquals(response.body?.string(), "foo")
    }

    @Test
    fun getEncryptionKey() {
        // foo -> [102, 111, 111]
        val successResponse = MockResponse().setResponseCode(200).setBody("foo")
        mockWebServer.enqueue(successResponse)
        val response = encryptionKeyDownloader.getEncryptionKey(mockWebServer.url("/").toString())
        mockWebServer.takeRequest()
        assertEquals(response, "[102, 111, 111]")
    }
}