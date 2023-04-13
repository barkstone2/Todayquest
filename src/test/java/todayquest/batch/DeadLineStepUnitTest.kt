package todayquest.batch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import todayquest.config.BatchConfig
import todayquest.job.BatchQuestFailStepListener
import todayquest.quest.entity.Quest
import todayquest.quest.repository.QuestRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DisplayName("퀘스트 데드라인 스텝 단위 테스트")
@EnableAutoConfiguration
@AutoConfigureDataJpa
@SpringJUnitConfig(BatchConfig::class)
@SpringBatchTest
class DeadLineStepUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val jobRepositoryTestUtils: JobRepositoryTestUtils,
    private val questDeadLineBatchJob: Job,
) {

    @MockBean
    private lateinit var questRepository: QuestRepository

    @MockBean(name = "questDeadLineReader")
    private lateinit var questDeadLineReader: ItemReader<Quest>

    @MockBean(name = "questFailProcessor")
    private lateinit var questFailProcessor: ItemProcessor<Quest, Quest>

    @MockBean(name = "questWriter")
    private lateinit var questWriter: ItemWriter<Quest>

    @MockBean
    private lateinit var batchQuestFailStepListener: BatchQuestFailStepListener

    @BeforeEach
    fun clearMetadata() {
        jobRepositoryTestUtils.removeJobExecutions()
    }

    @DisplayName("모든 예외에 대해 Retry 시도한다")
    @Test
    fun `모든 예외에 대해 Retry 시도한다`() {
        //given
        val mockQuest = mock<Quest>()
        jobLauncherTestUtils.job = questDeadLineBatchJob

        val targetDate = LocalDateTime.now()
        val jobParameters = JobParametersBuilder()
            .addString("targetDate", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .toJobParameters()

        doReturn(mockQuest, null).`when`(questDeadLineReader).read()
        doThrow(Exception("error1")).doReturn(mockQuest).`when`(questFailProcessor).process(any())
        doThrow(Exception("error2")).doNothing().`when`(questWriter).write(any())

        //when
        val jobExecution = jobLauncherTestUtils.launchStep("questDeadLineStep", jobParameters)

        //then
        assertThat(jobExecution.stepExecutions.first().exitStatus).isEqualTo(ExitStatus.COMPLETED)
        verify(batchQuestFailStepListener).onWriteError(any(), any())
        verify(batchQuestFailStepListener).onProcessError(any(), any())
    }

    @DisplayName("스텝 종료 후 listener를 통해 quest log를 저장한다")
    @Test
    fun `스텝 종료 후 listener를 통해 quest log를 저장한다`() {
        //given
        val mockQuest = mock<Quest>()
        jobLauncherTestUtils.job = questDeadLineBatchJob

        val targetDate = LocalDateTime.now()
        val jobParameters = JobParametersBuilder()
            .addString("targetDate", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .toJobParameters()

        doReturn(mockQuest, null).`when`(questDeadLineReader).read()
        doReturn(mockQuest).`when`(questFailProcessor).process(any())
        doNothing().`when`(questWriter).write(any())

        //when
        val jobExecution = jobLauncherTestUtils.launchStep("questDeadLineStep", jobParameters)

        //then
        verify(batchQuestFailStepListener).saveQuestLog(any())
    }


}