package com.tpstream.app

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tpstream.player.TPStreamsSDK
import com.tpstream.player.TpInitParams
import android.Manifest.permission.POST_NOTIFICATIONS
import com.tpstream.player.offline.TpStreamDownloadManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askPushNotificationPermission()
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

    private fun askPushNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) != PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(POST_NOTIFICATIONS),
                    1000
                )
            }
        }
    }
}