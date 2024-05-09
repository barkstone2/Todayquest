package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.batch.listener.step.ReadPerfectDayUserIdStepListener
import dailyquest.context.MockSqsClientTestContextConfig
import io.mockk.every
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import


@ExtendWith(MockKExtension::class)
@Import(ReadPerfectDayUserIdStepListener::class, ReadPerfectDayUserIdStepConfig::class, MockSqsClientTestContextConfig::class)
@EnableAutoConfiguration
@SpringBatchTest
@DisplayName("완벽한 하루 로그 스텝 유닛 테스트")
class ReadPerfectDayUserIdStepUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val readPerfectDayUserIdStep: Step
) {
    @MockkBean(name = "perfectDayUserIdReader", relaxed = true)
    private lateinit var perfectDayUserIdReader: RepositoryItemReader<Long>

    private val perfectDayLogUserIdsKey = "perfectDayLogUserIds"
    private val userIds = listOf(1L, 2L, 3L)
    private val stepName = "readPerfectDayUserIdStep"

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(readPerfectDayUserIdStep)
        jobLauncherTestUtils.job = simpleJob
        every { perfectDayUserIdReader.read() } returnsMany userIds andThen null
    }

    @DisplayName("스텝이 종료 됐을 때")
    @Nested
    inner class TestStepEnd {
        @DisplayName("실패 상태로 종료됐다면 StepExecutionContext에 처리한 유저 목록이 담겨 있지 않아야 한다")
        @Test
        fun `실패 상태로 종료됐다면 StepExecutionContext에 처리한 유저 목록이 담겨 있지 않아야 한다`() {
            //given
            every { perfectDayUserIdReader.read() } throws IllegalStateException()

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
            every { perfectDayUserIdReader.read() } throws IllegalStateException()

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
}