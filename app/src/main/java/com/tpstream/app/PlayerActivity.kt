package com.tpstream.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.TpStreamPlayerFragment

class PlayerActivity : AppCompatActivity() {
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
}

