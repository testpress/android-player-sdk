package com.tpstream.player

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class TpStreamPlayerPreference(
    val enableFullscreen: Boolean = true,
    val enablePlaybackSpeed: Boolean = true,
    val enableCaptions: Boolean = true,
    val showResolutionOptions: Boolean = true,
    val enableSeekButtons: Boolean = true,
    @ColorInt val seekBarColor: Int? = null
) : Parcelable {
    class Builder {
        private var enableFullscreen: Boolean = true
        private var enablePlaybackSpeed: Boolean = true
        private var enableCaptions: Boolean = true
        private var showResolutionOptions: Boolean = true
        private var enableSeekButtons: Boolean = true
        @ColorInt private var seekBarColor: Int? = null

        fun enableFullscreen(enable: Boolean) = apply { this.enableFullscreen = enable }
        fun enablePlaybackSpeed(enable: Boolean) = apply { this.enablePlaybackSpeed = enable }
        fun enableCaptions(enable: Boolean) = apply { this.enableCaptions = enable }
        fun showResolutionOptions(show: Boolean) = apply { this.showResolutionOptions = show }
        fun enableSeekButtons(enable: Boolean) = apply { this.enableSeekButtons = enable }
        fun setSeekBarColor(@ColorInt color: Int) = apply { this.seekBarColor = color }

        fun build() = TpStreamPlayerPreference(
            enableFullscreen,
            enablePlaybackSpeed,
            enableCaptions,
            showResolutionOptions,
            enableSeekButtons,
            seekBarColor
        )
    }
}
