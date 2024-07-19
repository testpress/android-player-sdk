package com.tpstream.app

import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.tpstream.player.*
import com.tpstream.player.constants.PlaybackError
import com.tpstream.player.ui.InitializationListener
import com.tpstream.player.ui.TpStreamPlayerFragment

const val TP_OFFLINE_PARAMS = "tp_offline_params"

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var tpStreamPlayer: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private lateinit var accessToken :String
    private lateinit var videoId :String
    private var orgCode :String = "6eafqn"
    private var provider: TPStreamsSDK.Provider = TPStreamsSDK.Provider.TPStreams
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

                    override fun onAccessTokenExpired(videoId: String, callback: (String) -> Unit) {
                        val newAccessToken = getAccessToken(videoId)
                        callback.invoke(newAccessToken)
                    }

                    override fun onMarkerCallback(timesInSeconds: Long) {
                        Toast.makeText(this@PlayerActivity,"$timesInSeconds",Toast.LENGTH_SHORT).show()
                    }

                    override fun onFullScreenChanged(isFullScreen: Boolean) {
                        Toast.makeText(this@PlayerActivity, isFullScreen.toString(), Toast.LENGTH_SHORT).show()
                    }

                    override fun onPlayerError(playbackError: PlaybackError) {
                        super.onPlayerError(playbackError)
                        Log.d("TAG", "onPlayerError: ${playbackError}")
                    }
                })
            }
        });
        playerFragment.setPreferredFullscreenExitOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
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
                accessToken = "a4c04ca8-9c0e-4c9c-a889-bd3bf8ea586a"
                videoId = "ATJfRdHIUC9"
                orgCode = "lmsdemo"
                provider = TPStreamsSDK.Provider.TestPress
            }
            "TP_AES_Encrypt" -> {
                accessToken = "5f6355d0-62ac-4bfd-98ca-4a1e9a2857e3"
                videoId = "ZZb3S5OB3nY"
                orgCode = "lmsdemo"
                provider = TPStreamsSDK.Provider.TestPress
            }
            "TP_NON_DRM" -> {
                accessToken = "5c49285b-0557-4cef-b214-66034d0b77c3"
                videoId = "z1TLpfuZzXh"
                orgCode = "lmsdemo"
                provider = TPStreamsSDK.Provider.TestPress
            }
            "TPS_DRM" -> {
                accessToken = "ab70caed-6168-497f-89c1-1e308da2c9aa"
                videoId = "6suEBPy7EG4"
                orgCode = "6eafqn"
                provider = TPStreamsSDK.Provider.TPStreams
            }
            "TPS_NON_DRM" -> {
                accessToken = "48a481d0-7a7f-465f-9d18-86f52129430b"
                videoId = "C65BJzhj48k"
                orgCode = "dcek2m"
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
        findViewById<Button>(R.id.enter_full_screen).setOnClickListener {
            playerFragment.showFullScreen()
        }
    }

    fun getAccessToken(videoId: String): String {
        return when (videoId) {
            "ATJfRdHIUC9" -> "a4c04ca8-9c0e-4c9c-a889-bd3bf8ea586a"
            "ZZb3S5OB3nY" -> "5f6355d0-62ac-4bfd-98ca-4a1e9a2857e3"
            "z1TLpfuZzXh" -> "5c49285b-0557-4cef-b214-66034d0b77c3"
            "6suEBPy7EG4" -> "ab70caed-6168-497f-89c1-1e308da2c9aa"
            "C65BJzhj48k" -> "48a481d0-7a7f-465f-9d18-86f52129430b"
            else -> ""
        }
    }

}

