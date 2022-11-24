package com.tpstream.player.views

import android.os.Bundle
import android.view.WindowManager
import com.tpstream.player.R

import androidx.appcompat.app.AppCompatActivity

class TestActivity:AppCompatActivity() {

    private var destroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.frame_layout)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyed = true
    }

    override fun isDestroyed(): Boolean {
        return destroyed
    }

}