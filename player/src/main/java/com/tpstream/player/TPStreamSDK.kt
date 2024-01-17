package com.tpstream.player

import com.tpstream.player.util.checkNotEmpty

object TPStreamsSDK {
    private var provider: Provider = Provider.TPStreams
    private var _orgCode: String? = null
    private var _authToken: String? = null
    val orgCode: String
        get() = checkNotNull(_orgCode) { "TPStreamsSDK is not initialized. You must call initialize first." }

    val authToken: String
        get() = checkNotNull(_authToken) { "TPStreamsSDK is not initialized. You must call initialize first." }

    fun initialize(provider: Provider = Provider.TPStreams, orgCode: String) {
        if (orgCode.isEmpty()) {
            throw IllegalArgumentException("orgCode cannot be empty.")
        }

        this._orgCode = orgCode
        this.provider = provider
    }

    fun initialize(provider: Provider = Provider.TPStreams, orgCode: String, authToken: String) {
        this._orgCode = checkNotEmpty(orgCode) { "orgCode cannot be empty." }
        this._authToken = checkNotEmpty(authToken) { "authToken cannot be empty." }
        this.provider = provider
    }

    fun constructVideoInfoUrl(contentId: String?, accessToken: String?): String {
        val url = when (provider) {
            Provider.TestPress -> "https://%s.testpress.in/api/v2.5/video_info/%s/?access_token=%s"
            Provider.TPStreams -> "https://app.tpstreams.com/api/v1/%s/assets/%s/"
        }
        return url.format(orgCode, contentId, accessToken)
    }

    fun constructDRMLicenseUrl(contentId: String?, accessToken: String?): String {
        val url = when (provider) {
            Provider.TestPress -> "https://%s.testpress.in/api/v2.5/drm_license_key/%s/?access_token=%s"
            Provider.TPStreams -> "https://app.tpstreams.com/api/v1/%s/assets/%s/drm_license/"
        }
        return url.format(orgCode, contentId, accessToken)
    }

    internal fun constructOfflineDRMLicenseUrl(contentId: String?, accessToken: String?): String {
        return "${constructDRMLicenseUrl(contentId, accessToken)}&drm_type=widevine&download=true"
    }

    enum class Provider {
        TestPress,
        TPStreams
    }
}