package com.tpstream.player

const val HARDWARE_SECURE = 1
const val HARDWARE_NON_SECURE = 2
const val SOFTWARE = 3
object TPStreamsSDK {
    private var _provider: Provider = Provider.TPStreams
    private var _orgCode: String? = null
    private var _codec: Int = -1
    val provider : Provider get() = _provider
    val orgCode: String
        get() {
            if (_orgCode == null) {
                throw IllegalStateException("TPStreamsSDK is not initialized. You must call initialize first.")
            }
            return _orgCode!!
        }
    val codec: Int get() = _codec

    fun initialize(provider: Provider = Provider.TPStreams, orgCode: String, codec: Int = -1) {
        if (orgCode.isEmpty()) {
            throw IllegalArgumentException("orgCode cannot be empty.")
        }

        this._orgCode = orgCode
        this._provider = provider
        this._codec = codec
    }

    fun constructVideoInfoUrl(contentId: String?, accessToken: String?): String {
        val url = when (provider) {
            Provider.TestPress -> "https://%s.testpress.in/api/v2.5/video_info/%s/?access_token=%s"
            Provider.TPStreams -> "https://app.tpstreams.com/api/v1/%s/assets/%s/?access_token=%s"
        }
        return url.format(orgCode, contentId, accessToken)
    }

    fun constructDRMLicenseUrl(contentId: String?, accessToken: String?): String {
        val url = when (provider) {
            Provider.TestPress -> "https://%s.testpress.in/api/v2.5/drm_license_key/%s/?access_token=%s"
            Provider.TPStreams -> "https://app.tpstreams.com/api/v1/%s/assets/%s/drm_license/?access_token=%s"
        }
        return url.format(orgCode, contentId, accessToken)
    }

    internal fun constructOfflineDRMLicenseUrl(
        contentId: String?,
        accessToken: String?,
        licenseDurationSeconds: Int
    ): String {
        return "${
            constructDRMLicenseUrl(
                contentId,
                accessToken
            )
        }&drm_type=widevine&download=true&rental_duration_seconds=$licenseDurationSeconds&license_duration_seconds=$licenseDurationSeconds"
    }

    enum class Provider {
        TestPress,
        TPStreams
    }
}