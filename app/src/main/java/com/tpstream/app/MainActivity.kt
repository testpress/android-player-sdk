package com.tpstream.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tpstream.player.OfflineDownloadResolution
import com.tpstream.player.TpInitParams
import com.tpstream.player.TpStreamDownloadManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun buttonClick(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","DRM")
        startActivity(myIntent)
    }

    fun buttonClick2(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","AES Encrypt")
        startActivity(myIntent)
    }

    fun buttonClick3(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("VideoParameter","Clear")
        startActivity(myIntent)
    }

    fun downloadButton(view: View) {
        val myIntent = Intent(this, DownloadListActivity::class.java)
        startActivity(myIntent)
    }

    fun apiDownload(view: View) {
        val drmVideo = TpInitParams.Builder()
            .setVideoId("C3XLe1CCcOq")
            .setAccessToken("c381512b-7337-4d8e-a8cf-880f4f08fd08")
            .setOrgCode("demoveranda")
            .build()

        val aesEncryptedVideo = TpInitParams.Builder()
            .setVideoId("o7pOsacWaJt")
            .setAccessToken("143a0c71-567e-4ecd-b22d-06177228c25b")
            .setOrgCode("demoveranda")
            .build()

        val clearVideo = TpInitParams.Builder()
            .setVideoId("qJQlWGLJvNv")
            .setAccessToken("70f61402-3724-4ed8-99de-5473b2310efe")
            .setOrgCode("demoveranda")
            .build()

        TpStreamDownloadManager(this).startDownloads(listOf(drmVideo,aesEncryptedVideo,clearVideo),OfflineDownloadResolution.HIGH)
    }

}