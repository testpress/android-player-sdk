package com.tpstream.player

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executors

class VideoDownloadManager() {

    private lateinit var downloadCache: Cache
    private lateinit var context: Context
    private var downloadManager: DownloadManager? = null
    private lateinit var databaseProvider: StandaloneDatabaseProvider
    private lateinit var downloadDirectory: File
    private lateinit var httpDataSourceFactory: DefaultHttpDataSource.Factory

    init {
        Log.d("TAG", "videoDownloadManager: ---------------")
    }

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

    @Synchronized
    fun getHttpDataSourceFactory(): DataSource.Factory {
        if (!::httpDataSourceFactory.isInitialized) {
            httpDataSourceFactory = DefaultHttpDataSource.Factory()
        }
        return httpDataSourceFactory
    }

    fun build(): CacheDataSource.Factory {
        val cache = VideoDownloadManager(context).getDownloadCache()
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(getHttpDataSourceFactory())
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