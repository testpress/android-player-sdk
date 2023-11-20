package com.tpstream.app

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.tpstream.player.*
import com.tpstream.player.ui.InitializationListener
import com.tpstream.player.ui.TpStreamPlayerFragment

const val TP_OFFLINE_PARAMS = "tp_offline_params"

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var tpStreamPlayer: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private lateinit var accessToken :String
    private lateinit var videoId :String
    private lateinit var orgCode :String
    private lateinit var provider: TPStreamsSDK.Provider
    private var parameters : TpInitParams? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        parameters = intent.getParcelableExtra(TP_OFFLINE_PARAMS)
        selectVideoParams(intent.getStringExtra("VideoParameter"))
        TPStreamsSDK.initialize(provider, orgCode)
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
        playerFragment.enableAutoFullScreenOnRotate()
        playerFragment.setOnInitializationListener(object: InitializationListener {
            override fun onInitializationSuccess(player: TpStreamPlayer) {
                tpStreamPlayer = player
                tpStreamPlayer.load(buildParams())
                tpStreamPlayer.setListener( object : TPStreamPlayerListener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                    }

                    override fun onMarkerCallback(timesInSeconds: Long) {
                        Toast.makeText(this@PlayerActivity,"$timesInSeconds",Toast.LENGTH_SHORT).show()
                    }

                    override fun onFullScreenChanged(isFullScreen: Boolean) {
                        Toast.makeText(this@PlayerActivity, isFullScreen.toString(), Toast.LENGTH_SHORT).show()
                    }
                })

                playerFragment.tpStreamPlayerView.setMarkers(longArrayOf(60,120,180),Color.RED,false)
                playerFragment.tpStreamPlayerView.enableWaterMark("Tpstreams",Color.RED)
            }
        });
        initializeSampleButtons();
    }

    fun buildParams(): TpInitParams {
        if (parameters == null) {
            parameters = TpInitParams.Builder()
                .setVideoId(videoId)
                .setAccessToken(accessToken)
                .setAutoPlay(true)
                .enableDownloadSupport(true)
                .build()
        }
        return parameters!!
    }

    private fun selectVideoParams(videoType: String?){
        when(videoType){
            "TP_DRM" -> {
                accessToken = "c381512b-7337-4d8e-a8cf-880f4f08fd08"
                videoId = "C3XLe1CCcOq"
                orgCode = "demoveranda"
                provider = TPStreamsSDK.Provider.TestPress
            }
            "TP_AES_Encrypt" -> {
                accessToken = "143a0c71-567e-4ecd-b22d-06177228c25b"
                videoId = "o7pOsacWaJt"
                orgCode = "demoveranda"
                provider = TPStreamsSDK.Provider.TestPress
            }
            "TP_NON_DRM" -> {
                accessToken = "70f61402-3724-4ed8-99de-5473b2310efe"
                videoId = "qJQlWGLJvNv"
                orgCode = "demoveranda"
                provider = TPStreamsSDK.Provider.TestPress
            }
            "TPS_DRM" -> {
                accessToken = "565a5b8c-310a-444b-956e-bbd6c7c74d7b"
                videoId = "d19729f0-8823-4805-9034-2a7ea9429195"
                orgCode = "edee9b"
                provider = TPStreamsSDK.Provider.TPStreams
            }
            "TPS_NON_DRM" -> {
                accessToken = "4b11bf9e-d6b7-4b1f-80b8-19d92b26e966"
                videoId = "73633fa3-61c6-443c-b625-ac4e85b28cfc"
                orgCode = "edee9b"
                provider = TPStreamsSDK.Provider.TPStreams
            }
            null ->{}
        }
    }

    private fun initializeSampleButtons() {
        findViewById<Button>(R.id.sample_play).setOnClickListener {
            tpStreamPlayer.play()
        }
        findViewById<Button>(R.id.sample_pause).setOnClickListener {
            tpStreamPlayer.pause()
        }
    }

}

