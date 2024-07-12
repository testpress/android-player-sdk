package com.tpstream.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.util.DebugTextViewHelper
import com.tpstream.player.TPStreamPlayerListener
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import com.tpstream.player.TpStreamPlayer
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
    private var debugTextView: TextView? = null
    private var debugViewHelper: DebugTextViewHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        parameters = intent.getParcelableExtra(TP_OFFLINE_PARAMS)
        selectVideoParams(intent.getStringExtra("VideoParameter"))
        TPStreamsSDK.initialize(provider, orgCode)
        debugTextView = findViewById(R.id.debug_text_view);
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
        playerFragment.enableAutoFullScreenOnRotate()
        playerFragment.setOnInitializationListener(object: InitializationListener {
            override fun onInitializationSuccess(player: TpStreamPlayer) {
                tpStreamPlayer = player
                tpStreamPlayer.load(buildParams())
                initDebuggLogger(tpStreamPlayer.getExoplayer())
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
        playerFragment.setPreferredFullscreenExitOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
        initializeSampleButtons();
    }

    fun initDebuggLogger(player: ExoPlayer){
        debugViewHelper = DebugTextViewHelper(player, debugTextView!!)
        debugViewHelper?.start()
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
                accessToken = "11a527f0-ab37-49c3-838a-5e712ccfe8c3"
                videoId = "65aTNSEBuNG"
                orgCode = "m9n4m6"
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

    override fun onDestroy() {
        super.onDestroy()
        debugViewHelper?.stop()
        debugViewHelper = null
    }

}

