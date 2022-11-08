package com.tpstream.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tpstream.player.InitializationListener
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.TpStreamPlayerFragment
import java.util.*
import kotlin.concurrent.schedule

class PlayerActivity : AppCompatActivity(), InitializationListener {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var player: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private val otp = "20160313versASE32334W6BUWHprQUWkxri6svgvB6sPWbtfn3ximrDVA8QMoakr"
    private val playbackInfo = "eyJ2aWRlb0lkIjoiZDNlYzIxOTY3OThhMGZiMzdhOWYyYThmNDAyZDdlZTcifQ=="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
        playerFragment.initialize(this);
    }

    override fun onInitializationSuccess(player: TpStreamPlayer) {
        player.load(getString(com.tpstream.player.R.string.media_url_dash))

        Timer().schedule(5000) {
            runOnUiThread{
                player.setPlayWhenReady(false)
            }
        }
    }

    override fun onInitializationFailure() {
        TODO("Not yet implemented")
    }
}

