package com.tpstream.player.data.source.local

import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tpstream.player.data.Asset
import com.tpstream.player.data.Video
import com.tpstream.player.data.asDomainAsset
import com.tpstream.player.data.asDomainAssets
import com.tpstream.player.data.asLocalVideo
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class VideoDaoTest {

    private lateinit var videoDao: VideoDao
    private lateinit var db: TPStreamsDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TPStreamsDatabase::class.java
        ).build()
        videoDao = db.videoDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testGetOfflineVideoInfoByVideoId() {
        insertData()
        val result = videoDao.getVideoByVideoId("VideoID_1")
        assertThat(result?.id, equalTo(1L))
    }

    @Test
    @Throws(Exception::class)
    fun testGetAllOfflineVideoInfo() {
        insertData()
        val result = videoDao.getAllVideo()
        assertThat(result?.size, equalTo(3))
    }

    @Test
    @Throws(Exception::class)
    fun testGetOfflineVideoInfoByUrl() {
        insertData()
        val result = videoDao.getVideoByUrl("url_2")
        assertThat(result?.id, equalTo(2L))
    }

    @Test
    @Throws(Exception::class)
    fun testDelete() = runBlocking {
        insertData()
        val asset4 = Asset(id = "VideoID_4")
        videoDao.insert(asset4.asLocalVideo())
        // Check data added
        val beforeResult = videoDao.getAllVideo()
        assertThat(beforeResult?.size, equalTo(4))
        // Delete one data
        videoDao.delete(asset4.id)
        // Check deleted
        val afterResult = videoDao.getAllVideo()
        assertThat(afterResult?.size, equalTo(3))

        val result = videoDao.getVideoByVideoId("VideoID_4")
        assertThat(result, equalTo(null))
    }

    @Test
    @Throws(Exception::class)
    fun testGetOfflineVideoInfoById() {
        insertData()
        CoroutineScope(Dispatchers.Main).launch {
            val liveData = Transformations.map(videoDao.getVideoById("VideoID_1")) { it?.asDomainAsset() }
            val observer = Observer<Asset?> { result ->
                assertNotNull(result)
                assertEquals("VideoID_1", result.id)
            }
            liveData.observeForever(observer)
            // Cleanup
            liveData.removeObserver(observer)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testGetAllDownloadInLiveData() {
        insertData()
        CoroutineScope(Dispatchers.Main).launch {
            val liveData = Transformations.map(videoDao.getAllDownloadInLiveData()) { it?.asDomainAssets() }
            val observer = Observer<List<Asset>?> { result ->
                assertNotNull(result)
                assertEquals(3, result.size)
                assertEquals("VideoID_1", result[0].id)
            }
            liveData.observeForever(observer)
            // Cleanup
            liveData.removeObserver(observer)
        }
    }

    private fun insertData() = runBlocking {
        val asset1 = Asset(id = "VideoID_1", video = Video(url = "url_1"))
        val asset2 = Asset(id = "VideoID_2", video = Video(url = "url_1"))
        val asset3 = Asset(id = "VideoID_3", video = Video(url = "url_1"))
        // Add data to db
        videoDao.insert(asset1.asLocalVideo())
        videoDao.insert(asset2.asLocalVideo())
        videoDao.insert(asset3.asLocalVideo())
    }

}