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
    private val accessToken = "92c51ffe-4334-49ac-8fb8-f8fa1d918d7a"
    private val videoId = "147ZxJ17uPA"
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

