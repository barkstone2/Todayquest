package dailyquest.batch

import dailyquest.properties.BatchContextProperties
import dailyquest.properties.BatchParameterProperties
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import dailyquest.quest.repository.QuestRepository
import dailyquest.search.repository.QuestIndexRepository
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.User
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.StepSynchronizationManager
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.MetaDataInstanceFactory
import org.springframework.batch.test.StepScopeTestExecutionListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.TestExecutionListeners
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@DisplayName("퀘스트 배치 구성 요소 테스트")
@TestExecutionListeners(StepScopeTestExecutionListener::class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@SpringBootTest
class QuestBatchComponentTest @Autowired constructor(
    private val questRepository: QuestRepository,
    private val questResetReader: RepositoryItemReader<Quest>,
    private val questDeadLineReader: RepositoryItemReader<Quest>,
    private val questFailProcessor: FunctionItemProcessor<Quest, Quest>,
    private val questWriter: RepositoryItemWriter<Quest>,
    private val entityManager: EntityManager,
    private val transactionTemplate: TransactionTemplate,
    private val jobRepository: JobRepository,
) {

    @MockBean
    private lateinit var batchParameterProperties: BatchParameterProperties
    @MockBean
    private lateinit var batchContextProperties: BatchContextProperties
    @MockBean
    lateinit var questIndexRepository: QuestIndexRepository
    lateinit var jobRepositoryTestUtils: JobRepositoryTestUtils

    lateinit var testUser: User
    lateinit var anotherUser: User

    @BeforeEach
    fun init() {
        transactionTemplate.executeWithoutResult {
            entityManager.createQuery("delete from Quest").executeUpdate()
            entityManager.createQuery("delete from User").executeUpdate()
        }
        jobRepositoryTestUtils = JobRepositoryTestUtils(jobRepository)
        jobRepositoryTestUtils.removeJobExecutions()

        testUser = User("testUser", "testUser", ProviderType.GOOGLE)
        anotherUser = User("anotherUser", "anotherUser", ProviderType.GOOGLE)

        transactionTemplate.executeWithoutResult {
            entityManager.persist(testUser)
            entityManager.persist(anotherUser)
            entityManager.clear()
        }
    }

    @DisplayName("questResetReader 동작 시 오늘 resetTime 이전에 등록된 퀘스트만 조회된다")
    @Test
    fun `questResetReader 동작 시 오늘 resetTime 이전에 등록된 퀘스트만 조회된다`() {
        //given
        transactionTemplate.executeWithoutResult {
            val query = entityManager
                .createNativeQuery("insert into quest (quest_id, created_date, description, user_quest_seq, state, title, type, user_id) values (default, ?, '', 1, 'PROCEED', '', 'MAIN', ?)")
                .setParameter(2, testUser.id)

            val resetDate = LocalDate.of(2022, 12, 1)
            val resetDateTime = LocalDateTime.of(resetDate, LocalTime.of(6, 0))

            val datetime1 = LocalDateTime.of(resetDate, LocalTime.of(5, 59))
            val datetime2 = LocalDateTime.of(resetDate, LocalTime.of(6, 0))
            val datetime3 = LocalDateTime.of(resetDate, LocalTime.of(6, 1))

            query.setParameter(1, datetime1).executeUpdate()
            query.setParameter(1, datetime2).executeUpdate()
            query.setParameter(1, datetime3).executeUpdate()

            val jobParameters = JobParametersBuilder()
                .addString("resetDateTime", resetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .toJobParameters()

            val stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters)
            StepSynchronizationManager.register(stepExecution)

            //when
            val readQuests = mutableListOf<Quest>()

            while (true) {
                val quest = questResetReader.read() ?: break
                readQuests.add(quest)
            }

            //then
            assertThat(readQuests).noneMatch { q -> q.createdDate?.isAfter(resetDateTime) == true }
        }
    }

    @DisplayName("questDeadLineReader 동작 시 deadLine이 targetDate 이전인 퀘스트만 조회된다")
    @Test
    fun `questDeadLineReader 동작 시 deadLine이 targetDate 이전인 퀘스트만 조회된다`() {
        //given

        val currentTime = LocalDateTime.now().withSecond(0).withNano(0)

        val mustContainList = listOf(
            questRepository.save(Quest("", "", testUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
            questRepository.save(Quest("", "", testUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
            questRepository.save(Quest("", "", anotherUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
            questRepository.save(Quest("", "", anotherUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
        )

        val mustNotContainList = listOf(
            questRepository.save(Quest("", "", testUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.plusMinutes(1))),
            questRepository.save(Quest("", "", testUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
            questRepository.save(Quest("", "", anotherUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.plusMinutes(1))),
            questRepository.save(Quest("", "", anotherUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
        )

        val jobParameters = JobParametersBuilder()
            .addString("targetDate", currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .toJobParameters()

        val stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters)
        StepSynchronizationManager.register(stepExecution)

        //when
        val readQuests = mutableListOf<Quest>()

        while (true) {
            val quest = questDeadLineReader.read() ?: break
            readQuests.add(quest)
        }

        //then
        assertThat(readQuests).containsAll(mustContainList)
        assertThat(readQuests).doesNotContainAnyElementsOf(mustNotContainList)
    }

    @DisplayName("failProcessor 동작 후 퀘스트가 fail 상태로 변경된다")
    @Test
    fun `failProcessor 동작 후 퀘스트가 fail 상태로 변경된다`() {
        //given
        val quest = Quest("", "", testUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, null)

        //when
        questFailProcessor.process(quest)

        //then
        assertThat(quest.state).isEqualTo(QuestState.FAIL)
    }

    @DisplayName("questWriter 동작 시 quest 상태가 저장된다")
    @Test
    fun `questWriter 동작 시 quest 상태가 저장된다`() {
        //given
        val quest = Quest("", "", testUser.id, 0L, QuestState.PROCEED, QuestType.MAIN, null)

        //when
        questWriter.write(Chunk(quest))

        //then
        assertThat(quest.id).isNotNull().isNotEqualTo(0L)
    }
}