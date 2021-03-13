import io.github.gnupinguin.tlgscraper.db.DbConfiguration
import io.github.gnupinguin.tlgscraper.db.mappers.*
import io.github.gnupinguin.tlgscraper.db.orm.DataSourceDbConnectionProvider
import io.github.gnupinguin.tlgscraper.db.orm.DbManager
import io.github.gnupinguin.tlgscraper.db.orm.DbProperties
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutorImpl
import io.github.gnupinguin.tlgscraper.db.repository.*
import io.github.gnupinguin.tlgscraper.model.db.*
import io.github.gnupinguin.tlgscraper.model.scraper.MessageType
import org.junit.After
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
    private lateinit var forwardingRepository: ForwardingRepositoryImpl
    private lateinit var replyingRepository: ReplyingRepositoryImpl

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
        val dbConfiguration = DbConfiguration()
        val hikariConfig = dbConfiguration.hikariConfig(props)
        val dataSource = dbConfiguration.hikariDataSource(hikariConfig)
        val manager = DbManager(DataSourceDbConnectionProvider(dataSource))
        val queryExecutor = QueryExecutorImpl(manager)

        chatRepository = ChatRepositoryImpl(queryExecutor, ChatSqlEntityMapper())
        messageRepository = MessageRepositoryImpl(queryExecutor, MessageSqlEntityMapper())
        mentionRepository = MentionRepositoryImpl(queryExecutor, MentionSqlEntityMapper())
        linkRepository = LinkRepositoryImpl(queryExecutor, LinkSqlEntityMapper())
        hashtagRepository = HashTagRepositoryImpl(queryExecutor, HashTagSqlEntityMapper())
        forwardingRepository = ForwardingRepositoryImpl(queryExecutor, ForwardingSqlEntityMapper())
        replyingRepository = ReplyingRepositoryImpl(queryExecutor, ReplyingSqlEntityMapper())

        utilsRepository =
            UtilsRepository(queryExecutor)

        chat = getChat()
        message = getMessage(chat)
    }

    @After
    fun clearRepositories() {
        forwardingRepository.delete()
        replyingRepository.delete()
        hashtagRepository.delete()
        linkRepository.delete()
        mentionRepository.delete()
        messageRepository.delete()
        chatRepository.delete()
    }

    @Test
    fun testChatRepository() {
        clearRepositories()
        val secondChat = Chat(null, "second", "title", "dsc", 1, null)
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
        val secondChat = Chat(null, "second", "title", "dsc", 1, null)
        val chats = listOf(chat, secondChat)
        chatRepository.save(chats)

        val foundChats = chatRepository.getChatsByNames(listOf(chat.name, secondChat.name))
        assertEquals(chats, foundChats)
    }

    @Test
    fun testMessageRepository() {
        chatRepository.save(chat)

        assertNull(message.internalId)
        messageRepository.save(message)
        assertNotNull(message.internalId)
        val clone = messageRepository.get(message.internalId)

        assertEquals(message.internalId, clone!!.internalId)
        messageRepository.delete(listOf(message.internalId))
        assertNull(messageRepository.get(message.internalId))
    }

    @Test
    fun testLinkRepository() {
        chatRepository.save(chat)
        messageRepository.save(message)

        val link = Link(message, 1, "url")
        linkRepository.save(link)
        assertNotNull(link.id)
        val clone = linkRepository.get(link.id)

        assertEquals(link.id, clone!!.id)
        linkRepository.delete(listOf(link.id))
        assertNull(linkRepository.get(link.id))
    }

    @Test
    fun testMentionRepository() {
        chatRepository.save(chat)
        messageRepository.save(message)

        val mention =
            Mention(message, 1, "mention")
        mentionRepository.save(mention)
        assertNotNull(mention.id)
        val clone = mentionRepository.get(mention.id)

        assertEquals(mention.id, clone!!.id)
        mentionRepository.delete(listOf(mention.id))
        assertNull(mentionRepository.get(mention.id))
    }

    @Test
    fun testHashTagRepository() {
        chatRepository.save(chat)
        messageRepository.save(message)

        val hashTag =
            HashTag(message, 1, "tag")
        hashtagRepository.save(hashTag)
        assertNotNull(hashTag.id)
        val clone = hashtagRepository.get(hashTag.id)

        assertEquals(hashTag.id, clone!!.id)
        hashtagRepository.delete(listOf(hashTag.id))
        assertNull(hashtagRepository.get(hashTag.id))
    }

    @Test
    fun testDbConnection() {
        assertNotNull(utilsRepository.currentDbDate)
    }

    @Test
    fun testForwarding() {
        chatRepository.save(chat)
        messageRepository.save(message)

        val forwarding = Forwarding(message, "anotherChat", 1)
        forwardingRepository.save(forwarding)
        val clone = forwardingRepository.get(forwarding.message.internalId)

        assertEquals(forwarding.forwardedFromMessageId, clone!!.forwardedFromMessageId)
        forwardingRepository.delete(listOf(forwarding.message.internalId))
        assertNull(forwardingRepository.get(forwarding.message.internalId))
    }

    @Test
    fun testReplying() {
        chatRepository.save(chat)
        messageRepository.save(message)

        val replying = Replying(message, 1)
        replyingRepository.save(replying)
        val clone = replyingRepository.get(replying.message.internalId)

        assertEquals(replying.replyToMessageId, clone!!.replyToMessageId)
        replyingRepository.delete(listOf(replying.message.internalId))
        assertNull(replyingRepository.get(replying.message.internalId))
    }

    private fun getChat() =
        Chat(1, "chat", "title", "description", 1, null)

    private fun getMessage(chat: Chat) =
        Message(null, chat, 1, MessageType.Text, "some_text", date(), date(), 12, null, null, emptySet(), emptySet(), emptySet())

    private fun date() = java.sql.Timestamp(Date().time)

}