package com.tpstream.player.database.dao

import android.content.Context
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.OfflineVideoInfo
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
class OfflineVideoInfoDaoTest {

    private lateinit var offlineVideoInfoDao: OfflineVideoInfoDao
    private lateinit var db: TPStreamsDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TPStreamsDatabase::class.java
        ).build()
        offlineVideoInfoDao = db.offlineVideoInfoDao()
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
        val result = offlineVideoInfoDao.getOfflineVideoInfoByVideoId("VideoID_1")
        assertThat(result?.id, equalTo(1L))
    }

    @Test
    @Throws(Exception::class)
    fun testGetAllOfflineVideoInfo() {
        insertData()
        val result = offlineVideoInfoDao.getAllOfflineVideoInfo()
        assertThat(result?.size, equalTo(3))
    }

    @Test
    @Throws(Exception::class)
    fun testGetOfflineVideoInfoByUrl() {
        insertData()
        val result = offlineVideoInfoDao.getOfflineVideoInfoByUrl("url_2")
        assertThat(result?.id, equalTo(2L))
    }

    @Test
    @Throws(Exception::class)
    fun testDelete() = runBlocking {
        insertData()
        val offlineVideoInfo4 = OfflineVideoInfo(id = 4L, videoId = "VideoID_4")
        offlineVideoInfoDao.insert(offlineVideoInfo4)
        // Check data added
        val beforeResult = offlineVideoInfoDao.getAllOfflineVideoInfo()
        assertThat(beforeResult?.size, equalTo(4))
        // Delete one data
        offlineVideoInfoDao.delete(offlineVideoInfo4)
        // Check deleted
        val afterResult = offlineVideoInfoDao.getAllOfflineVideoInfo()
        assertThat(afterResult?.size, equalTo(3))

        val result = offlineVideoInfoDao.getOfflineVideoInfoByVideoId("VideoID_4")
        assertThat(result, equalTo(null))
    }

    @Test
    @Throws(Exception::class)
    fun testGetOfflineVideoInfoById() {
        insertData()
        CoroutineScope(Dispatchers.Main).launch {
            val liveData = offlineVideoInfoDao.getOfflineVideoInfoById("VideoID_1")
            val observer = Observer<OfflineVideoInfo?> { result ->
                assertNotNull(result)
                assertEquals(1L, result.id)
                assertEquals("VideoID_1", result.videoId)
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
            val liveData = offlineVideoInfoDao.getAllDownloadInLiveData()
            val observer = Observer<List<OfflineVideoInfo>?> { result ->
                assertNotNull(result)
                assertEquals(3, result.size)
                assertEquals("VideoID_1", result[0].videoId)
            }
            liveData.observeForever(observer)
            // Cleanup
            liveData.removeObserver(observer)
        }
    }

    private fun insertData() = runBlocking {
        val offlineVideoInfo1 = OfflineVideoInfo(id = 1L, videoId = "VideoID_1", url = "url_1")
        val offlineVideoInfo2 = OfflineVideoInfo(id = 2L, videoId = "VideoID_2", url = "url_2")
        val offlineVideoInfo3 = OfflineVideoInfo(id = 3L, videoId = "VideoID_3", url = "url_3")
        // Add data to db
        offlineVideoInfoDao.insert(offlineVideoInfo1)
        offlineVideoInfoDao.insert(offlineVideoInfo2)
        offlineVideoInfoDao.insert(offlineVideoInfo3)
    }

}