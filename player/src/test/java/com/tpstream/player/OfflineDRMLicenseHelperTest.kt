package com.tpstream.player

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.offline.DownloadHelper
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class OfflineDRMLicenseHelperTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    private lateinit var mockWebServer: MockWebServer

    @Mock
    private lateinit var context: Context

    private lateinit var downloadHelper: DownloadHelper

    private val accessToken = "c381512b-7337-4d8e-a8cf-880f4f08fd08"
    private val videoId = "C3XLe1CCcOq"
    private val orgCode = "demoveranda"

    private val tpInitParams = TpInitParams.Builder()
        .setAccessToken(accessToken)
        .setVideoId(videoId)
        .setOrgCode(orgCode)
        .build()

    private var format = Format.Builder()
        .setHeight(1080)
        .setWidth(1920)
        .build()

    private lateinit var newLicenseKey: ByteArray

    private var drmLicenseFetchCallback = object : DRMLicenseFetchCallback {
        override fun onLicenseFetchSuccess(keySetId: ByteArray) {
            newLicenseKey = keySetId
        }

        override fun onLicenseFetchFailure() {

        }

    }

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        downloadHelper = DownloadHelper(
            MediaItem.EMPTY,
            null,
            DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS_WITHOUT_CONTEXT,
            arrayOf()
        )
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun isFetchDRMLicence() {

        OfflineDRMLicenseHelper.fetchLicense(
            context,
            tpInitParams,
            downloadHelper,
            drmLicenseFetchCallback
        )

        Mockito.`when`(OfflineDRMLicenseHelper.getKetSetID(format)).thenReturn(byteArrayOf(123))

        Assert.assertEquals(byteArrayOf(123), newLicenseKey)
    }


}