package com.tpstream.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import com.tpstream.player.offline.TpStreamDownloadManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.findViewById<Button>(R.id.testpress_drm).setOnClickListener {
            val parameters = TpInitParams.Builder()
                .setVideoId("6suEBPy7EG4")
                .setAccessToken("ab70caed-6168-497f-89c1-1e308da2c9aa")
                .build()
            val myIntent = Intent(this, PlayerActivity::class.java)
            myIntent.putExtra(TP_OFFLINE_PARAMS,parameters)
            startActivity(myIntent)
        }

        this.findViewById<Button>(R.id.testpress_non_drm).setOnClickListener {
            val parameters = TpInitParams.Builder()
                .setVideoId("8DjR3FzHy4Z")
                .setAccessToken("0cebd232-3699-4908-81f0-3cc2fa9497f8")
                .build()
            val myIntent = Intent(this, PlayerActivity::class.java)
            myIntent.putExtra(TP_OFFLINE_PARAMS,parameters)
            startActivity(myIntent)
        }


        this.findViewById<Button>(R.id.exoplayer_drm).setOnClickListener {
            val parameters = TpInitParams.Builder()
                .setVideoId("exoplayer_drm")
                .setAccessToken("ab70caed-6168-497f-89c1-1e308da2c9aa")
                .build()
            val myIntent = Intent(this, PlayerActivity::class.java)
            myIntent.putExtra(TP_OFFLINE_PARAMS,parameters)
            startActivity(myIntent)
        }

        this.findViewById<Button>(R.id.exoplayer_non_drm).setOnClickListener {
            val parameters = TpInitParams.Builder()
                .setVideoId("exoplayer_non_drm")
                .setAccessToken("0cebd232-3699-4908-81f0-3cc2fa9497f8")
                .build()
            val myIntent = Intent(this, PlayerActivity::class.java)
            myIntent.putExtra(TP_OFFLINE_PARAMS,parameters)
            startActivity(myIntent)
        }

    }

}