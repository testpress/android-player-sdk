package com.tpstream.player

object TPStreamsSDK {
    private var provider: Provider = Provider.TPStreams
    private var _orgCode: String? = null
    val orgCode: String
        get() {
            if (_orgCode == null) {
                throw IllegalStateException("TPStreamsSDK is not initialized. You must call initialize first.")
            }
            return _orgCode!!
        }

    fun initialize(provider: Provider = Provider.TPStreams, orgCode: String) {
        if (orgCode.isEmpty()) {
            throw IllegalArgumentException("orgCode cannot be empty.")
        }

        this._orgCode = orgCode
        this.provider = provider
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

    enum class Provider {
        TestPress,
        TPStreams
    }
}