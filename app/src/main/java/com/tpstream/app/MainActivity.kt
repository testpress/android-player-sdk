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
        myIntent.putExtra("params",1)
        startActivity(myIntent)
    }

    fun buttonClick2(view: View) {
        val myIntent = Intent(this, PlayerActivity::class.java)
        myIntent.putExtra("params",2)
        startActivity(myIntent)
    }
}