package com.tpstream.player

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageSaverTest {

    private lateinit var context: Context
    private lateinit var imageSaver: ImageSaver

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        imageSaver = ImageSaver(context)
    }

    @Test
    fun testSaveImageAndLoadImage() {
        // Image resolution - 320 x 180
        val thumbnailUrl =
            "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/chapter_contents/2152/834b2970ae694beab29129ca3793b147.png"
        val name = "saveImage"
        runBlocking {
            imageSaver.save(thumbnailUrl, name)
        }
        val savedImage = imageSaver.load(name)
        assertEquals(320, savedImage?.width)
        assertEquals(180, savedImage?.height)
    }

    @Test
    fun testDeleteImage() {
        val thumbnailUrl =
            "https://d36vpug2b5drql.cloudfront.net/institute/lmsdemo/chapter_contents/2152/834b2970ae694beab29129ca3793b147.png"
        val name = "deleteImage"
        runBlocking {
            imageSaver.save(thumbnailUrl, name)
        }
        val savedImage = imageSaver.load(name)
        assertNotNull(savedImage)
        val result = imageSaver.delete(name)
        assertEquals(true, result)
    }
}