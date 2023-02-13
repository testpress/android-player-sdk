package com.tpstream.player.database

import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tpstream.player.models.OfflineVideoInfo
import com.tpstream.player.models.OfflineVideoState
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import java.util.*


@RunWith(AndroidJUnit4::class)
class OfflineVideoInfoDaoTest : TPStreamsDatabaseTest() {

    @Test
    fun testAddData() = runBlocking {
        val offlineVideoInfo = createData()
        db.offlineVideoInfoDao().insert(offlineVideoInfo)

        val fetchOfflineVideoInfo = db.offlineVideoInfoDao().getOfflineVideoInfoByVideoId("Demo")
        assertThat(fetchOfflineVideoInfo?.videoId, equalTo(offlineVideoInfo.videoId))
    }

    @Test
    fun testDeleteData() = runBlocking {
        val offlineVideoInfo = createData()
        //Adding offlineVideoInfo
        db.offlineVideoInfoDao().insert(offlineVideoInfo)
        assertTrue(db.offlineVideoInfoDao().getAllOfflineVideoInfo()!!.isNotEmpty())
        //Deleting offlineVideoInfo
        db.offlineVideoInfoDao().delete(offlineVideoInfo)
        assertTrue(db.offlineVideoInfoDao().getAllOfflineVideoInfo()!!.isEmpty())
    }

    @Test
    fun testGetOfflineVideoInfoByVideoId() = runBlocking {
        val offlineVideoInfo = createData()
        db.offlineVideoInfoDao().insert(offlineVideoInfo)

        val fetchOfflineVideoInfoByVideoID = db.offlineVideoInfoDao().getOfflineVideoInfoByVideoId("Demo")
        assertThat(fetchOfflineVideoInfoByVideoID?.videoId, equalTo(offlineVideoInfo.videoId))
    }

    @Test
    fun testGetOfflineVideoInfoByUrl() = runBlocking {
        val offlineVideoInfo = createData()
        db.offlineVideoInfoDao().insert(offlineVideoInfo)

        val fetchOfflineVideoInfoByUrl = db.offlineVideoInfoDao().getOfflineVideoInfoByUrl("playback_url")
        assertThat(fetchOfflineVideoInfoByUrl?.url, equalTo(offlineVideoInfo.url))
    }

    @Test
    fun testGetAllVideoInfo() = runBlocking {
        val data1 = createData(1L)
        val data2 = createData(2L)
        val data3 = createData(3L)

        db.offlineVideoInfoDao().insert(data1)
        db.offlineVideoInfoDao().insert(data2)
        db.offlineVideoInfoDao().insert(data3)

        assertThat(db.offlineVideoInfoDao().getAllOfflineVideoInfo()!!.size, equalTo(3))
    }

//    @Mock
//    private val observer :Observer<List<OfflineVideoInfo>?> = Observer<List<OfflineVideoInfo>?>()

//    @Test
//    fun lie() = runBlocking {
//        val offlineVideoInfo = createData()
//        db.offlineVideoInfoDao().getAllDownloadInLiveData().observeForever(observer)
//        db.offlineVideoInfoDao().insert(offlineVideoInfo)
//        verify(observer).onChanged(Collections.singletonList(offlineVideoInfo))
//    }

    private fun createData(count: Long = 0L): OfflineVideoInfo {
        return OfflineVideoInfo(
            1+count,
            "Demo",
            "title $count",
            "thumbnail",
            "playback_url",
            "10:00:00",
            "description",
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
