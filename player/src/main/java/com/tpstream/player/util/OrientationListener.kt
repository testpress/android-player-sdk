package com.tpstream.player.util

import android.content.Context
import android.content.pm.ActivityInfo
import android.provider.Settings
import android.view.OrientationEventListener

internal class OrientationListener(val context: Context): OrientationEventListener(context) {
    private var isLandscape = false
    private val isAutoRotationIsON: Boolean
        get() = Settings.System.getInt(
            context.contentResolver,
            Settings.System.ACCELEROMETER_ROTATION, 0
        ) == 1
    private var listener: OnOrientationChangeListener? = null

    override fun onOrientationChanged(orientation: Int) {
        if(!isAutoRotationIsON) {
            return
        }

        if (isLandscape != isOrientationChanged(orientation)) {
            isLandscape = !isLandscape
            listener?.onChange(isLandscape)
        }
    }

    private fun isOrientationChanged(orientation: Int): Boolean {
        var isLandscapeCurrently = false
        if (isLandscape && orientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            isLandscapeCurrently = true
        }
        if (orientation in 45..135 || orientation in 225..300) {
            isLandscapeCurrently = true
        }
        return isLandscapeCurrently
    }

    fun setOnChangeListener(listener: OnOrientationChangeListener) {
        this.listener = listener
    }

    fun start() {
        if (canDetectOrientation()) {
            enable()
        }
    }

    fun stop() {
        disable()
    }
}

internal fun interface OnOrientationChangeListener {
    fun onChange(isLandscape: Boolean)
}