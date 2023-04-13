package todayquest.batch

import org.assertj.core.api.Assertions.assertThat
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
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestState
import todayquest.quest.entity.QuestType
import todayquest.quest.repository.QuestRepository
import todayquest.user.repository.UserRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DisplayName("퀘스트 배치 통합 테스트")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestBatchIntegrationTest @Autowired constructor(
    private val questResetBatchJob: Job,
    private val questDeadLineBatchJob: Job,
    private val questRepository: QuestRepository,
    private val userRepository: UserRepository,
    private val jobLauncher: JobLauncher,
) {
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @BeforeEach
    fun init() {
        jobLauncherTestUtils = JobLauncherTestUtils()
        jobLauncherTestUtils.jobLauncher = jobLauncher
    }

    @DisplayName("퀘스트 초기화 배치 테스트")
    @Test
    fun resetBatchTest() {
        //given
        val user = userRepository.findById(1L).get()
        val resetTime = user.resetTime

        val savedIds = listOf(
            questRepository.save(Quest("t", "d", user, 1L, type = QuestType.MAIN)),
            questRepository.save(Quest("t", "d", user, 1L, type = QuestType.MAIN)),
            questRepository.save(Quest("t", "d", user, 1L, type = QuestType.MAIN)),
        ).map { q -> q.id }

        jobLauncherTestUtils.job = questResetBatchJob

        val jobParameters = JobParametersBuilder()
            .addString("resetTime", resetTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .toJobParameters()

        //when
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        //then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

        val savedQuests = questRepository.findAllById(savedIds)
        assertThat(savedQuests).allMatch { q -> q.state == QuestState.FAIL }
    }

    @DisplayName("퀘스트 데드라인 배치 테스트")
    @Test
    fun deadLineBatchTest() {
        //given
        val user = userRepository.findById(1L).get()
        val currentTime = LocalDateTime.now()

        val savedIds = listOf(
            questRepository.save(
                Quest("t", "d", user, 1L,
                    type = QuestType.MAIN,
                    deadline = currentTime.minusMinutes(1)
                )
            ),
            questRepository.save(
                Quest("t", "d", user, 1L,
                    type = QuestType.MAIN,
                    deadline = currentTime.minusMinutes(1)
                )
            ),
            questRepository.save(
                Quest("t", "d", user, 1L,
                    type = QuestType.MAIN,
                    deadline = currentTime.minusMinutes(1)
                )
            ),
        ).map { q -> q.id }

        jobLauncherTestUtils.job = questDeadLineBatchJob

        val jobParameters = JobParametersBuilder()
            .addString("targetDate", currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .toJobParameters()

        //when
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        //then
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)

        val savedQuests = questRepository.findAllById(savedIds)
        assertThat(savedQuests).allMatch { q -> q.state == QuestState.FAIL }
    }

}