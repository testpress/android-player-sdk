package com.tpstream.player.enum

import com.tpstream.player.ui.ResolutionOptions

enum class VideoResolution {
    AUTO {
        override fun getResolutionOptions() = ResolutionOptions.AUTO
    },
    HIGH {
        override fun getResolutionOptions() = ResolutionOptions.HIGH
    },
    LOW {
        override fun getResolutionOptions() = ResolutionOptions.LOW
    };

    internal abstract fun getResolutionOptions(): ResolutionOptions
}