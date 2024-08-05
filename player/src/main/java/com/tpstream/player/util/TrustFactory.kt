package com.tpstream.player.util

import android.content.Context
import com.tpstream.player.R
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class TrustFactory {
    companion object {
        fun getTrustFactoryManager(context: Context): Pair<SSLSocketFactory, X509TrustManager> {
            val cf = CertificateFactory.getInstance("X.509")

            val isrgRoot1Input = context.resources.openRawResource(R.raw.isrg_root_x1)
            val isrgRoot1Certificate: Certificate = isrgRoot1Input.use {
                cf.generateCertificate(it)
            }

            val isrgRoot2Input = context.resources.openRawResource(R.raw.isrg_root_x2)
            val isrgRoot2Certificate: Certificate = isrgRoot2Input.use {
                cf.generateCertificate(it)
            }

            val keyStoreType = KeyStore.getDefaultType()
            val keyStore = KeyStore.getInstance(keyStoreType).apply {
                load(null, null)
                setCertificateEntry("isrgrootx1", isrgRoot1Certificate)
                setCertificateEntry("isrgrootx2", isrgRoot2Certificate)
            }

            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
                init(keyStore)
            }

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, tmf.trustManagers, null)
            }

            return Pair(sslContext.socketFactory, tmf.trustManagers[0] as X509TrustManager)
        }
    }
}