import io.github.gnupinguin.tlgscraper.db.DbConfiguration
import io.github.gnupinguin.tlgscraper.db.orm.DataSourceDbConnectionProvider
import io.github.gnupinguin.tlgscraper.db.orm.DbManager
import io.github.gnupinguin.tlgscraper.db.orm.DbProperties
import io.github.gnupinguin.tlgscraper.db.orm.QueryExecutorImpl
import io.github.gnupinguin.tlgscraper.db.queue.MentionTask
import io.github.gnupinguin.tlgscraper.db.queue.MentionTaskQueue
import io.github.gnupinguin.tlgscraper.db.queue.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.testcontainers.containers.PostgreSQLContainer

class QueueTest {

    @Rule
    @JvmField
    var psqlContainer = PostgreSQLContainer<Nothing>("postgres:latest").apply {
        withDatabaseName("db")
        withUsername("user")
        withPassword("password")
        withFileSystemBind(this::class.java.classLoader.getResource("schema")?.path, "/docker-entrypoint-initdb.d/")
    }

    private lateinit var mentionTaskQueue: MentionTaskQueue

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

        mentionTaskQueue = MentionTaskQueue(queryExecutor)
    }

    @Test
    fun testInitialValues() {
        val mentions = mentionTaskQueue.poll(100)

        assertTrue(mentions.isNotEmpty())
        assertEquals(47, mentions.size)
        assertTrue(mentions.map{it.name}.contains("navalny"))
        assertEquals(47, mentions.map{it.status.status}.sum())
    }

    @Test
    fun testBlocks(){
        mentionTaskQueue.poll(100)
        val mentions = mentionTaskQueue.poll(100);
        assertTrue(mentions.isEmpty())
    }

    @Test
    fun testInsert() {
        mentionTaskQueue.add(listOf(
            MentionTask(
                TaskStatus.Initial, "hello_test")
        ))

        val mentions = mentionTaskQueue.poll(100).map{it.name}
        assertTrue(mentions.contains("hello_test"))
    }

    @Test
    fun testInsertDuplicate() {
        val name = "hello_test"
        mentionTaskQueue.add(listOf(
            MentionTask(TaskStatus.Initial, name)
        ))
        mentionTaskQueue.add(listOf(
            MentionTask(TaskStatus.Initial, name)
        ))

        val mentions = mentionTaskQueue.poll(100).map{it.name}.filter { it == name }
        assertEquals(1, mentions.size)
        assertEquals(name, mentions.first())
    }

    @Test
    fun testUpdateStatus() {
        val mention1 = MentionTask(TaskStatus.Initial, "hello_test1")
        val mention2 = MentionTask(TaskStatus.Initial, "hello_test2")

        val testMentions = listOf(mention1, mention2)
        mentionTaskQueue.add(testMentions)
        mentionTaskQueue.updateStatuses(testMentions.map { it.status= TaskStatus.SuccessfullyProcessed; it })

        val mentions = mentionTaskQueue.poll(100)
            .map{it.name}
            .filter { testMentions.map {m -> m.name }.contains(it) }
        assertTrue(mentions.isEmpty())
    }

    @Test
    fun testLocked() {
        val poll = mentionTaskQueue.poll(5)
        val locked = mentionTaskQueue.getLocked(5)

        assertEquals(poll, locked)
    }

}