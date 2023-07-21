package com.tpstream.player.util

internal data class MarkerState(var played: Boolean, val deleteAfterDelivery: Boolean)

internal fun Collection<MarkerState>.getPlayedArray(): BooleanArray = map { it.played }.toBooleanArray()