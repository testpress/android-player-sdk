package com.tpstream.player

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import com.tpstream.player.util.DeviceUtil
import com.tpstream.player.util.DeviceUtil.Companion.toCodecDetails
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class DeviceUtilTest {

    private lateinit var mockCodecList: MediaCodecList
    private lateinit var mockCodecInfo: MediaCodecInfo
    private lateinit var mockCapabilities: MediaCodecInfo.CodecCapabilities
    private lateinit var mockVideoCapabilities: MediaCodecInfo.VideoCapabilities

    @Before
    fun setup() {
        mockCodecList = Mockito.mock(MediaCodecList::class.java)
        mockCodecInfo = Mockito.mock(MediaCodecInfo::class.java)
        mockCapabilities = Mockito.mock(MediaCodecInfo.CodecCapabilities::class.java)
        mockVideoCapabilities = Mockito.mock(MediaCodecInfo.VideoCapabilities::class.java)
    }

    @Test
    fun testGetAvailableAVCCodecs_success() {
        `when`(mockCodecInfo.name).thenReturn("dummyCodec")
        `when`(mockCodecInfo.isEncoder).thenReturn(false)
        `when`(mockCodecInfo.supportedTypes).thenReturn(arrayOf("video/avc"))
        `when`(mockCodecInfo.getCapabilitiesForType("video/avc")).thenReturn(mockCapabilities)
        `when`(mockCodecInfo.getCapabilitiesForType("video/avc").videoCapabilities).thenReturn(mockVideoCapabilities)
        `when`(mockVideoCapabilities.isSizeSupported(1920, 1080)).thenReturn(true)
        `when`(mockVideoCapabilities.isSizeSupported(3840, 2160)).thenReturn(false)
        `when`(mockVideoCapabilities.areSizeAndRateSupported(1920, 1080, 48.0)).thenReturn(true)
        `when`(mockVideoCapabilities.areSizeAndRateSupported(3840, 2160, 48.0)).thenReturn(false)
        `when`(mockCodecList.codecInfos).thenReturn(arrayOf(mockCodecInfo))

        val codecs = DeviceUtil.getAvailableAVCCodecs(mockCodecList)

        assertEquals(1, codecs.size)
        assertEquals(true, codecs[0].is1080pSupported)
    }

    @Test
    fun testGetAvailableAVCCodecs_emptyCodecInfos() {
        `when`(mockCodecList.codecInfos).thenReturn(arrayOf())
        val codecs = DeviceUtil.getAvailableAVCCodecs(mockCodecList)
        Assert.assertTrue(codecs.isEmpty())
    }

    @Test
    fun testGetAvailableAVCCodecs_noAVCCodecs() {
        `when`(mockCodecInfo.isEncoder).thenReturn(false)
        `when`(mockCodecInfo.supportedTypes).thenReturn(arrayOf("video/hevc"))
        `when`(mockCodecList.codecInfos).thenReturn(arrayOf(mockCodecInfo))
        val codecs = DeviceUtil.getAvailableAVCCodecs(mockCodecList)
        Assert.assertTrue(codecs.isEmpty())
    }

    @Test
    fun testGetAvailableAVCCodecs_encoderOnly() {
        `when`(mockCodecInfo.isEncoder).thenReturn(true)
        `when`(mockCodecList.codecInfos).thenReturn(arrayOf(mockCodecInfo))
        val codecs = DeviceUtil.getAvailableAVCCodecs(mockCodecList)
        Assert.assertTrue(codecs.isEmpty())
    }

    @Test
    fun testGetAvailableAVCCodecs_nullCapabilities() {
        `when`(mockCodecInfo.isEncoder).thenReturn(false)
        `when`(mockCodecInfo.supportedTypes).thenReturn(arrayOf("video/avc"))
        `when`(mockCodecInfo.getCapabilitiesForType("video/avc")).thenReturn(null)
        `when`(mockCodecList.codecInfos).thenReturn(arrayOf(mockCodecInfo))
        val codecs = DeviceUtil.getAvailableAVCCodecs(mockCodecList)
        Assert.assertTrue(codecs.isEmpty())
    }

    @Test
    fun toCodecDetails_allSupported() {
        `when`(mockCodecInfo.name).thenReturn("dummyCodec")
        `when`(mockCodecInfo.isEncoder).thenReturn(false)
        `when`(mockCodecInfo.supportedTypes).thenReturn(arrayOf("video/avc"))
        `when`(mockCodecInfo.getCapabilitiesForType("video/avc")).thenReturn(mockCapabilities)
        `when`(mockCodecInfo.getCapabilitiesForType("video/avc").videoCapabilities).thenReturn(mockVideoCapabilities)
        `when`(mockVideoCapabilities.isSizeSupported(1920, 1080)).thenReturn(true)
        `when`(mockVideoCapabilities.isSizeSupported(3840, 2160)).thenReturn(true)
        `when`(mockVideoCapabilities.areSizeAndRateSupported(1920, 1080, 48.0)).thenReturn(true)
        `when`(mockVideoCapabilities.areSizeAndRateSupported(3840, 2160, 48.0)).thenReturn(true)

        val codecDetails = mockCodecInfo.toCodecDetails()

        Assert.assertTrue(codecDetails!!.is1080pSupported)
        Assert.assertTrue(codecDetails.is4KSupported)
        Assert.assertTrue(codecDetails.is1080pAt2xSupported)
        Assert.assertTrue(codecDetails.is4KAt2xSupported)
    }

    @Test
    fun toCodecDetails_noneSupported() {
        `when`(mockCodecInfo.name).thenReturn("dummyCodec")
        `when`(mockCodecInfo.isEncoder).thenReturn(false)
        `when`(mockCodecInfo.supportedTypes).thenReturn(arrayOf("video/avc"))
        `when`(mockCodecInfo.getCapabilitiesForType("video/avc")).thenReturn(mockCapabilities)
        `when`(mockCodecInfo.getCapabilitiesForType("video/avc").videoCapabilities).thenReturn(mockVideoCapabilities)
        `when`(mockVideoCapabilities.isSizeSupported(1920, 1080)).thenReturn(false)
        `when`(mockVideoCapabilities.isSizeSupported(3840, 2160)).thenReturn(false)
        `when`(mockVideoCapabilities.areSizeAndRateSupported(1920, 1080, 48.0)).thenReturn(false)
        `when`(mockVideoCapabilities.areSizeAndRateSupported(3840, 2160, 48.0)).thenReturn(false)

        val codecDetails = mockCodecInfo.toCodecDetails()

        Assert.assertTrue(!codecDetails!!.is1080pSupported)
        Assert.assertTrue(!codecDetails.is4KSupported)
        Assert.assertTrue(!codecDetails.is1080pAt2xSupported)
        Assert.assertTrue(!codecDetails.is4KAt2xSupported)
    }
}