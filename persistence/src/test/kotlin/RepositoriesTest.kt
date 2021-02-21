import io.github.gnupinguin.tlgscraper.db.mappers.*
import io.github.gnupinguin.tlgscraper.db.orm.DbManager
import io.github.gnupinguin.tlgscraper.db.orm.DbProperties
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutorImpl
import io.github.gnupinguin.tlgscraper.db.repository.*
import io.github.gnupinguin.tlgscraper.model.db.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*

/**
 * for correct running you can pull (https://github.com/testcontainers/testcontainers-java/issues/3574):
 * testcontainers/ryuk:0.3.0
 * alpine:3.5
 */
class RepositoriesTest {

    @Rule
    @JvmField
    var psqlContainer = PostgreSQLContainer<Nothing>("postgres:latest").apply {
        withDatabaseName("db")
        withUsername("password")
        withPassword("pass")
        withFileSystemBind(this::class.java.classLoader.getResource("schema")?.path, "/docker-entrypoint-initdb.d/")
    }

    private lateinit var chatRepository: ChatRepositoryImpl
    private lateinit var messageRepository: MessageRepositoryImpl
    private lateinit var mentionRepository: MentionRepositoryImpl
    private lateinit var linkRepository: LinkRepositoryImpl
    private lateinit var hashtagRepository: HashTagRepositoryImpl

    private lateinit var utilsRepository: UtilsRepository

    private lateinit var chat: Chat
    private lateinit var message: Message


    @Before
    fun setUp() {
        val props = DbProperties(
            psqlContainer.jdbcUrl,
            psqlContainer.username,
            psqlContainer.password
        )
        val manager = DbManager(props)
        val queryExecutor = QueryExecutorImpl(manager)

        chatRepository =
            ChatRepositoryImpl(
                queryExecutor,
                ChatSqlEntityMapper()
            )
        messageRepository = MessageRepositoryImpl(
            queryExecutor,
            MessageSqlEntityMapper()
        )
        mentionRepository = MentionRepositoryImpl(
            queryExecutor,
            MentionSqlEntityMapper()
        )
        linkRepository =
            LinkRepositoryImpl(
                queryExecutor,
                LinkSqlEntityMapper()
            )
        hashtagRepository = HashTagRepositoryImpl(
            queryExecutor,
            HashTagSqlEntityMapper()
        )

        utilsRepository =
            UtilsRepository(queryExecutor)

        chat = getChat()
        message = getMessage(chat)
    }

    private fun clearRepositories() {
        hashtagRepository.delete()
        linkRepository.delete()
        mentionRepository.delete()
        messageRepository.delete()
        chatRepository.delete()
    }

    @Test
    fun testChatRepository() {
        clearRepositories()
        val secondChat = Chat(2, "second", "dsc", 1)
        chatRepository.save(listOf(chat, secondChat))
        val clone = chatRepository.get(chat.id)
        assertEquals(chat, clone)

        val clone1 = chatRepository.get(secondChat.id)
        assertEquals(secondChat, clone1)

        chatRepository.delete(listOf(secondChat.id, chat.id))

        assertNull(chatRepository.get(chat.id))
        assertNull(chatRepository.get(secondChat.id))
    }

    @Test
    fun testChatRepositorySelectByNames() {
        clearRepositories()
        val secondChat = Chat(2, "second", "dsc", 1)
        val chats = listOf(chat, secondChat)
        chatRepository.save(chats)

        val foundChats = chatRepository.getChatsByNames(listOf(chat.name, secondChat.name))
        assertEquals(chats, foundChats)
    }

    @Test
    fun testMessageRepository() {
        clearRepositories()
        chatRepository.save(chat)

        assertNull(message.internalId)
        messageRepository.save(message)
        assertNotNull(message.internalId)
        val clone = messageRepository.get(message.internalId)

        assertEquals(message, clone)
        messageRepository.delete(listOf(message.internalId))
        assertNull(messageRepository.get(message.internalId))
    }

    @Test
    fun testLinkRepository() {
        clearRepositories()
        chatRepository.save(chat)
        messageRepository.save(message)

        val link = Link(null, message.internalId, "url")
        linkRepository.save(link)
        assertNotNull(link.id)
        val clone = linkRepository.get(link.id)

        assertEquals(link, clone)
        linkRepository.delete(listOf(link.id))
        assertNull(linkRepository.get(link.id))
    }

    @Test
    fun testMentionRepository() {
        clearRepositories()
        chatRepository.save(chat)
        messageRepository.save(message)

        val mention =
            Mention(null, message.internalId, "mention")
        mentionRepository.save(mention)
        assertNotNull(mention.id)
        val clone = mentionRepository.get(mention.id)

        assertEquals(mention, clone)
        mentionRepository.delete(listOf(mention.id))
        assertNull(mentionRepository.get(mention.id))
    }

    @Test
    fun testHashTagRepository() {
        chatRepository.save(chat)
        messageRepository.save(message)

        val hashTag =
            HashTag(null, message.internalId, "tag")
        hashtagRepository.save(hashTag)
        assertNotNull(hashTag.id)
        val clone = hashtagRepository.get(hashTag.id)

        assertEquals(hashTag, clone)
        hashtagRepository.delete(listOf(hashTag.id))
        assertNull(hashtagRepository.get(hashTag.id))
    }

    @Test
    fun testDbConnection() {
        assertNotNull(utilsRepository.currentDbDate)
    }

    private fun getChat() =
        Chat(1, "chat", "description", 1)

    private fun getMessage(chat: Chat) =
        Message(
            null, chat.id,
            1, 1, 1, 1,
            1, "some_text", date(), date(), 12
        )

    private fun date() = java.sql.Timestamp(Date().time)

}