package com.tpstream.player.util

import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController

internal object SystemBars {
    fun setVisibility(window: Window?, hide: Boolean) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window?.let { win ->
                win.setDecorFitsSystemWindows(!hide)
                win.insetsController?.let { controller ->
                    if (hide) {
                        controller.hide(WindowInsets.Type.systemBars())
                        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        controller.show(WindowInsets.Type.systemBars())
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            window?.decorView?.systemUiVisibility = if (hide) {
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN
            } else {
                View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }
}