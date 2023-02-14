package com.tpstream.player.database

import android.app.usage.UsageEvents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.OfflineVideoState
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import com.tpstream.player.util.getOrAwaitValue
import org.junit.runner.RunWith

import java.util.*


@RunWith(AndroidJUnit4::class)
class OfflineVideoInfoDaoTest : TPStreamsDatabaseTest() {

    @Test
    fun testInsert() = runBlocking {
        val videoID = "Demo_1"
        addDataToDB()
        val fetchOfflineVideoInfo = db.offlineVideoInfoDao().getOfflineVideoInfoByVideoId(videoID)
        assertThat(fetchOfflineVideoInfo?.videoId, equalTo(videoID))
    }

    @Test
    fun testDelete() = runBlocking {
        val offlineVideoInfo = createData(1L)
        //Adding offlineVideoInfo
        db.offlineVideoInfoDao().insert(offlineVideoInfo)
        assertTrue(db.offlineVideoInfoDao().getAllOfflineVideoInfo()!!.isNotEmpty())
        //Deleting offlineVideoInfo
        db.offlineVideoInfoDao().delete(offlineVideoInfo)
        assertTrue(db.offlineVideoInfoDao().getAllOfflineVideoInfo()!!.isEmpty())
    }

    @Test
    fun testGetOfflineVideoInfoByVideoId() = runBlocking {
        val videoID = "Demo_2"
        addDataToDB()
        val fetchOfflineVideoInfoByVideoID = db.offlineVideoInfoDao().getOfflineVideoInfoByVideoId(videoID)
        assertThat(fetchOfflineVideoInfoByVideoID?.videoId, equalTo(videoID))
    }

    @Test
    fun testGetOfflineVideoInfoByUrl() = runBlocking {
        val url = "playback_url_2"
        addDataToDB()
        val fetchOfflineVideoInfoByUrl = db.offlineVideoInfoDao().getOfflineVideoInfoByUrl(url)
        assertThat(fetchOfflineVideoInfoByUrl?.url, equalTo(url))
    }

    @Test
    fun testGetAllVideoInfo() = runBlocking {
        addDataToDB()
        assertThat(db.offlineVideoInfoDao().getAllOfflineVideoInfo()!!.size, equalTo(5))
    }

    @Test
    fun testGetAllDownloadInLiveData() = runBlocking {
        addDataToDB()
        val fetchedContent = db.offlineVideoInfoDao().getAllDownloadInLiveData().getOrAwaitValue()
        assertThat(fetchedContent?.size, equalTo(5))
    }

    @Test
    fun testGetOfflineVideoInfoById() = runBlocking {
        val videoID = "Demo_5"
        addDataToDB()
        val fetchOfflineVideoInfoByID = db.offlineVideoInfoDao().getOfflineVideoInfoById(videoID).getOrAwaitValue()
        assertThat(fetchOfflineVideoInfoByID?.videoId, equalTo(videoID))
    }

    private fun addDataToDB() = runBlocking {
        val data1 = createData(1L)
        val data2 = createData(2L)
        val data3 = createData(3L)
        val data4 = createData(4L)
        val data5 = createData(5L)

        db.offlineVideoInfoDao().insert(data1)
        db.offlineVideoInfoDao().insert(data2)
        db.offlineVideoInfoDao().insert(data3)
        db.offlineVideoInfoDao().insert(data4)
        db.offlineVideoInfoDao().insert(data5)
    }

    private fun createData(count: Long = 0L): OfflineVideoInfo {
        return OfflineVideoInfo(
            0+count,
            "Demo_$count",
            "title_$count",
            "thumbnail_$count",
            "playback_url_$count",
            "10:00:00",
            "description_$count",
            "4",
            100,
            100000,
            100,
            OfflineVideoState.COMPLETE,
            1080,
            720
        )
    }
}
