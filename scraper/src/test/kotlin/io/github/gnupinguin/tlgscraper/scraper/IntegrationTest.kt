package io.github.gnupinguin.tlgscraper.scraper

import io.github.gnupinguin.tlgscraper.scraper.notification.TelegramBotNotificator
import io.github.gnupinguin.tlgscraper.scraper.proxy.TorProxyProvider
import io.github.gnupinguin.tlgscraper.scraper.telegram.TelegramWebClient
import io.github.gnupinguin.tlgscraper.scraper.utils.Profiles
import org.junit.Assert.*
import org.junit.Ignore
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

    @Autowired
    lateinit var torProxySource: TorProxyProvider

    @Autowired
    lateinit var notificator: TelegramBotNotificator

    @Test
    fun testChannelFound() {
        val channel = telegramWebClient.searchChannel("nexta_live")
        assertNotNull(channel)
        assertEquals(channel?.entity?.name, "nexta_live")
    }

    @Test
    fun testMessages() {
        val messages = telegramWebClient.getLastMessages("nexta_live", 20)
        assertTrue(messages.size >= 20)
    }

    @Test
    fun testTorProxy() {
        assertTrue(torProxySource.forceUpdate())
    }

    @Test
    @Ignore
    fun testNotification() {
        assertTrue(notificator.approveRestoration(listOf("a", "b", "c")))
    }

    @Test
    fun findLowercaseName() {
        val lastMessages = telegramWebClient.getLastMessages("MRZLKVk", 10)//should be MRZLKVK
        assertTrue(lastMessages.size >= 10)
    }

}