package com.tpstream.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.tpstream.player.InitializationListener
import com.tpstream.player.TpInitParams
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.TpStreamPlayerFragment

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var tpStreamPlayer: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private lateinit var accessToken :String
    private lateinit var videoId :String
    private lateinit var orgCode :String
    private lateinit var parameters : TpInitParams

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        selectVideoParams(intent.getStringExtra("VideoParameter")!!)
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
        playerFragment.enableAutoFullScreenOnRotate()
        playerFragment.setOnInitializationListener(object: InitializationListener {
            override fun onInitializationSuccess(player: TpStreamPlayer) {
                play()
            }
        });
    }

    fun play(){
        parameters = TpInitParams.Builder()
            .setVideoId(videoId)
            .setAccessToken(accessToken)
            .setOrgCode(orgCode)
            .setAutoPlay(true)
            .enableDownloadSupport(true)
            .build()
        playerFragment.load(parameters)

    }

    private fun selectVideoParams(videoType: String){
        when(videoType){
            "DRM" -> {
                accessToken = "c381512b-7337-4d8e-a8cf-880f4f08fd08"
                videoId = "C3XLe1CCcOq"
                orgCode = "demoveranda"
            }
            "AES Encrypt" -> {
                accessToken = "143a0c71-567e-4ecd-b22d-06177228c25b"
                videoId = "o7pOsacWaJt"
                orgCode = "demoveranda"
            }
            "Clear" -> {
                accessToken = "70f61402-3724-4ed8-99de-5473b2310efe"
                videoId = "qJQlWGLJvNv"
                orgCode = "demoveranda"
            }
        }
    }

}

