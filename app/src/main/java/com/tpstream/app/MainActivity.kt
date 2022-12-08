package com.tpstream.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

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

}