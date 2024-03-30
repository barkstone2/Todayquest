package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import dailyquest.batch.listener.step.PerfectDayLogStepListener
import dailyquest.log.perfectday.entity.PerfectDayLog
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
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
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.TransactionTimedOutException
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
@Import(PerfectDayLogStepListener::class, PerfectDayLogStepConfig::class)
@EnableAutoConfiguration
@SpringBatchTest
@DisplayName("완벽한 하루 로그 스텝 유닛 테스트")
class PerfectDayLogStepUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val perfectDayLogStep: Step
) {
    @MockkBean(name = "perfectDayUserReader", relaxed = true)
    private lateinit var perfectDayUserReader: RepositoryItemReader<Long>
    @MockkBean(name = "perfectDayLogProcessor", relaxed = true)
    private lateinit var perfectDayLogProcessor: FunctionItemProcessor<Long, PerfectDayLog>
    @MockkBean(name = "perfectDayLogWriter", relaxed = true)
    private lateinit var perfectDayLogWriter: RepositoryItemWriter<PerfectDayLog>

    @SpykBean
    private lateinit var transactionManager: PlatformTransactionManager

    private val perfectDayLogUserIdsKey = "perfectDayLogUserIds"
    private val userIds = listOf(1L, 2L, 3L)
    private val stepName = "perfectDayLogStep"

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(perfectDayLogStep)
        jobLauncherTestUtils.job = simpleJob
        every { perfectDayUserReader.read() } returnsMany userIds andThen null
        every { perfectDayLogProcessor.process(any()) } returnsMany userIds.map { PerfectDayLog(it, LocalDate.now()) }
        every { perfectDayLogWriter.write(any()) } just Runs
    }

    @DisplayName("스텝이 종료 됐을 때")
    @Nested
    inner class TestStepEnd {
        @DisplayName("실패 상태로 종료됐다면 StepExecutionContext에 처리한 유저 목록이 담겨 있지 않아야 한다")
        @Test
        fun `실패 상태로 종료됐다면 StepExecutionContext에 처리한 유저 목록이 담겨 있지 않아야 한다`() {
            //given
            every { perfectDayUserReader.read() } throws IllegalStateException()

            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val stepExecutionContext = stepExecution.executionContext
            val result = stepExecutionContext.get(perfectDayLogUserIdsKey)

            //then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.FAILED)
            assertThat(result).isNull()
        }

        @DisplayName("성공 상태로 종료됐다면 StepExecutionContext에 처리한 유저 목록이 담겨 있지 않아야 한다")
        @Test
        fun `성공 상태로 종료됐다면 StepExecutionContext에 처리한 유저 목록이 담겨 있지 않아야 한다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val stepExecutionContext = stepExecution.executionContext
            val result = stepExecutionContext.get(perfectDayLogUserIdsKey)

            //then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(result).isNull()
        }

        @DisplayName("성공 상태로 종료됐다면 JobExecutionContext에 처리한 유저 목록이 담겨 있어야 한다")
        @Test
        fun `성공 상태로 종료됐다면 JobExecutionContext에 처리한 유저 목록이 담겨 있어야 한다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val jobExecutionContext = jobExecution.executionContext
            val result = jobExecutionContext.get(perfectDayLogUserIdsKey)

            //then
            assertThat(stepExecution.status).isEqualTo(BatchStatus.COMPLETED)
            assertThat(result).isNotNull()
        }

        @DisplayName("실패 상태로 종료됐다면 JobExecutionContext에 처리한 유저 목록이 담겨 있지 않아야 한다")
        @Test
        fun `실패 상태로 종료됐다면 JobExecutionContext에 처리한 유저 목록이 담겨 있지 않아야 한다`() {
            //given
            every { perfectDayUserReader.read() } throws IllegalStateException()

            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName)
            val stepExecution = jobExecution.stepExecutions.first()
            val jobExecutionContext = jobExecution.executionContext
            val result = jobExecutionContext.get(perfectDayLogUserIdsKey)

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

        @DisplayName("StepExecutionContext에 처리한 유저 ID가 담겨있지 않아야 한다")
        @Test
        fun `StepExecutionContext에 처리한 유저 ID가 담겨있지 않아야 한다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep("perfectDayLogStep")

            //then
            val stepExecutionContext = jobExecution.stepExecutions.first().executionContext
            assertThat(stepExecutionContext.get(perfectDayLogUserIdsKey)).isNull()
        }

        @DisplayName("JobExecutionContext에 처리한 유저 아이디가 담겨있지 않아야 한다")
        @Test
        fun `JobExecutionContext에 처리한 유저 아이디가 담겨있지 않아야 한다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep("perfectDayLogStep")

            //then
            val jobExecutionContext = jobExecution.executionContext
            assertThat(jobExecutionContext.get(perfectDayLogUserIdsKey)).isNull()
        }

        @DisplayName("동일한 파라미터로 다시 스텝을 실행하면 JobExecutionContext에 처리한 유저 아이디가 담겨야 한다")
        @Test
        fun `동일한 파라미터로 다시 스텝을 실행하면 JobExecutionContext에 처리한 유저 아이디가 담겨야 한다`() {
            //given
            val jobParameters = JobParameters()
            jobLauncherTestUtils.launchStep("perfectDayLogStep", jobParameters)
            every { perfectDayUserReader.read() } returnsMany userIds andThen null
            every { perfectDayLogProcessor.process(any()) } returnsMany userIds.map { PerfectDayLog(it, LocalDate.now()) }

            //when
            val jobExecution = jobLauncherTestUtils.launchStep("perfectDayLogStep", jobParameters)

            //then
            val jobExecutionContext = jobExecution.executionContext
            val result = jobExecutionContext.get(perfectDayLogUserIdsKey) as List<Long>
            assertThat(result).containsExactlyElementsOf(userIds)
        }

        @DisplayName("fault-tolerant에 의해 재실행 되지 않는다")
        @Test
        fun `fault-tolerant에 의해 재실행 되지 않는다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep("perfectDayLogStep")

            //then
            assertThat(jobExecution.stepExecutions.first().writeCount).isEqualTo(0)
        }
    }
}