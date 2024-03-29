package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.batch.listener.step.PerfectDayLogStepListener
import dailyquest.log.perfectday.entity.PerfectDayLog
import dailyquest.quest.repository.QuestLogRepository
import io.mockk.junit5.MockKExtension
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
import org.springframework.batch.item.data.RepositoryItemWriter
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
@DisplayName("완벽한 하루 로그 스텝 리더 유닛 테스트")
class PerfectDayLogStepReaderUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val perfectDayLogStep: Step,
    private val perfectDayUserReader: RepositoryItemReader<Long>,
) {
    @MockkBean(relaxed = true)
    private lateinit var perfectDayLogStepListener: PerfectDayLogStepListener
    @MockkBean(name = "perfectDayLogProcessor", relaxed = true)
    private lateinit var perfectDayLogProcessor: FunctionItemProcessor<Long, PerfectDayLog>
    @MockkBean(name = "perfectDayLogWriter", relaxed = true)
    private lateinit var perfectDayLogWriter: RepositoryItemWriter<PerfectDayLog>

    @MockkBean(relaxed = true)
    private lateinit var questLogRepository: QuestLogRepository
    private lateinit var job: Job

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(perfectDayLogStep)
        job = simpleJob
    }

    @DisplayName("jobParameters가 Reader에 제대로 전달된다")
    @Test
    fun `jobParameters가 Reader에 제대로 전달된다`() {
        //given
        jobLauncherTestUtils.job = job
        val loggedDate = LocalDate.of(2000, 12, 1)
        val jobParameters = JobParametersBuilder().addLocalDate("loggedDate", loggedDate).toJobParameters()

        //when
        jobLauncherTestUtils.launchStep("perfectDayLogStep", jobParameters)

        //then
        verify {
            questLogRepository.getAllUserIdsWhoAchievedPerfectDay(eq(loggedDate), any())
        }
    }
}