package com.tpstream.player.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tpstream.player.database.roomMigration.RoomMigration1To2.MIGRATION_1_2
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
    fun migrate1To2() {
        db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """INSERT INTO OfflineVideoInfo VALUES ('videoID', 'title', 'thumbnail', 'thumbnailSmall', 'thumbnailMedium', 'url', 'dashUrl', 'hlsUrl', 'duration', 'description', 'transcodingStatus', 100, 1000, 1000, null, 1920, 1080)""".trim())
            close()
        }
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        val versionDB = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TPStreamsDatabase::class.java,
            TEST_DB
        )
            .build()

        val video = versionDB.videoDao().getVideoByUrl("url")
        assertEquals("videoID",video?.videoId)
        assertEquals(1L,video?.id)
    }

}