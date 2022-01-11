package io.github.gnupinguin.tlgscraper.scraper

import io.github.gnupinguin.tlgscraper.scraper.proxy.TorProxyProvider
import io.github.gnupinguin.tlgscraper.scraper.telegram.TelegramWebClient
import io.github.gnupinguin.tlgscraper.scraper.utils.ScraperProfiles
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.containers.PostgreSQLContainer
import java.io.File

//Check only scraper requests functionality with mocked queue and database
@SpringBootTest
@ActiveProfiles(profiles = [ScraperProfiles.LOCAL])
@RunWith(SpringRunner::class)
@ContextConfiguration(initializers = [ScraperModuleIntegrationTest.Initializer::class])
class ScraperModuleIntegrationTest {

    companion object {

        //TODO add fat jar
        private val schemaPath = File("").absoluteFile.parentFile.absolutePath + File.separator + "persistence/src/main/resources/schema".replace("/", File.separator)

        @JvmField
        @ClassRule
        var psqlContainer = PostgreSQLContainer<Nothing>("postgres:latest").apply {
            withDatabaseName("db")
            withUsername("user")
            withPassword("password")
            withFileSystemBind(schemaPath, "/docker-entrypoint-initdb.d/")
        }
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext?> {
        override fun initialize(applicationContext: ConfigurableApplicationContext?) {
            TestPropertyValues.of(
                "db.url=${psqlContainer.jdbcUrl}",
                "db.username=${psqlContainer.username}",
                "db.password=${psqlContainer.password}"
            ).applyTo(applicationContext?.environment)
        }
    }

    @Autowired
    lateinit var telegramWebClient: TelegramWebClient

    @Autowired
    lateinit var torProxySource: TorProxyProvider

    @Test
    fun testTelegramChannelAvailability() {
        val channel = telegramWebClient.searchChannel("nexta_live")
        Assert.assertNotNull(channel)
        Assert.assertEquals(channel?.entity?.name, "nexta_live")
    }

    @Test
    fun testTelegramPostsAvailability() {
        val messages = telegramWebClient.getLastMessages("nexta_live", 20)
        assertTrue(messages.size >= 20)
    }

    @Test
    fun testTorProxy() {
        assertTrue(torProxySource.forceUpdate())
    }

    @Test
    fun findLowercaseName() {
        val lastMessages = telegramWebClient.getLastMessages("MRZLKVk", 10)//should be MRZLKVK
        assertTrue(lastMessages.size >= 10)
    }

}