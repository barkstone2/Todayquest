package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.batch.listener.step.PerfectDayAchievementStepListener
import dailyquest.context.MockSqsClientTestContextConfig
import dailyquest.user.dto.UserPerfectDayCount
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.item.ItemProcessor
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
@Import(PerfectDayAchievementStepListener::class, PerfectDayAchievementStepConfig::class, MockSqsClientTestContextConfig::class)
@EnableAutoConfiguration(exclude = [ElasticsearchDataAutoConfiguration::class])
@SpringBatchTest
@DisplayName("완벽한 하루 업적 스텝 유닛 테스트")
class PerfectDayAchievementStepUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val perfectDayAchievementStep: Step
) {
    @MockkBean(name = "perfectDayCountReader", relaxed = true)
    private lateinit var perfectDayCountReader: ItemReader<UserPerfectDayCount>
    @MockkBean(name = "perfectDayAchievementProcessor", relaxed = true)
    private lateinit var perfectDayAchievementProcessor: ItemProcessor<UserPerfectDayCount, AchievementAchieveLog>
    @MockkBean(name = "perfectDayAchievementWriter", relaxed = true)
    private lateinit var perfectDayAchievementWriter: ItemWriter<AchievementAchieveLog>
    @MockkBean(relaxed = true)
    private lateinit var achievementRepository: AchievementRepository

    @SpykBean
    private lateinit var transactionManager: PlatformTransactionManager
    private val perfectDayCounts = listOf(UserPerfectDayCount(1L, 1), UserPerfectDayCount(2L, 1), UserPerfectDayCount(3L, 1))
    private val achievement = mockk<Achievement>(relaxed = true)
    private val achievedLogsKey = "achievedLogs"
    private val stepName = "perfectDayAchievementStep"

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(perfectDayAchievementStep)
        jobLauncherTestUtils.job = simpleJob
        every { perfectDayCountReader.read() } returnsMany perfectDayCounts andThen null
        every { perfectDayAchievementProcessor.process(any()) } returnsMany perfectDayCounts.map { AchievementAchieveLog(achievement, it.userId) }
        every { perfectDayAchievementWriter.write(any()) } just Runs
    }

    @DisplayName("스텝이 종료 됐을 때")
    @Nested
    inner class TestStepEnd {
        @DisplayName("실패 상태로 종료됐다면 StepExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다")
        @Test
        fun `실패 상태로 종료됐다면 StepExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다`() {
            //given
            every { perfectDayCountReader.read() } throws IllegalStateException()

            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val stepExecutionContext = stepExecution.executionContext
            val result = stepExecutionContext.get(achievedLogsKey)

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
            val result = stepExecutionContext.get(achievedLogsKey)

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
            val result = jobExecutionContext.get(achievedLogsKey)

            //then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(result).isNotNull()
        }

        @DisplayName("실패 상태로 종료됐다면 JobExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다")
        @Test
        fun `실패 상태로 종료됐다면 JobExecutionContext에 처리한 업적 달성 로그가 담겨 있지 않아야 한다`() {
            //given
            every { perfectDayCountReader.read() } throws IllegalStateException()

            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val jobExecutionContext = jobExecution.executionContext
            val result = jobExecutionContext.get(achievedLogsKey)

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
            assertThat(stepExecutionContext.get(achievedLogsKey)).isNull()
        }

        @DisplayName("JobExecutionContext에 처리한 데이터가 담겨있지 않아야 한다")
        @Test
        fun `JobExecutionContext에 처리한 데이터가 담겨있지 않아야 한다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)

            //then
            val jobExecutionContext = jobExecution.executionContext
            assertThat(jobExecutionContext.get(achievedLogsKey)).isNull()
        }

        @DisplayName("동일한 파라미터로 다시 스텝을 실행하면 JobExecutionContext에 처리한 데이터가 담겨야 한다")
        @Test
        fun `동일한 파라미터로 다시 스텝을 실행하면 JobExecutionContext에 처리한 데이터가 담겨야 한다`() {
            //given
            val jobParameters = JobParameters()
            jobLauncherTestUtils.launchStep(stepName, jobParameters)
            every { perfectDayCountReader.read() } returnsMany perfectDayCounts andThen null
            every { perfectDayAchievementProcessor.process(any()) } returnsMany perfectDayCounts.map { AchievementAchieveLog(achievement, it.userId) }

            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName, jobParameters)

            //then
            val jobExecutionContext = jobExecution.executionContext
            val result = jobExecutionContext.get(achievedLogsKey) as List<AchievementAchieveLog>
            assertThat(result).allMatch { it.achievement == achievement }
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