import io.github.gnupinguin.tlgscraper.db.DbConfiguration
import io.github.gnupinguin.tlgscraper.db.mappers.ChatSqlEntityMapper
import io.github.gnupinguin.tlgscraper.db.mappers.MessageSqlEntityMapper
import io.github.gnupinguin.tlgscraper.db.orm.DataSourceDbConnectionProvider
import io.github.gnupinguin.tlgscraper.db.orm.DbManager
import io.github.gnupinguin.tlgscraper.db.orm.DbProperties
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutorImpl
import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus
import io.github.gnupinguin.tlgscraper.db.queue.messages.MessageTask
import io.github.gnupinguin.tlgscraper.db.queue.messages.MessagesTaskQueueImpl
import io.github.gnupinguin.tlgscraper.db.repository.ChatRepositoryImpl
import io.github.gnupinguin.tlgscraper.db.repository.MessageRepositoryImpl
import io.github.gnupinguin.tlgscraper.db.repository.UtilsRepository
import io.github.gnupinguin.tlgscraper.model.db.Chat
import io.github.gnupinguin.tlgscraper.model.db.Message
import io.github.gnupinguin.tlgscraper.model.scraper.MessageType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*

class MessageTaskIntegrationTest {

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
    private lateinit var messageTaskQueue: MessagesTaskQueueImpl


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
        messageTaskQueue = MessagesTaskQueueImpl(queryExecutor)


        utilsRepository =
            UtilsRepository(queryExecutor)

        chat = getChat()
        message = getMessage(chat)

        chatRepository.save(chat)
        messageRepository.save(message)
    }

    @After
    fun clearRepositories() {
        messageRepository.delete()
        chatRepository.delete()
    }

    @Test
    fun testAddNewAndPoll() {
        messageTaskQueue.add(listOf(messageTask()))

        val tasks = messageTaskQueue.poll(2)
        assertEquals(1, tasks.size)
        val task = tasks.first()
        assertEquals(chat.name, task.entity.channel)
        assertEquals(message.internalId, task.internalMessageId)
        assertEquals(message.id, task.entity.beforeMessageId)
        assertEquals(TaskStatus.Active, task.status)
    }

    @Test
    fun testSelectOnlyInitial() {
        messageTaskQueue.add(listOf(messageTask()))

        val tasks = messageTaskQueue.poll(2)
        assertEquals(1, tasks.size)
        assertTrue(messageTaskQueue.poll(2).isEmpty())
    }

    @Test
    fun testUpdateStatus() {
        messageTaskQueue.add(listOf(messageTask()))
        val tasks1 = messageTaskQueue.poll(1)
        assertEquals(1, tasks1.size)

        val old = tasks1.first()
        messageTaskQueue.update(listOf(old.withStatus(TaskStatus.Initial)))
        val tasks2 = messageTaskQueue.poll(1)
        assertEquals(1, tasks2.size)
        val fresh = tasks2.first()
        assertEquals(old, fresh)
    }

    private fun messageTask() = MessageTask(message.internalId, TaskStatus.Initial,
        MessageTask.MessageTaskInfo(chat.name, message.id))
    private fun getChat() =
        Chat(1, "chat", "title", "description", 1, null)

    private fun getMessage(chat: Chat) =
        Message(null, chat, 1, MessageType.Text, "some_text", date(), date(), 12, null, null, emptySet(), emptySet(), emptySet())

    private fun date() = java.sql.Timestamp(Date().time)

}