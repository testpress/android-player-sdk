package com.tpstream.player.util

internal data class MarkerState(var isPlayed: Boolean, val shouldDeleteAfterDelivery: Boolean)

internal fun Collection<MarkerState>.getPlayedStatusArray(): BooleanArray = map { it.isPlayed }.toBooleanArray()