package com.tpstream.player.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tpstream.player.models.VideoInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoInfoDaoTest : TPStreamsDatabaseTest() {

    @Test
    fun testAddData() {
        val videoInfo = createData()
        db.videoInfoDao().insert(videoInfo)

        val fetchVideoInfo = db.videoInfoDao().getVideoInfoByVideoId("Test")
        assertThat(fetchVideoInfo, equalTo(videoInfo))
    }

    @Test
    fun testDeleteData() {
        val videoInfo = createData()
        db.videoInfoDao().insert(videoInfo)

        assertTrue(db.videoInfoDao().getAllVideoInfo()!!.isNotEmpty())

        db.videoInfoDao().delete(videoInfo)

        assertTrue(db.videoInfoDao().getAllVideoInfo()!!.isEmpty())
    }

    @Test
    fun testGetVideoInfoByVideoId() {
        val videoInfo = createData()
        db.videoInfoDao().insert(videoInfo)

        assertThat(db.videoInfoDao().getVideoInfoByVideoId("Test"), equalTo(videoInfo))
    }

    @Test
    fun testGetAllVideoInfo() {
        val data1 = createData("1")
        val data2 = createData("2")
        val data3 = createData("3")

        db.videoInfoDao().insert(data1)
        db.videoInfoDao().insert(data2)
        db.videoInfoDao().insert(data3)

        assertThat(db.videoInfoDao().getAllVideoInfo()!!.size, equalTo(3))
    }

    private fun createData(count: String = ""): VideoInfo {
        return VideoInfo(
            "Test$count",
            "Demo",
            null,
            null,
            null,
            "url",
            "dashUrl",
            "hslUrl",
            null,
            null,
            null
        )
    }
}
