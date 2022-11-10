package com.tpstream.player

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NetworkTest{
    lateinit var mockWebServer: MockWebServer
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()



}