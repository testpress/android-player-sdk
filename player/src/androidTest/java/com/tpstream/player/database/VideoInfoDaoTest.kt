package com.tpstream.player.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tpstream.player.models.VideoInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class VideoInfoDaoTest :TPStreamsDatabaseTest(){

    private fun createData(): VideoInfo {
        return VideoInfo(
            "Test",
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

    @Test
    fun testAddData(){
        val videoInfo = createData()
        db.videoInfoDao().insert(videoInfo)

        val fetchVideoInfo = db.videoInfoDao().getVideoUrlByVideoId("Test")
        assertThat(fetchVideoInfo, equalTo(videoInfo))
    }

    @Test
    fun testDeleteData(){
        val videoInfo = createData()
        db.videoInfoDao().insert(videoInfo)

        assertTrue(db.videoInfoDao().getAllVideoInfo().isNotEmpty())

        db.videoInfoDao().delete(videoInfo)

        assertTrue(db.videoInfoDao().getAllVideoInfo().isEmpty())
    }

    @Test
    fun testGetVideoInfoByVideoId(){
        val videoInfo = createData()
        db.videoInfoDao().insert(videoInfo)

        assertThat(db.videoInfoDao().getVideoUrlByVideoId("Test"), equalTo(videoInfo))
    }


}
