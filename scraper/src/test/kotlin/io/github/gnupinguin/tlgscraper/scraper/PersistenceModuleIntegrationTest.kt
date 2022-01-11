package io.github.gnupinguin.tlgscraper.scraper

import io.github.gnupinguin.tlgscraper.db.repository.ChatRepository
import io.github.gnupinguin.tlgscraper.model.db.Chat
import io.github.gnupinguin.tlgscraper.scraper.utils.ScraperProfiles
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner


@SpringBootTest
@ActiveProfiles(profiles = [ScraperProfiles.LOCAL])
@RunWith(SpringRunner::class)
class PersistenceModuleIntegrationTest {

    @Autowired
    lateinit var chatRepository: ChatRepository

    @Test
    fun testDb() {
        val name = RandomStringUtils.random(32)
        val chat = Chat(null, name, "test", "test", 1, emptyList())
        chatRepository.save(chat)
        val list = chatRepository.getChatsByNames(listOf(name))
        assertEquals(1, list.size)
        assertTrue(chatRepository.delete(listOf(list.first().id)))
    }

}