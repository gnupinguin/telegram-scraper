package io.github.gnupinguin.tlgscraper.scraper

import io.github.gnupinguin.tlgscraper.scraper.telegram.TelegramWebClient
import io.github.gnupinguin.tlgscraper.scraper.utils.Profiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@ActiveProfiles(Profiles.LOCAL)
@RunWith(SpringRunner::class)
class IntegrationTest {

    @Autowired
    lateinit var telegramWebClient: TelegramWebClient

    @Test
    fun testChannelFound() {
        val channel = telegramWebClient.searchChannel("nexta_live")
        assertNotNull(channel)
        assertEquals(channel.entity.name, "nexta_live")
    }

    @Test
    fun testMessages() {
        val messages = telegramWebClient.getLastMessages("nexta_live", 100)
        assertNotNull(messages)
    }


}