package com.tpstream.player.util
import java.text.SimpleDateFormat
import java.util.*

fun parseDateTime(dateTimeString: String, pattern: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.parse(dateTimeString)
}