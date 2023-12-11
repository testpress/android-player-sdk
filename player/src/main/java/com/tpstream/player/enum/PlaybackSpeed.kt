package com.tpstream.player.enum

enum class PlaybackSpeed(val value: Float, val text: String) {
    PLAYBACK_SPEED_0_25(0.25f, "0.25x"),
    PLAYBACK_SPEED_0_5(0.5f, "0.5x"),
    PLAYBACK_SPEED_0_75(0.75f, "0.75x"),
    PLAYBACK_SPEED_1_0(1.0f, "1x"),
    PLAYBACK_SPEED_1_25(1.25f, "1.25x"),
    PLAYBACK_SPEED_1_5(1.5f, "1.5x"),
    PLAYBACK_SPEED_2_0(2.0f, "2x")
}