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
    private lateinit var authToken: String
    private lateinit var videoId :String
    private lateinit var orgCode :String
    private lateinit var provider: TPStreamsSDK.Provider
    private var parameters : TpInitParams? = null

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
                videoId = TestpressSample.DRM_VIDEO
                orgCode = TestpressSample.ORG_CODE
                provider = TPStreamsSDK.Provider.TestPress
                authToken = TestpressSample.AUTH_TOKEN
            }
            "TP_AES_Encrypt" -> {
                videoId = TestpressSample.AES_VIDEO
                orgCode = TestpressSample.ORG_CODE
                provider = TPStreamsSDK.Provider.TestPress
                authToken = TestpressSample.AUTH_TOKEN
            }
            "TP_NON_DRM" -> {
                videoId = TestpressSample.NON_DRM_VIDEO
                orgCode = TestpressSample.ORG_CODE
                provider = TPStreamsSDK.Provider.TestPress
                authToken = TestpressSample.AUTH_TOKEN
            }
            "TPS_DRM" -> {
                videoId = TPStreamsSample.DRM_VIDEO
                orgCode = TPStreamsSample.ORG_CODE
                provider = TPStreamsSDK.Provider.TPStreams
                authToken = TPStreamsSample.AUTH_TOKEN
            }
            "TPS_NON_DRM" -> {
                videoId = TPStreamsSample.NON_DRM_VIDEO
                orgCode = TPStreamsSample.ORG_CODE
                provider = TPStreamsSDK.Provider.TPStreams
                authToken = TPStreamsSample.AUTH_TOKEN
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

