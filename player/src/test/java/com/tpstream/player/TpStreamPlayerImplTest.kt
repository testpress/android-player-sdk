package com.tpstream.player

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TpStreamPlayerImplTest{

    @Mock
    lateinit var player:ExoPlayer
    @Mock
    lateinit var tpStreamPlayerImpl:TpStreamPlayerImpl
    lateinit var context: Context

    @Before
    fun createPlayer(){
        context = ApplicationProvider.getApplicationContext()
        player = ExoPlayer.Builder(context).build()
        tpStreamPlayerImpl = TpStreamPlayerImpl(player)
    }

//    @Test
//    fun testSetPlayWhenReady() {
//        `when`(tpStreamPlayerImpl.getPlayWhenReady()).thenReturn(true)
//        assertEquals(true,tpStreamPlayerImpl.getPlayWhenReady())
//    }
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