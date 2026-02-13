package com.tpstream.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.internal.EMPTY_REQUEST
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
    fun isErrorReceiveOnGetSyncRequest() {
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

    @Test
    fun isErrorReceiveOnGetAsyncRequest() {
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
    fun isDataReceiveOnPostSyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getDataJSON())
        mockWebServer.enqueue(successResponse)
        val response = network.post(mockWebServer.url("/").toString(), EMPTY_REQUEST)
        mockWebServer.takeRequest()
        assertEquals(response?.data, "foo")
    }

    @Test
    fun isErrorReceiveOnPostSyncRequest() {
        val errorResponse = MockResponse().setResponseCode(400)
        mockWebServer.enqueue(errorResponse)
        try {
            network.post(mockWebServer.url("/").toString(), EMPTY_REQUEST)
            mockWebServer.takeRequest()
        } catch (exception: TPException) {
            assertEquals(exception.response?.code, 400)
        }
    }

    @Test
    fun isDataReceiveOnPostAsyncRequest() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getDataJSON())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            network.post(mockWebServer.url("/").toString(), EMPTY_REQUEST, callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        assertEquals(callbackResult.data, "foo")
    }

    @Test
    fun isErrorReceiveOnPostAsyncRequest() {
        val errorResponse = MockResponse().setResponseCode(400)
        mockWebServer.enqueue(errorResponse)
        runBlocking {
            network.post(mockWebServer.url("/").toString(), EMPTY_REQUEST, callbackResponse)
            mockWebServer.takeRequest()
            delay(50)
        }
        assertEquals(callbackException.response?.code, 400)
    }

    private fun getDataJSON(): String {
        return """
            {"data":"foo"}
        """.trimIndent()
    }
}