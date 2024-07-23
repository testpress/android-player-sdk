package com.tpstream.app

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tpstream.player.*
import com.tpstream.player.constants.PlaybackError
import com.tpstream.player.ui.InitializationListener
import com.tpstream.player.ui.TpStreamPlayerFragment
import java.io.File
import java.io.IOException

const val TP_OFFLINE_PARAMS = "tp_offline_params"

class PlayerActivity : AppCompatActivity() {
    lateinit var playerFragment: TpStreamPlayerFragment;
    lateinit var tpStreamPlayer: TpStreamPlayer;
    private val TAG = "PlayerActivity"
    private lateinit var accessToken :String
    private lateinit var videoId :String
    private var orgCode :String = "6eafqn"
    private var provider: TPStreamsSDK.Provider = TPStreamsSDK.Provider.TPStreams
    private var parameters : TpInitParams? = null
    private var eventLog = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        parameters = intent.getParcelableExtra(TP_OFFLINE_PARAMS)
        selectVideoParams(intent.getStringExtra("VideoParameter"))
        TPStreamsSDK.initialize(provider, orgCode)
        val eventLogTextView = findViewById<TextView>(R.id.event_log)
        eventLogTextView.movementMethod = ScrollingMovementMethod.getInstance()
        playerFragment =
            supportFragmentManager.findFragmentById(R.id.tpstream_player_fragment) as TpStreamPlayerFragment
        playerFragment.enableAutoFullScreenOnRotate()
        playerFragment.setOnInitializationListener(object: InitializationListener {
            override fun onInitializationSuccess(player: TpStreamPlayer) {
                tpStreamPlayer = player
                tpStreamPlayer.load(buildParams())
                tpStreamPlayer.setListener( object : TPStreamPlayerListener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                    }

                    override fun onAccessTokenExpired(videoId: String, callback: (String) -> Unit) {
                        val newAccessToken = getAccessToken(videoId)
                        callback.invoke(newAccessToken)
                    }

                    override fun onMarkerCallback(timesInSeconds: Long) {
                        Toast.makeText(this@PlayerActivity,"$timesInSeconds",Toast.LENGTH_SHORT).show()
                    }

                    override fun onFullScreenChanged(isFullScreen: Boolean) {
                        Toast.makeText(this@PlayerActivity, isFullScreen.toString(), Toast.LENGTH_SHORT).show()
                    }

                    override fun onPlayerError(playbackError: PlaybackError) {
                        super.onPlayerError(playbackError)
                        Log.d("TAG", "onPlayerError: ${playbackError}")
                    }
                })

                tpStreamPlayer.addTestpressAnalyticsListener(TestpressLogger {
                    eventLog += it
                    eventLogTextView.text = eventLog
                })
            }
        });
        playerFragment.setPreferredFullscreenExitOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
        initializeSampleButtons();
    }

    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)
        } else {
            // Permission already granted
            downloadLogFile()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadLogFile()
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadLogFile() {
        val eventLogTextView = findViewById<TextView>(R.id.event_log)
        val logContent = eventLogTextView.text.toString()

        if (logContent.isNotEmpty()) {
            val fileName = "event_log.txt"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            try {
                file.writeText(logContent)
                Toast.makeText(this, "Log file saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save log file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No events to log", Toast.LENGTH_SHORT).show()
        }
    }

    fun buildParams(): TpInitParams {
        if (parameters == null) {
            parameters = TpInitParams.Builder()
                .setVideoId(videoId)
                .setAccessToken(accessToken)
                .setAutoPlay(true)
                .setOfflineLicenseExpireTime(FIFTEEN_DAYS)
                .enableDownloadSupport(true)
                .build()
        }
        return parameters!!
    }

    private fun selectVideoParams(videoType: String?){
        when(videoType){
            "TP_DRM" -> {
                accessToken = "a4c04ca8-9c0e-4c9c-a889-bd3bf8ea586a"
                videoId = "ATJfRdHIUC9"
                orgCode = "lmsdemo"
                provider = TPStreamsSDK.Provider.TestPress
            }
            "TP_AES_Encrypt" -> {
                accessToken = "5f6355d0-62ac-4bfd-98ca-4a1e9a2857e3"
                videoId = "ZZb3S5OB3nY"
                orgCode = "lmsdemo"
                provider = TPStreamsSDK.Provider.TestPress
            }
            "TP_NON_DRM" -> {
                accessToken = "5c49285b-0557-4cef-b214-66034d0b77c3"
                videoId = "z1TLpfuZzXh"
                orgCode = "lmsdemo"
                provider = TPStreamsSDK.Provider.TestPress
            }
            "TPS_DRM" -> {
                accessToken = "ab70caed-6168-497f-89c1-1e308da2c9aa"
                videoId = "6suEBPy7EG4"
                orgCode = "6eafqn"
                provider = TPStreamsSDK.Provider.TPStreams
            }
            "TPS_NON_DRM" -> {
                accessToken = "020c75d2-87ed-4171-8d8a-048a2ad12276"
                videoId = "3cX2G3c9QMd"
                orgCode = "m9n4m6"
                provider = TPStreamsSDK.Provider.TPStreams
            }
            null ->{}
        }
    }

    private fun initializeSampleButtons() {
        findViewById<Button>(R.id.sample_play).setOnClickListener {
            tpStreamPlayer.play()
        }
        findViewById<Button>(R.id.sample_pause).setOnClickListener {
            tpStreamPlayer.pause()
        }
        findViewById<Button>(R.id.enter_full_screen).setOnClickListener {
            playerFragment.showFullScreen()
        }
        val downloadButton = findViewById<Button>(R.id.download_log)
        downloadButton.setOnClickListener {
            checkPermissions()
        }
    }

    fun getAccessToken(videoId: String): String {
        return when (videoId) {
            "ATJfRdHIUC9" -> "a4c04ca8-9c0e-4c9c-a889-bd3bf8ea586a"
            "ZZb3S5OB3nY" -> "5f6355d0-62ac-4bfd-98ca-4a1e9a2857e3"
            "z1TLpfuZzXh" -> "5c49285b-0557-4cef-b214-66034d0b77c3"
            "6suEBPy7EG4" -> "ab70caed-6168-497f-89c1-1e308da2c9aa"
            "C65BJzhj48k" -> "48a481d0-7a7f-465f-9d18-86f52129430b"
            else -> ""
        }
    }

}

