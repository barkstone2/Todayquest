package dailyquest.batch

import dailyquest.batch.job.BatchQuestFailStepListener
import dailyquest.config.BatchConfig
import dailyquest.quest.entity.Quest
import dailyquest.quest.repository.QuestRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@DisplayName("퀘스트 리셋 스텝 단위 테스트")
@EnableAutoConfiguration
@SpringJUnitConfig(BatchConfig::class)
@SpringBatchTest
class ResetStepUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val jobRepositoryTestUtils: JobRepositoryTestUtils,
    private val questResetBatchJob: Job,
) {

    @MockBean
    private lateinit var questRepository: QuestRepository

    @MockBean(name = "questResetReader")
    private lateinit var questResetReader: RepositoryItemReader<Quest>

    @MockBean(name = "questFailProcessor")
    private lateinit var questFailProcessor: FunctionItemProcessor<Quest, Quest>

    @MockBean(name = "questWriter")
    private lateinit var questWriter: RepositoryItemWriter<Quest>

    @MockBean
    private lateinit var batchQuestFailStepListener: BatchQuestFailStepListener

    @BeforeEach
    fun clearMetadata() {
        jobRepositoryTestUtils.removeJobExecutions()
    }

    @DisplayName("읽기 과정에서 오류가 발생하면 에러 로그를 기록하고 스텝을 실패 처리한다")
    @Test
    fun `읽기 과정에서 오류가 발생하면 에러 로그를 기록하고 스텝을 실패 처리한다`() {
        //given
        jobLauncherTestUtils.job = questResetBatchJob

        val resetTime = LocalTime.of(6, 0)
        val jobParameters = JobParametersBuilder()
            .addString("resetTime", resetTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .toJobParameters()

        doThrow(Exception("read error")).`when`(questResetReader).read()
        doThrow(Exception("process error")).`when`(questFailProcessor).process(any())
        doThrow(Exception("write error")).`when`(questWriter).write(any())

        //when
        val jobExecution = jobLauncherTestUtils.launchStep("questResetStep", jobParameters)

        //then
        assertThat(jobExecution.stepExecutions.first().exitStatus.exitCode).isEqualTo(ExitStatus.FAILED.exitCode)
        verify(batchQuestFailStepListener, times(1)).onReadError(any())
        verify(batchQuestFailStepListener, times(0)).onProcessError(any(), any())
        verify(batchQuestFailStepListener, times(0)).onWriteError(any(), any())
    }

    @DisplayName("처리 과정이나 쓰기 과정에서 오류 발생 시 단계별로 3회까지 재시도 한다")
    @Test
    fun `처리 과정이나 쓰기 과정에서 오류 발생 시 단계별로 3회까지 재시도 한다`() {
        //given
        val mockQuest = mock<Quest>()
        jobLauncherTestUtils.job = questResetBatchJob

        val resetTime = LocalTime.of(6, 0)
        val jobParameters = JobParametersBuilder()
            .addString("resetTime", resetTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .toJobParameters()

        doReturn(mockQuest, null).`when`(questResetReader).read()
        doThrow(Exception("process error"), Exception("process error")).doReturn(mockQuest).`when`(questFailProcessor).process(any())
        doThrow(Exception("write error"), Exception("write error")).doNothing().`when`(questWriter).write(any())

        //when
        val jobExecution = jobLauncherTestUtils.launchStep("questResetStep", jobParameters)

        //then
        assertThat(jobExecution.stepExecutions.first().exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        verify(batchQuestFailStepListener, times(2)).onProcessError(any(), any())
        verify(batchQuestFailStepListener, times(2)).onWriteError(any(), any())
    }

    @DisplayName("처리 과정이나 쓰기 과정에서 각 단계별로 3회를 초과한 오류 발생 시 스텝이 실패한다")
    @Test
    fun `처리 과정이나 쓰기 과정에서 각 단계별로 3회를 초과한 오류 발생 시 스텝이 실패한다`() {
        //given
        val mockQuest = mock<Quest>()
        jobLauncherTestUtils.job = questResetBatchJob

        val resetTime = LocalTime.of(6, 0)
        val jobParameters = JobParametersBuilder()
            .addString("resetTime", resetTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .toJobParameters()

        doReturn(mockQuest, null).`when`(questResetReader).read()
        val processError = Exception("process error")
        doThrow(processError, processError, processError).doReturn(mockQuest).`when`(questFailProcessor).process(any())
        doThrow(Exception("write error")).doNothing().`when`(questWriter).write(any())

        //when
        val jobExecution = jobLauncherTestUtils.launchStep("questResetStep", jobParameters)

        //then
        assertThat(jobExecution.stepExecutions.first().exitStatus.exitCode).isEqualTo(ExitStatus.FAILED.exitCode)
        verify(batchQuestFailStepListener, times(3)).onProcessError(any(), any())
        verify(batchQuestFailStepListener, times(0)).onWriteError(any(), any())
    }

    @DisplayName("스텝 종료 후 listener를 통해 afterWrite 가 호출된다")
    @Test
    fun `스텝 종료 후 listener를 통해 afterWrite 가 호출된다`() {
        //given
        val mockQuest = mock<Quest>()
        jobLauncherTestUtils.job = questResetBatchJob

        val resetTime = LocalTime.of(6, 0)
        val jobParameters = JobParametersBuilder()
            .addString("resetTime", resetTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .toJobParameters()

        doReturn(mockQuest, null).`when`(questResetReader).read()
        doReturn(mockQuest).`when`(questFailProcessor).process(any())
        doNothing().`when`(questWriter).write(any())

        //when
        val jobExecution = jobLauncherTestUtils.launchStep("questResetStep", jobParameters)

        //then
        assertThat(jobExecution.stepExecutions.first().exitStatus.exitCode).isEqualTo(ExitStatus.COMPLETED.exitCode)
        verify(batchQuestFailStepListener).afterWrite(any())
    }

}