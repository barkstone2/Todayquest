package dailyquest.batch

import dailyquest.common.CustomElasticsearchContainer
import dailyquest.properties.BatchContextProperties
import dailyquest.properties.BatchParameterProperties
import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import dailyquest.quest.repository.QuestLogRepository
import dailyquest.quest.repository.QuestRepository
import dailyquest.search.repository.QuestIndexRepository
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.User
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Testcontainers
@DisplayName("퀘스트 배치 통합 테스트")
@SpringBootTest
class QuestBatchIntegrationTest @Autowired constructor(
    private val questResetBatchJob: Job,
    private val questDeadLineBatchJob: Job,
    private val questRepository: QuestRepository,
    private val questLogRepository: QuestLogRepository,
    private val questIndexRepository: QuestIndexRepository,
    private val jobLauncher: JobLauncher,
    private val jobRepository: JobRepository,
    private val entityManager: EntityManager,
    private val transactionTemplate: TransactionTemplate,
) {
    @MockBean
    private lateinit var batchParameterProperties: BatchParameterProperties
    @MockBean
    private lateinit var batchContextProperties: BatchContextProperties
    private lateinit var jobRepositoryTestUtils: JobRepositoryTestUtils
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils
    private lateinit var testUser: User
    private lateinit var anotherUser: User

    companion object {
        @JvmStatic
        @Container
        val elasticsearch = CustomElasticsearchContainer()
    }

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

        jobLauncherTestUtils = JobLauncherTestUtils()
        jobLauncherTestUtils.jobLauncher = jobLauncher
    }

    @DisplayName("퀘스트 초기화 배치 동작 시")
    @Nested
    inner class TestQuestRestBatch {
        @DisplayName("오늘 resetTime 이전에 등록된 퀘스트만 상태를 변경한다")
        @Test
        fun onlyChangeQuestOfCreatedBeforeResetTimeOfToday() {
            //given
            val filterTitle = "resetTimeTestTarget"

            val resetDate = LocalDate.of(2022, 12, 1)
            val resetDateTime = LocalDateTime.of(resetDate, LocalTime.of(6, 0))

            val datetime1 = LocalDateTime.of(resetDate, LocalTime.of(5, 59))
            val datetime2 = LocalDateTime.of(resetDate, LocalTime.of(6, 0))
            val datetime3 = LocalDateTime.of(resetDate, LocalTime.of(6, 1))

            transactionTemplate.executeWithoutResult {
                val query = entityManager
                    .createNativeQuery("insert into quest (quest_id, created_date, description, user_quest_seq, state, title, type, user_id) values (default, ?, '', 1, 'PROCEED', ?, 'MAIN', ?)")
                    .setParameter(3, testUser.id)

                query.setParameter(1, datetime1).setParameter(2, filterTitle).executeUpdate()
                query.setParameter(1, datetime2).setParameter(2, filterTitle).executeUpdate()
                query.setParameter(1, datetime3).setParameter(2, filterTitle).executeUpdate()
            }

            jobLauncherTestUtils.job = questResetBatchJob

            val jobParameters = JobParametersBuilder()
                .addString("resetDateTime", resetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .toJobParameters()

            //when
            val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

            //then
            assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

            val filteredQuest = questRepository.findAll().filter { q -> q.title == filterTitle }
            assertThat(filteredQuest).noneMatch { q ->
                q.createdDate?.isAfter(resetDateTime) == true && q.state == QuestState.FAIL ||
                        q.createdDate?.isBefore(resetDateTime) == true && q.state == QuestState.PROCEED
            }
        }

        @DisplayName("작업 종료 후 QuestLog 를 저장한다")
        @Test
        fun saveQuestLogAfterBatch() {
            //given
            val filterTitle = "resetTimeAfterLogTestTarget"

            val resetDate = LocalDate.of(2022, 12, 1)
            val resetDateTime = LocalDateTime.of(resetDate, LocalTime.of(6, 0))

            transactionTemplate.executeWithoutResult {
                val query = entityManager
                    .createNativeQuery("insert into quest (quest_id, created_date, description, user_quest_seq, state, title, type, user_id) values (default, ?, '', 1, 'PROCEED', ?, 'MAIN', ?)")
                    .setParameter(3, testUser.id)

                val datetime1 = LocalDateTime.of(resetDate, LocalTime.of(5, 58))
                val datetime2 = LocalDateTime.of(resetDate, LocalTime.of(5, 59))

                query.setParameter(1, datetime1).setParameter(2, filterTitle).executeUpdate()
                query.setParameter(1, datetime2).setParameter(2, filterTitle).executeUpdate()
            }

                jobLauncherTestUtils.job = questResetBatchJob

                val jobParameters = JobParametersBuilder()
                    .addString(
                        "resetDateTime",
                        resetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
                    .toJobParameters()

                //when
                jobLauncherTestUtils.launchJob(jobParameters)

                //then
                val filteredQuestId = questRepository.findAll().filter { it.title == filterTitle }.map { it.id }
                val logs = questLogRepository.findAll()
                assertThat(logs).isNotEmpty
                assertThat(logs.map { it.questId }.toList()).containsAll(filteredQuestId)
            }

        @DisplayName("작업 종료 후 QuestDocument 를 업데이트 한다")
        @Test
        fun updateQuestDocumentAfterBatch() {
            //given
            val filterTitle = "resetTimeAfterDocumentTestTarget"

            val resetDate = LocalDate.of(2022, 12, 1)
            val resetDateTime = LocalDateTime.of(resetDate, LocalTime.of(6, 0))

            val datetime1 = LocalDateTime.of(resetDate, LocalTime.of(5, 58))
            val datetime2 = LocalDateTime.of(resetDate, LocalTime.of(5, 59))

            transactionTemplate.executeWithoutResult {
                val query = entityManager
                    .createNativeQuery("insert into quest (quest_id, created_date, description, user_quest_seq, state, title, type, user_id) values (default, ?, '', 1, 'PROCEED', ?, 'MAIN', ?)")
                    .setParameter(3, testUser.id)

                query.setParameter(1, datetime1).setParameter(2, filterTitle).executeUpdate()
                query.setParameter(1, datetime2).setParameter(2, filterTitle).executeUpdate()
            }

            jobLauncherTestUtils.job = questResetBatchJob

            val jobParameters = JobParametersBuilder()
                .addString(
                    "resetDateTime",
                    resetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                )
                .toJobParameters()

            //when
            jobLauncherTestUtils.launchJob(jobParameters)

            //then
            val filteredQuestId = questRepository.findAll().filter { it.title == filterTitle }.map { it.id }
            val documents = questIndexRepository.findAll()
            assertThat(documents).isNotEmpty
            assertThat(documents.map { it.id }.toList()).containsAll(filteredQuestId)
        }
    }

    @DisplayName("퀘스트 데드라인 배치 동작 시")
    @Nested
    inner class DeadLineBatchTest {

        @DisplayName("데드라인이 잡 파라미터보다 같거나 이전인 퀘스트만 실패 처리된다")
        @Test
        fun onlyChangeThatHaveLteDeadLineThanJobParameter() {
            //given
            val currentTime = LocalDateTime.now().withSecond(0).withNano(0)

            val processedIds = listOf(
                questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
                questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
                questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
                questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
            ).map(Quest::id)

            val notProcessedIds = listOf(
                questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.plusMinutes(1))),
                questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
                questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.plusMinutes(1))),
                questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
            ).map(Quest::id)

            jobLauncherTestUtils.job = questDeadLineBatchJob

            val jobParameters = JobParametersBuilder()
                .addString("targetDate", currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .toJobParameters()

            //when
            val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

            //then
            assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

            val processedQuests = questRepository.findAllById(processedIds)
            assertThat(processedQuests).allMatch { q -> q.state == QuestState.FAIL }
            val notProcessedQuests = questRepository.findAllById(notProcessedIds)
            assertThat(notProcessedQuests).allMatch { q -> q.state == QuestState.PROCEED }
        }

        @DisplayName("작업 종료 후 QuestLog 를 저장한다")
        @Test
        fun saveQuestLogAfterBatch() {
            //given
            val currentTime = LocalDateTime.now().withSecond(0).withNano(0)

            val processedIds = listOf(
                questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
                questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
                questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
                questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
            ).map(Quest::id)

            jobLauncherTestUtils.job = questDeadLineBatchJob

            val jobParameters = JobParametersBuilder()
                .addString("targetDate", currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .toJobParameters()

            //when
            jobLauncherTestUtils.launchJob(jobParameters)

            //then
            val logs = questLogRepository.findAll()
            assertThat(logs).isNotEmpty
            assertThat(logs.map { it.questId }.toList()).containsAll(processedIds)
        }

        @DisplayName("작업 종료 후 QuestDocument 를 업데이트 한다")
        @Test
        fun updateQuestDocumentAfterBatch() {
            //given
            val currentTime = LocalDateTime.now().withSecond(0).withNano(0)

            val processedIds = listOf(
                questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
                questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
                questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
                questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
            ).map(Quest::id)

            jobLauncherTestUtils.job = questDeadLineBatchJob

            val jobParameters = JobParametersBuilder()
                .addString("targetDate", currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .toJobParameters()

            //when
            jobLauncherTestUtils.launchJob(jobParameters)

            //then
            val documents = questIndexRepository.findAll()
            assertThat(documents).isNotEmpty
            assertThat(documents.map { it.id }.toList()).containsAll(processedIds)
        }
    }


}