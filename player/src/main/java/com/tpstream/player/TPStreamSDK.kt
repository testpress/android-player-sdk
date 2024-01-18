package com.tpstream.player

object TPStreamsSDK {
    private var provider: Provider = Provider.TPStreams
    private var _orgCode: String? = null
    private var _authToken: String? = null
    val orgCode: String
        get() = checkNotNull(_orgCode) { "TPStreamsSDK is not initialized. You must call initialize first." }

    val authToken: String
        get() = checkNotNull(_authToken) { "TPStreamsSDK is not initialized. You must call initialize first." }

    internal val authenticationHeader: Map<String, String>
        get() = getAuthenticationHeader()

    fun initialize(provider: Provider = Provider.TPStreams, orgCode: String) {
        if (orgCode.isEmpty()) {
            throw IllegalArgumentException("orgCode cannot be empty.")
        }

        this._orgCode = orgCode
        this.provider = provider
    }

    fun initialize(provider: Provider = Provider.TPStreams, orgCode: String, authToken: String) {
        require(orgCode.isNotEmpty()) { "orgCode cannot be empty." }
        require(authToken.isNotEmpty()) { "authToken cannot be empty." }
        this._orgCode = orgCode
        this._authToken = authToken
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

    internal fun constructOfflineDRMLicenseUrl(videoId: String?, accessToken: String?): String {
        return "${constructDRMLicenseUrl(videoId, accessToken)}&drm_type=widevine&download=true"
    }

    private fun getAuthenticationHeader() : Map<String,String> {
        return when (provider) {
            Provider.TestPress -> mapOf("Authorization" to "JWT $authToken")
            Provider.TPStreams -> mapOf("Authorization" to "Token $authToken")
        }
    }

    enum class Provider {
        TestPress,
        TPStreams
    }
}