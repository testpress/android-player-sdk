package com.tpstream.player.offline

import android.annotation.SuppressLint
import android.content.Context
import com.tpstream.player.*
import com.tpstream.player.TpInitParams
import com.tpstream.player.util.VideoPlayerInterceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.Executors

internal class VideoDownloadManager {

    private lateinit var downloadCache: Cache
    private lateinit var context: Context
    private var downloadManager: DownloadManager? = null
    private lateinit var databaseProvider: StandaloneDatabaseProvider
    private lateinit var downloadDirectory: File
    private lateinit var httpDataSourceFactory: OkHttpDataSourceFactory

    fun get(): DownloadManager {
        if (downloadManager == null) {
            initializeDownloadManger()
        }
        return downloadManager!!
    }

    @Synchronized
    private fun initializeDownloadManger() {
        downloadManager = DownloadManager(
            context,
            getDatabaseProvider(context),
            getDownloadCache(),
            getHttpDataSourceFactory(),
            Executors.newFixedThreadPool(6)
        )
    }

    @Synchronized
    private fun getDatabaseProvider(context: Context): DatabaseProvider {
        if (!::databaseProvider.isInitialized) {
            databaseProvider = StandaloneDatabaseProvider(context)
        }
        return databaseProvider
    }

    fun getHttpDataSourceFactory(params: TpInitParams? = null): DataSourceFactory {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(VideoPlayerInterceptor(context,params))
            .build()
        httpDataSourceFactory = OkHttpDataSourceFactory(okHttpClient)
            .setDefaultRequestProperties(TPStreamsSDK.getAuthenticationHeader())
        return httpDataSourceFactory
    }

    fun build(params: TpInitParams? = null): CacheDataSourceFactory {
        val cache = VideoDownloadManager(context).getDownloadCache()
        return CacheDataSourceFactory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(getHttpDataSourceFactory(params))
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @Synchronized
    fun getDownloadCache(): Cache {
        if (!::downloadCache.isInitialized) {
            val downloadContentDirectory =
                File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache =
                SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), getDatabaseProvider(context))
        }
        return downloadCache
    }

    fun getDownloadIndex(): DefaultDownloadIndex {
        return DefaultDownloadIndex(databaseProvider)
    }

    @Synchronized
    private fun getDownloadDirectory(): File {
        if (!::downloadDirectory.isInitialized) {
            downloadDirectory = if (context.getExternalFilesDir(null) != null) {
                context.getExternalFilesDir(null)!!
            } else {
                context.filesDir
            }
        }
        return downloadDirectory
    }


    companion object {
        const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
        @SuppressLint("StaticFieldLeak")
        private lateinit var INSTANCE: VideoDownloadManager

        @JvmStatic
        operator fun invoke(context: Context): VideoDownloadManager {
            synchronized(VideoDownloadManager::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = VideoDownloadManager()
                    INSTANCE.context = context
                }
                return INSTANCE
            }
        }
    }
}