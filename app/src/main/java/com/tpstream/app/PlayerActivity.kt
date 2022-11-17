package com.tpstream.app

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.tpstream.player.InitializationListener
import com.tpstream.player.TpInitParams
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.TpStreamPlayerFragment

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var player: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private val accessToken = "bbc6f451-7a61-4670-b481-b74dfcf453a5"
    private val videoId = "phapsu13HXk"
    private val orgCode = "demoveranda"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
        playerFragment.enableAutoFullScreenOnRotate()
        playerFragment.setOnInitializationListener(object: InitializationListener {
            override fun onInitializationSuccess(player: TpStreamPlayer) {
                val parameters = TpInitParams.Builder()
                    .setVideoId(videoId)
                    .setAccessToken(accessToken)
                    .setOrgCode(orgCode)
                    .build()
                player.load(parameters)
                player.setPlayWhenReady(true)
            }
        });
    }
}

