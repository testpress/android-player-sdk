package com.tpstream.player.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromMap(value: Map<String, String>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, String>? {
        return if (value == null) null else Gson().fromJson(
            value,
            object : TypeToken<Map<String, String>>() {}.type
        )
    }
}
