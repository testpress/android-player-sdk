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
import com.tpstream.player.data.source.local.migration.RoomMigration4To5.MIGRATION_4_5
import com.tpstream.player.data.source.local.migration.RoomMigration5To6.MIGRATION_5_6
import com.tpstream.player.data.source.local.migration.RoomMigration6To7.MIGRATION_6_7
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
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        )
            .build()

        val video = versionDB.assetDao().getAssetByUrl("url")
        assertEquals("videoID",video?.videoId)
    }

    @Test
    @Throws(IOException::class)
    fun testMigration2To7() {
        db = helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """INSERT INTO OfflineVideoInfo VALUES ('1', 'videoID1', 'title1', 'thumbnail1', 'url1', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            execSQL(
                """INSERT INTO OfflineVideoInfo VALUES ('2', 'videoID2', 'title2', 'thumbnail2', 'url2', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            execSQL(
                """INSERT INTO OfflineVideoInfo VALUES ('3', 'videoID3', 'title3', 'thumbnail3', 'url3', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        )
            .build()

        val video = versionDB.assetDao().getAssetByUrl("url2")
        val allVideos = versionDB.assetDao().getAllAsset()
        assertEquals("videoID2",video?.videoId)
        assertEquals(3,allVideos?.size)
    }

    @Test
    @Throws(IOException::class)
    fun testMigration3To7() {
        db = helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                """INSERT INTO Video VALUES ('1', 'videoID1', 'title1', 'thumbnail1', 'url1', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            execSQL(
                """INSERT INTO Video VALUES ('2', 'videoID2', 'title2', 'thumbnail2', 'url2', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        )
            .build()

        val video = versionDB.assetDao().getAssetByUrl("url2")
        val allVideos = versionDB.assetDao().getAllAsset()
        assertEquals("videoID2",video?.videoId)
        assertEquals(2,allVideos?.size)
    }

    @Test
    @Throws(IOException::class)
    fun testMigration4To7() {
        db = helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """INSERT INTO Asset VALUES (1, 'videoID1', 'title1', 'thumbnail1', 'url1', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim()
            )
            execSQL(
                """INSERT INTO Asset VALUES (2, 'videoID2', 'title2', 'thumbnail2', 'url2', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim()
            )
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        ).build()

        val video = versionDB.assetDao().getAssetByUrl("url2")
        val allVideos = versionDB.assetDao().getAllAsset()
        assertEquals("videoID2", video?.videoId)
        assertEquals(2, allVideos?.size)
    }

    @Test
    @Throws(IOException::class)
    fun testMigration5To7() {
        db = helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """INSERT INTO Asset VALUES ('videoID1', 'title1', 'thumbnail1', 'url1', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim()
            )
            execSQL(
                """INSERT INTO Asset VALUES ('videoID2', 'title2', 'thumbnail2', 'url2', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim()
            )
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_5_6, MIGRATION_6_7)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        ).build()

        val video = versionDB.assetDao().getAssetByUrl("url2")
        val allVideos = versionDB.assetDao().getAllAsset()
        assertEquals("videoID2", video?.videoId)
        assertEquals(2, allVideos?.size)
        assertEquals(null, video?.folderTree)
        assertEquals(0L, video?.duration)
    }

    @Test
    @Throws(IOException::class)
    fun testMigration6To7() {
        db = helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                """INSERT INTO Asset VALUES ('videoID1', 'title1', 'thumbnail1', 'url1', 4500, 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080, '1>2>3>4>5', 2000)""".trim()
            )
            execSQL(
                """INSERT INTO Asset VALUES ('videoID2', 'title2', 'thumbnail2', 'url2', 3600, 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080, '1>2>3>4>5>6>7>8>9>10', 2001)""".trim()
            )
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        ).build()

        val video = versionDB.assetDao().getAssetByUrl("url2")
        val allVideos = versionDB.assetDao().getAllAsset()
        assertEquals("videoID2", video?.videoId)
        assertEquals(2, allVideos?.size)
        assertEquals("1>2>3>4>5>6>7>8>9>10", video?.folderTree)
        assertEquals(3600L, video?.duration)
        assertEquals(null, video?.metadata)
    }

}