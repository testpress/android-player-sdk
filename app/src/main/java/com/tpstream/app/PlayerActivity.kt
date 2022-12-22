package com.tpstream.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.common.*
import com.tpstream.player.*

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var tpStreamPlayer: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private lateinit var accessToken :String
    private lateinit var videoId :String
    private lateinit var orgCode :String
    private lateinit var parameters : TpInitParams

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        selectVideoParams(intent.getStringExtra("VideoParameter")!!)
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
        playerFragment.enableAutoFullScreenOnRotate()
        playerFragment.setOnInitializationListener(object: InitializationListener {
            override fun onInitializationSuccess(player: TpStreamPlayer) {
                play()
            }
        });
        playerFragment.playbackStateListener = object : TPPlayerListener {
            override fun onTracksChanged(tracks: Tracks) {
            }

            override fun onMetadata(metadata: Metadata) {
            }

            override fun onIsPlayingChanged(playing: Boolean) {
            }

            override fun onIsLoadingChanged(loading: Boolean) {
            }

            override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            }

            override fun onEvents(player: TpStreamPlayer?, events: Player.Events) {
            }

            override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
            }

            override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
            }

            override fun onPlayerError(error: PlaybackException) {
            }
        }
    }

    fun play(){
        parameters = TpInitParams.Builder()
            .setVideoId(videoId)
            .setAccessToken(accessToken)
            .setOrgCode(orgCode)
            .setAutoPlay(true)
            .startAt(20L)
            .enableDownloadSupport(true)
            .build()
        playerFragment.load(parameters)

    }

    private fun selectVideoParams(videoType: String){
        when(videoType){
            "DRM" -> {
                accessToken = "1630b77c-a5c4-4813-bf3e-8f6b9dde8e48"
                videoId = "vDTimhUHaJQ"
                orgCode = "demoveranda"
            }
            "AES Encrypt" -> {
                accessToken = "143a0c71-567e-4ecd-b22d-06177228c25b"
                videoId = "o7pOsacWaJt"
                orgCode = "demoveranda"
            }
            "Clear" -> {
                accessToken = "f85035fe-7b7b-4e4d-bcf8-5151e385dc95"
                videoId = "r8W3kSNVj0v"
                orgCode = "demoveranda"
            }
        }
    }

}

