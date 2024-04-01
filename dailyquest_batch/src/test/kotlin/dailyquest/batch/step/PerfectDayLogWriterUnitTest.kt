package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.batch.listener.step.PerfectDayLogStepListener
import dailyquest.log.perfectday.entity.PerfectDayLog
import dailyquest.perfectday.repository.PerfectDayLogRepository
import dailyquest.quest.repository.QuestLogRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
@Import(PerfectDayLogStepConfig::class)
@EnableAutoConfiguration
@SpringBatchTest
@DisplayName("완벽한 하루 로그 스텝 라이터 유닛 테스트")
class PerfectDayLogWriterUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val perfectDayLogStep: Step,
) {
    @MockkBean(name = "perfectDayUserReader", relaxed = true)
    private lateinit var perfectDayUserReader: RepositoryItemReader<Long>
    @MockkBean
    private lateinit var questLogRepository: QuestLogRepository
    @MockkBean(name = "perfectDayLogProcessor", relaxed = true)
    private lateinit var perfectDayLogProcessor: FunctionItemProcessor<Long, PerfectDayLog>
    @MockkBean(relaxed = true)
    private lateinit var perfectDayLogStepListener: PerfectDayLogStepListener
    @MockkBean
    private lateinit var perfectDayLogRepository: PerfectDayLogRepository

    private lateinit var job: Job

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(perfectDayLogStep)
        job = simpleJob
        every { perfectDayUserReader.read() } returns 1L andThen null
    }

    @DisplayName("writer 동작 시 repository의 save가 호출된다")
    @Test
    fun `writer 동작 시 repository의 save가 호출된다`() {
        //given
        jobLauncherTestUtils.job = job
        val loggedDate = LocalDate.of(2000, 12, 1)
        val jobParameters = JobParametersBuilder().addLocalDate("loggedDate", loggedDate).toJobParameters()
        every { perfectDayLogProcessor.process(any()) } returns mockk() andThen null

        //when
        jobLauncherTestUtils.launchStep("perfectDayLogStep", jobParameters)

        //then
        verify { perfectDayLogRepository.saveAll<PerfectDayLog>(any()) }
    }
}