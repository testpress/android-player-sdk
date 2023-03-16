package com.tpstream.player.database.dao

import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tpstream.player.database.TPStreamsDatabase
import com.tpstream.player.models.Video
import com.tpstream.player.models.asDomainVideos
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
        val video4 = Video(id = 4L, videoId = "VideoID_4")
        videoDao.insert(video4.asDatabaseVideo())
        // Check data added
        val beforeResult = videoDao.getAllVideo()
        assertThat(beforeResult?.size, equalTo(4))
        // Delete one data
        videoDao.delete(video4.videoId)
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
            val liveData = Transformations.map(videoDao.getVideoById("VideoID_1")) { it?.asDomainVideo() }
            val observer = Observer<Video?> { result ->
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
            val liveData = Transformations.map(videoDao.getAllDownloadInLiveData()) { it?.asDomainVideos() }
            val observer = Observer<List<Video>?> { result ->
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
        val video1 = Video(id = 1L, videoId = "VideoID_1", url = "url_1")
        val video2 = Video(id = 2L, videoId = "VideoID_2", url = "url_2")
        val video3 = Video(id = 3L, videoId = "VideoID_3", url = "url_3")
        // Add data to db
        videoDao.insert(video1.asDatabaseVideo())
        videoDao.insert(video2.asDatabaseVideo())
        videoDao.insert(video3.asDatabaseVideo())
    }

}