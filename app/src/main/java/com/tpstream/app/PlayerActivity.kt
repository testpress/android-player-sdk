package com.tpstream.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tpstream.player.*
import android.util.Log
import com.tpstream.player.InitializationListener
import com.tpstream.player.TpInitParams
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.TpStreamPlayerFragment

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var player: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private lateinit var accessToken :String
    private lateinit var videoId :String
    private lateinit var orgCode :String
    private lateinit var parameters : TpInitParams

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        selectVideoParams(intent.getIntExtra("params",-1))
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
        playerFragment.enableAutoFullScreenOnRotate()
        playerFragment.setOnInitializationListener(object: InitializationListener {
            override fun onInitializationSuccess(player: TpStreamPlayer) {
                parameters = TpInitParams.Builder()
                    .setVideoId(videoId)
                    .setAccessToken(accessToken)
                    .setOrgCode(orgCode)
                    .build()
                player.load(parameters)
                player.setPlayWhenReady(true)
            }
        });
    }

    private fun selectVideoParams(int : Int){
        when(int){
            1 -> {
                accessToken = "c381512b-7337-4d8e-a8cf-880f4f08fd08"
                videoId = "C3XLe1CCcOq"
                orgCode = "demoveranda"
            }
            2 -> {
                accessToken = "c39cd7a3-2e0f-431b-b7cc-844e515d2046"
                videoId = "JISUX8uDKfZ"
                orgCode = "demoveranda"
            }
        }
    }

    fun videoDownloadButton(view: View){

    }

    fun videoDeleteButton(view: View){

    }
}

