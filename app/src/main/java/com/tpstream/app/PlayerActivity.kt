package com.tpstream.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tpstream.player.InitializationListener
import com.tpstream.player.TpInitParams
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.TpStreamPlayerFragment

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var player: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private val accessToken = "bbf23112-0c14-4519-a848-73c95cb024ac"
    private val videoId = "E44ulfSWhYx"
    private val orgCode = "drm"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
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

