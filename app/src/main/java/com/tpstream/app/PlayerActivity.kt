package com.tpstream.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.media3.common.*
import com.tpstream.player.*

const val TP_OFFLINE_PARAMS = "tp_offline_params"

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var tpStreamPlayer: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private lateinit var accessToken :String
    private lateinit var videoId :String
    private lateinit var orgCode :String
    private var parameters : TpInitParams? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        parameters = intent.getParcelableExtra(TP_OFFLINE_PARAMS)
        selectVideoParams(intent.getStringExtra("VideoParameter"))
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
        playerFragment.enableAutoFullScreenOnRotate()
        playerFragment.setOnInitializationListener(object: InitializationListener {
            override fun onInitializationSuccess(player: TpStreamPlayer) {
                play()
                player.setListener( object : TPStreamPlayerListener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d(TAG, "onPlaybackStateChanged: ${playbackState}")
                    }
                })
            }
        });
    }

    fun play(){
        if (parameters == null){
            parameters = TpInitParams.Builder()
                .setVideoId(videoId)
                .setAccessToken(accessToken)
                .setOrgCode(orgCode)
                .setAutoPlay(true)
                .enableDownloadSupport(true)
                .build()
        }
        playerFragment.load(parameters!!)

    }

    private fun selectVideoParams(videoType: String?){
        when(videoType){
            "DRM" -> {
                accessToken = "565a5b8c-310a-444b-956e-bbd6c7c74d7b"
                videoId = "d19729f0-8823-4805-9034-2a7ea9429195"
                orgCode = "edee9b"
            }
            "AES Encrypt" -> {
                accessToken = "143a0c71-567e-4ecd-b22d-06177228c25b"
                videoId = "o7pOsacWaJt"
                orgCode = "demoveranda"
            }
            "Clear" -> {
                accessToken = "d6af76a5-3fe2-4f68-adb5-53b65b6d094f"
                videoId = "9ObJCCwYApW"
                orgCode = "demoveranda"
            }
            null ->{}
        }
    }

}

