package com.tpstream.player.util

inline fun checkNotEmpty(value: String, lazyMessage: () -> String): String {
    require(value.isNotEmpty()) { lazyMessage() }
    return value
}