package com.tpstream.player.models

import com.google.gson.annotations.SerializedName

data class DRMLicenseURL(
    @SerializedName("license_url")
    val licenseUrl: String
)
