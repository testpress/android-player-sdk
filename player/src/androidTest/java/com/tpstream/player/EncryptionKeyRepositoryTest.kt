package com.tpstream.player

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptionKeyRepositoryTest {
    private lateinit var encryptionKeyRepository: EncryptionKeyRepository
    private val encryptionKey = byteArrayOf(91, 49, 48, 50, 44, 32, 49, 49, 49, 44, 32, 49, 49, 49, 93)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        encryptionKeyRepository = EncryptionKeyRepository(context)
    }

    @Test
    fun testSaveAndGetEncryptionKey() {
        val encryptionKeyUrl = "https://example.com/encryption-key"
        encryptionKeyRepository.save(encryptionKey.contentToString(), encryptionKeyUrl)
        val retrievedKey = encryptionKeyRepository.get(encryptionKeyUrl)
        assertNotNull(retrievedKey)
        assertArrayEquals(encryptionKey, retrievedKey)
    }

    @Test
    fun testHasEncryptionKey() {
        val encryptionKeyUrl = "https://example.com/encryption-key"
        encryptionKeyRepository.save(encryptionKey.contentToString(), encryptionKeyUrl)
        assertTrue(encryptionKeyRepository.hasEncryptionKey(encryptionKeyUrl))
    }

    @Test
    fun testDeleteEncryptionKey() {
        val encryptionKeyUrl = "https://example.com/encryption-key"
        encryptionKeyRepository.save(encryptionKey.contentToString(), encryptionKeyUrl)
        assertTrue(encryptionKeyRepository.hasEncryptionKey(encryptionKeyUrl))
        encryptionKeyRepository.delete(encryptionKeyUrl)
        assertFalse(encryptionKeyRepository.hasEncryptionKey(encryptionKeyUrl))
    }

    @Test
    fun testReceivedNullForWrongUrlEncryptionKey() {
        val encryptionKeyUrl = "https://example.com/encryption-key"
        encryptionKeyRepository.save(encryptionKey.contentToString(), encryptionKeyUrl)
        assertFalse(encryptionKeyRepository.hasEncryptionKey("encryptionKeyUrl"))
    }
}