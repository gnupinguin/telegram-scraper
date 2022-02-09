package io.github.gnupinguin.tlgscraper.scraper


import io.github.gnupinguin.tlgscraper.scraper.persistence.model.Channel
import io.github.gnupinguin.tlgscraper.scraper.persistence.repository.ChannelRepository
import io.github.gnupinguin.tlgscraper.scraper.utils.ScraperProfiles
import org.apache.commons.lang3.RandomStringUtils
import org.junit.Assert.assertEquals
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
    lateinit var channelRepository: ChannelRepository

    @Test
    fun testDb() {
        val name = RandomStringUtils.random(32)
        val chat = Channel(null, name, "test", "test", 1, emptyList())
        channelRepository.save(chat)
        val list = channelRepository.getChannelsByNameIsIn(listOf(name))
        assertEquals(1, list.size)
        channelRepository.delete(list.first())
    }

}