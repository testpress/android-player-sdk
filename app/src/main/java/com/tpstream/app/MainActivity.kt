package com.tpstream.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import com.tpstream.player.offline.TpStreamDownloadManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun buttonClick(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TP_DRM")
        startActivity(myIntent)
    }

    fun buttonClick2(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TP_AES_Encrypt")
        startActivity(myIntent)
    }

    fun buttonClick3(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TP_NON_DRM")
        startActivity(myIntent)
    }

    fun buttonClick4(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TPS_DRM")
        startActivity(myIntent)
    }

    fun buttonClick5(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","TPS_NON_DRM")
        startActivity(myIntent)
    }

    fun downloadButton(view: View) {
        val myIntent = Intent(this, DownloadListActivity::class.java)
        startActivity(myIntent)
    }

    fun downloadDRMVideo(view: View) {
        TPStreamsSDK.initialize(TPStreamsSDK.Provider.TPStreams, "6eafqn")
        val parameters = TpInitParams.Builder()
            .setVideoId("6suEBPy7EG4")
            .setAccessToken("ab70caed-6168-497f-89c1-1e308da2c9aa")
            .build()
        TpStreamDownloadManager(applicationContext).startDownload(this,parameters)
    }

}