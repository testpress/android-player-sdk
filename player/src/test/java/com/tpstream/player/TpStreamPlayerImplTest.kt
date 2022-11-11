package com.tpstream.player

import android.content.Context
import androidx.media3.common.*
import androidx.media3.exoplayer.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TpStreamPlayerImplTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var player: ExoPlayer

    private lateinit var tpStreamPlayerImpl: TpStreamPlayerImpl

    @Before
    fun createPlayer() {
        player = ExoPlayer.Builder(mockContext).build()
        tpStreamPlayerImpl = TpStreamPlayerImpl(player)
    }

    @Test
    fun testSetPlayWhenReady() {
        assertEquals(4,4)
//        `when`(player.getPlayWhenReady()).thenReturn(true)
//        assertEquals(true,tpStreamPlayerImpl.getPlayWhenReady())
    }
//
//    @Test
//    fun testGetPlayWhenReady() {
//    }
//
//    @Test
//    fun getPlaybackState() {
//    }
//
//    @Test
//    fun getCurrentTime() {
//    }
//
//    @Test
//    fun getBufferedTime() {
//    }
//
//    @Test
//    fun setPlaybackSpeed() {
//    }
//
//    @Test
//    fun seekTo() {
//    }
//
//    @Test
//    fun release() {
//    }

}