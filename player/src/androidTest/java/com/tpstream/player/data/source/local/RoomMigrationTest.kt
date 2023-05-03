package com.tpstream.player.data.source.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tpstream.player.data.source.local.migration.RoomMigration1To2.MIGRATION_1_2
import com.tpstream.player.data.source.local.migration.RoomMigration2To3.MIGRATION_2_3
import com.tpstream.player.data.source.local.migration.RoomMigration3To4.MIGRATION_3_4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomMigrationTest {

    lateinit var db : SupportSQLiteDatabase
    private val TEST_DB = "tpStreams-database"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TPStreamsDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun testMigrationUpToLatestVersion() {
        db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """INSERT INTO OfflineVideoInfo VALUES ('videoID', 'title', 'thumbnail', 'thumbnailSmall', 'thumbnailMedium', 'url', 'dashUrl', 'hlsUrl', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        )
            .build()

        val video = versionDB.videoDao().getVideoByUrl("url")
        assertEquals(null,video?.videoId)

        val allVideos = versionDB.videoDao().getAllVideo()
        assertEquals(0,allVideos?.size)
    }

    @Test
    @Throws(IOException::class)
    fun testMigration2To4() {
        db = helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """INSERT INTO OfflineVideoInfo VALUES ('1', 'videoID1', 'title1', 'thumbnail1', 'url1', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            execSQL(
                """INSERT INTO OfflineVideoInfo VALUES ('2', 'videoID2', 'title2', 'thumbnail2', 'url2', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            execSQL(
                """INSERT INTO OfflineVideoInfo VALUES ('3', 'videoID3', 'title3', 'thumbnail3', 'url3', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, 'COMPLETE', 1920, 1080)""".trim())
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        )
            .build()

        val video = versionDB.videoDao().getVideoByUrl("url2")
        assertEquals(null,video?.videoId)

        val allVideos = versionDB.videoDao().getAllVideo()
        assertEquals(1,allVideos?.size)
    }

    @Test
    @Throws(IOException::class)
    fun testMigration3To4() {
        db = helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                """INSERT INTO Video VALUES ('1', 'videoID1', 'title1', 'thumbnail1', 'url1', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            execSQL(
                """INSERT INTO Video VALUES ('2', 'videoID2', 'title2', 'thumbnail2', 'url2', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            execSQL(
                """INSERT INTO Video VALUES ('3', 'videoID3', 'title3', 'thumbnail3', 'url3', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, 'COMPLETE', 1920, 1080)""".trim())
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        )
            .build()

        val video = versionDB.videoDao().getVideoByUrl("url3")
        assertEquals("videoID3",video?.videoId)
        assertEquals("",video?.orgCode)
        val allVideos = versionDB.videoDao().getAllVideo()
        assertEquals(1,allVideos?.size)
    }
}