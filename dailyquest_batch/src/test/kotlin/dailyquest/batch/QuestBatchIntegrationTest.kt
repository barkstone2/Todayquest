package dailyquest.batch

import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import dailyquest.quest.repository.QuestRepository
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DisplayName("퀘스트 배치 통합 테스트")
@SpringBootTest
class QuestBatchIntegrationTest @Autowired constructor(
    private val questResetBatchJob: Job,
    private val questDeadLineBatchJob: Job,
    private val questRepository: QuestRepository,
    private val jobLauncher: JobLauncher,
    private val entityManager: EntityManager,
    private val transactionTemplate: TransactionTemplate,
) {
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils
    private lateinit var testUser: UserInfo
    private lateinit var anotherUser: UserInfo

    @BeforeEach
    fun init() {
        testUser = UserInfo("testUser", "testUser", ProviderType.GOOGLE)
        anotherUser = UserInfo("anotherUser", "anotherUser", ProviderType.GOOGLE)
        anotherUser.updateResetTime(9, LocalDateTime.now())

        transactionTemplate.executeWithoutResult {
            entityManager.persist(testUser)
            entityManager.persist(anotherUser)
            entityManager.clear()
        }

        jobLauncherTestUtils = JobLauncherTestUtils()
        jobLauncherTestUtils.jobLauncher = jobLauncher
    }

    @AfterEach
    fun clear() {
        transactionTemplate.executeWithoutResult {
            entityManager.createQuery("delete from Quest").executeUpdate()
            entityManager.createQuery("delete from UserInfo").executeUpdate()
        }
    }

    @DisplayName("퀘스트 초기화 배치 동작 시 resetTime이 일치하는 사용자의 퀘스트만 상태를 변경한다")
    @Test
    fun onlyChangeQuestOfUserWhoHaveSameResetTime() {
        //given
        val resetTime = testUser.resetTime

        val savedIds = listOf(
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
        ).map(Quest::id)

        val notProcessedId = questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)).id

        jobLauncherTestUtils.job = questResetBatchJob

        val jobParameters = JobParametersBuilder()
            .addString("resetTime", resetTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .toJobParameters()

        //when
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        //then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

        val savedQuests = questRepository.findAllById(savedIds)
        val notProcessedQuest = questRepository.findById(notProcessedId)
        assertThat(savedQuests).allMatch { q -> q.state == QuestState.FAIL }
        assertThat(notProcessedQuest).isNotEmpty
        assertThat(notProcessedQuest.get().state).isEqualTo(QuestState.PROCEED)
    }

    @DisplayName("퀘스트 데드라인 배치 동작 시 데드라인이 잡 파라미터보다 같거나 이전인 퀘스트만 실패 처리된다")
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

}