package com.tpstream.app

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.tpstream.player.*
import com.tpstream.player.enum.PlaybackError
import com.tpstream.player.ui.InitializationListener
import com.tpstream.player.ui.TpStreamPlayerFragment

const val TP_OFFLINE_PARAMS = "tp_offline_params"

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var tpStreamPlayer: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private lateinit var videoId :String
    private lateinit var orgCode :String
    private lateinit var provider: TPStreamsSDK.Provider
    private var parameters : TpInitParams? = null
    private lateinit var authToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        parameters = intent.getParcelableExtra(TP_OFFLINE_PARAMS)
        selectVideoParams(intent.getStringExtra("VideoParameter"))
        TPStreamsSDK.initialize(provider, orgCode, authToken)
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

                    override fun onPlayerError(playbackError: PlaybackError) {
                        super.onPlayerError(playbackError)
                        Log.d("TAG", "onPlayerError: ${playbackError}")
                    }
                })
            }
        });
        initializeSampleButtons();
    }

    fun buildParams(): TpInitParams {
        if (parameters == null) {
            parameters = TpInitParams.Builder()
                .setVideoId(videoId)
                .setAutoPlay(true)
                .enableDownloadSupport(true)
                .build()
        }
        return parameters!!
    }

    private fun selectVideoParams(videoType: String?){
        when(videoType){
            "TP_DRM" -> {
                videoId = "ATJfRdHIUC9"
                orgCode = "lmsdemo"
                provider = TPStreamsSDK.Provider.TestPress
                authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6MiwidXNlcl9pZCI6MiwiaW5zdGl0dXRlIjoxMSwiaWQiOjIsImV4cCI6MTcwNTQ5NzM1NCwiZW1haWwiOiIxMjJAZ21haWwuY29tIn0.0l3cyZClu480VOpkF0XM3NQcEKBXPyXPUikomEx0m1M"
            }
            "TP_AES_Encrypt" -> {
                videoId = "ZZb3S5OB3nY"
                orgCode = "lmsdemo"
                provider = TPStreamsSDK.Provider.TestPress
                authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6MiwidXNlcl9pZCI6MiwiaW5zdGl0dXRlIjoxMSwiaWQiOjIsImV4cCI6MTcwNTQ5NzM1NCwiZW1haWwiOiIxMjJAZ21haWwuY29tIn0.0l3cyZClu480VOpkF0XM3NQcEKBXPyXPUikomEx0m1M"
            }
            "TP_NON_DRM" -> {
                videoId = "z1TLpfuZzXh"
                orgCode = "lmsdemo"
                provider = TPStreamsSDK.Provider.TestPress
                authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6MiwidXNlcl9pZCI6MiwiaW5zdGl0dXRlIjoxMSwiaWQiOjIsImV4cCI6MTcwNTQ5NzM1NCwiZW1haWwiOiIxMjJAZ21haWwuY29tIn0.0l3cyZClu480VOpkF0XM3NQcEKBXPyXPUikomEx0m1M"
            }
            "TPS_DRM" -> {
                videoId = "6suEBPy7EG4"
                orgCode = "6eafqn"
                provider = TPStreamsSDK.Provider.TPStreams
                authToken = "7ac134707a29077369a40a5ea99c3342e51d1cf64bddef5bcee31031e0b1f346"
            }
            "TPS_NON_DRM" -> {
                videoId = "5X3sT3UXyNY"
                orgCode = "6eafqn"
                provider = TPStreamsSDK.Provider.TPStreams
                authToken = "7ac134707a29077369a40a5ea99c3342e51d1cf64bddef5bcee31031e0b1f346"
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

}

