package com.tpstream.player.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.Executors

abstract class TPStreamsDatabaseTest{
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var _db: TPStreamsDatabase
    val db: TPStreamsDatabase
        get() = _db

    @Before
    fun initDb() {
        _db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TPStreamsDatabase::class.java
        )
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    @After
    fun closeDb() {
        _db.close()
    }
}
