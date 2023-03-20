package com.tpstream.player

import android.content.Context
import androidx.media3.common.*
import androidx.media3.exoplayer.*
import androidx.media3.exoplayer.trackselection.TrackSelector
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TpStreamPlayerImplTest {

    private val PLAYER_STATE = 1
    private val PLAYER_CURRENT_POSOTION = 1000L
    private val PLAYER_BUFFER_TIMING = 1000L
    private val PLAYER_SEEK_TIME = 1000L
    private val PLAYBACK_SPEET_TIME = 10.00F
    private val VIDEO_DURATION = 10000L
    private val format = Format.Builder()
        .setHeight(1080)
        .build()

    @Mock
    private lateinit var player: ExoPlayer
    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var trackSelectionParameters :TrackSelectionParameters
    @Mock
    private lateinit var trackSelector: TrackSelector
    private lateinit var tpStreamPlayerImpl: TpStreamPlayerImpl
    private var called = false

    @Before
    fun createPlayer() {
        tpStreamPlayerImpl = TpStreamPlayerImpl(context)
        tpStreamPlayerImpl.exoPlayer = player

    }

    @Test
    fun testSetPlayWhenReady() {
        called = false
        `when`(player.setPlayWhenReady(true)).then { isCalled() }
        tpStreamPlayerImpl.setPlayWhenReady(true)
        assertEquals(true, called)
    }

    @Test
    fun testGetPlayWhenReady() {
        `when`(player.playWhenReady).thenReturn(true)
        assertEquals(true, tpStreamPlayerImpl.getPlayWhenReady())
    }

    @Test
    fun testGetPlaybackState() {
        `when`(player.playbackState).thenReturn(PLAYER_STATE)
        assertEquals(PLAYER_STATE, tpStreamPlayerImpl.getPlaybackState())
    }

    @Test
    fun testGetCurrentTime() {
        `when`(player.currentPosition).thenReturn(PLAYER_CURRENT_POSOTION)
        assertEquals(PLAYER_CURRENT_POSOTION, tpStreamPlayerImpl.getCurrentTime())
    }

    @Test
    fun testGetBufferedTime() {
        `when`(player.bufferedPosition).thenReturn(PLAYER_BUFFER_TIMING)
        assertEquals(PLAYER_BUFFER_TIMING, tpStreamPlayerImpl.getBufferedTime())
    }

    @Test
    fun testGetVideoFormat() {
        `when`(player.videoFormat).thenReturn(format)
        assertEquals(format.height, tpStreamPlayerImpl.getVideoFormat()?.height)
    }

    @Test
    fun testGetDuration() {
        `when`(player.duration).thenReturn(VIDEO_DURATION)
        assertEquals(VIDEO_DURATION, tpStreamPlayerImpl.getDuration())
    }


    @Test
    fun testSetPlaybackSpeed() {
        called = false
        `when`(player.setPlaybackSpeed(PLAYBACK_SPEET_TIME)).then { isCalled() }
        tpStreamPlayerImpl.setPlaybackSpeed(PLAYBACK_SPEET_TIME)
        assertEquals(true, called)
    }

    @Test
    fun testSeekTo() {
        called = false
        `when`(player.seekTo(PLAYER_SEEK_TIME)).then { isCalled() }
        tpStreamPlayerImpl.seekTo(PLAYER_SEEK_TIME)
        assertEquals(true, called)
    }

    @Test
    fun testRelease() {
        called = false
        `when`(player.release()).then { isCalled() }
        tpStreamPlayerImpl.release()
        assertEquals(true, called)
    }

    private fun isCalled() {
        called = true
    }

    @Test
    fun testSetTrackSelectionParameters(){
        called = false
        `when`(player.setTrackSelectionParameters(trackSelectionParameters)).then { isCalled() }
        tpStreamPlayerImpl.setTrackSelectionParameters(trackSelectionParameters)
        assertEquals(true, called)
    }

    @Test
    fun testGetTrackSelectionParameters(){
        called = false
        `when`(player.trackSelectionParameters).thenReturn(trackSelectionParameters)
        assertEquals(
            trackSelectionParameters.hashCode(),
            tpStreamPlayerImpl.getTrackSelectionParameters().hashCode()
        )
    }

    @Test
    fun testGetTrackSelector(){
        called = false
        `when`(player.trackSelector).thenReturn(trackSelector)
        assertEquals(
            trackSelector.hashCode(),
            tpStreamPlayerImpl.getTrackSelector().hashCode()
        )
    }
}