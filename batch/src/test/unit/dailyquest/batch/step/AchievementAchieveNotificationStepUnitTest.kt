package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.batch.listener.step.AchievementAchieveNotificationStepListener
import dailyquest.context.MockSqsClientTestContextConfig
import dailyquest.notification.entity.Notification
import dailyquest.notification.repository.NotificationRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.TransactionTimedOutException


@ExtendWith(MockKExtension::class)
@Import(AchievementAchieveNotificationStepConfig::class, AchievementAchieveNotificationStepListener::class, MockSqsClientTestContextConfig::class)
@EnableAutoConfiguration(exclude = [ElasticsearchDataAutoConfiguration::class])
@SpringBatchTest
@DisplayName("업적 달성 알림 스텝 유닛 테스트")
class AchievementAchieveNotificationStepUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val achievementAchieveNotificationStep: Step,
) {
    @MockkBean(name = "achievementAchieveLogReader", relaxed = true)
    private lateinit var achievementAchieveLogReader: ItemReader<AchievementAchieveLog>
    @MockkBean(name = "achievementAchieveNotificationWriter", relaxed = true)
    private lateinit var achievementAchieveNotificationWriter: ItemWriter<Notification>
    @MockkBean(relaxed = true)
    private lateinit var notificationRepository: NotificationRepository

    @SpykBean
    private lateinit var transactionManager: PlatformTransactionManager

    @MockK(relaxed = true)
    private lateinit var achievement: Achievement
    private val userIds = listOf(1L, 2L, 3L)
    private lateinit var job: Job
    private lateinit var achieveLogs: MutableList<AchievementAchieveLog>
    private val stepName = "achievementAchieveNotificationStep"
    private val notifiedUserIdsKey = "notifiedUserIds"

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(achievementAchieveNotificationStep)
        job = simpleJob
        jobLauncherTestUtils.job = job
        achieveLogs = mutableListOf()
        for (userId in userIds) {
            achieveLogs.add(AchievementAchieveLog(achievement, userId))
        }
        every { achievementAchieveLogReader.read() } returnsMany achieveLogs andThen null
        every { achievementAchieveNotificationWriter.write(any()) } just Runs
    }

    @DisplayName("스텝이 종료 됐을 때")
    @Nested
    inner class TestStepEnd {
        @DisplayName("실패 상태로 종료됐다면 StepExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다")
        @Test
        fun `실패 상태로 종료됐다면 StepExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다`() {
            //given
            every { achievementAchieveLogReader.read() } throws IllegalStateException()

            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val stepExecutionContext = stepExecution.executionContext
            val result = stepExecutionContext.get(notifiedUserIdsKey)

            //then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
            assertThat(result).isNull()
        }

        @DisplayName("성공 상태로 종료됐다면 StepExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다")
        @Test
        fun `성공 상태로 종료됐다면 StepExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val stepExecutionContext = stepExecution.executionContext
            val result = stepExecutionContext.get(notifiedUserIdsKey)

            //then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(result).isNull()
        }

        @DisplayName("성공 상태로 종료됐다면 JobExecutionContext에 처리한 업적 달성 로그가 담겨 있어야 한다")
        @Test
        fun `성공 상태로 종료됐다면 JobExecutionContext에 처리한 업적 달성 로그가 담겨 있어야 한다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val jobExecutionContext = jobExecution.executionContext
            val result = jobExecutionContext.get(notifiedUserIdsKey)

            //then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(result).isNotNull()
        }

        @DisplayName("실패 상태로 종료됐다면 JobExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다")
        @Test
        fun `실패 상태로 종료됐다면 JobExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다`() {
            //given
            every { achievementAchieveLogReader.read() } throws IllegalStateException()

            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val jobExecutionContext = jobExecution.executionContext
            val result = jobExecutionContext.get(notifiedUserIdsKey)

            //then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
            assertThat(result).isNull()
        }
    }

    @DisplayName("트랜잭션 커밋 중에 오류가 발생했을 때")
    @Nested
    inner class TestTransactionCommitError {
        @BeforeEach
        fun init() {
            var haveToThrow = true
            every { transactionManager.commit(any()) } answers {
                val callStack = invocation.callStack.invoke()
                if (callStack.size > 7) {
                    val methodName = callStack[6].methodName
                    if (methodName == "doInChunkContext" && haveToThrow) {
                        haveToThrow = false
                        transactionManager.rollback(this.arg<TransactionStatus>(0))
                        throw TransactionTimedOutException("commit-error")
                    }
                }
                callOriginal()
            }
        }

        @DisplayName("StepExecutionContext에 처리한 데이터가 담겨있지 않아야 한다")
        @Test
        fun `StepExecutionContext에 처리한 데이터가 담겨있지 않아야 한다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)

            //then
            val stepExecutionContext = jobExecution.stepExecutions.first().executionContext
            assertThat(stepExecutionContext.get(notifiedUserIdsKey)).isNull()
        }

        @DisplayName("JobExecutionContext에 처리한 데이터가 담겨있지 않아야 한다")
        @Test
        fun `JobExecutionContext에 처리한 데이터가 담겨있지 않아야 한다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)

            //then
            val jobExecutionContext = jobExecution.executionContext
            assertThat(jobExecutionContext.get(notifiedUserIdsKey)).isNull()
        }

        @DisplayName("동일한 파라미터로 다시 스텝을 실행하면 JobExecutionContext에 처리한 데이터가 담겨야 한다")
        @Test
        fun `동일한 파라미터로 다시 스텝을 실행하면 JobExecutionContext에 처리한 데이터가 담겨야 한다`() {
            //given
            val jobParameters = JobParameters()
            jobLauncherTestUtils.launchStep(stepName, jobParameters)
            every { achievementAchieveLogReader.read() } returnsMany achieveLogs andThen null

            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName, jobParameters)

            //then
            val jobExecutionContext = jobExecution.executionContext
            val result = jobExecutionContext.get(notifiedUserIdsKey) as List<Long>
            assertThat(result).containsExactlyElementsOf(userIds)
        }

        @DisplayName("fault-tolerant에 의해 재실행 되지 않는다")
        @Test
        fun `fault-tolerant에 의해 재실행 되지 않는다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)

            //then
            assertThat(jobExecution.stepExecutions.first().writeCount).isEqualTo(0)
        }
    }
}