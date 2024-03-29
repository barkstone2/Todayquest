package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.batch.listener.step.PerfectDayLogStepListener
import dailyquest.log.perfectday.entity.PerfectDayLog
import dailyquest.quest.repository.QuestLogRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
@Import(PerfectDayLogStepConfig::class)
@EnableAutoConfiguration
@SpringBatchTest
@DisplayName("완벽한 하루 로그 스텝 프로세서 유닛 테스트")
class PerfectDayLogStepProcessorUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val perfectDayLogStep: Step,
    private var perfectDayLogProcessor: FunctionItemProcessor<Long, PerfectDayLog>
) {
    @MockkBean(relaxed = true)
    private lateinit var questLogRepository: QuestLogRepository
    @MockkBean(name = "perfectDayLogWriter", relaxed = true)
    private lateinit var perfectDayLogWriter: RepositoryItemWriter<PerfectDayLog>
    @MockkBean(relaxed = true)
    private lateinit var perfectDayLogStepListener: PerfectDayLogStepListener

    private lateinit var job: Job

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(perfectDayLogStep)
        job = simpleJob
        every { questLogRepository.getAllUserIdsWhoAchievedPerfectDay(any(), any()) } returns PageImpl(listOf(1L)) andThen Page.empty()
    }

    @DisplayName("jobParameters가 processor에 제대로 전달된다")
    @Test
    fun `jobParameters가 processor에 제대로 전달된다`() {
        //given
        jobLauncherTestUtils.job = job
        val loggedDate = LocalDate.of(2000, 12, 1)
        val jobParameters = JobParametersBuilder().addLocalDate("loggedDate", loggedDate).toJobParameters()
        val processedItems = mutableListOf<PerfectDayLog>()
        every { perfectDayLogWriter.write(any()) } answers {
            callOriginal().also { processedItems.addAll(firstArg<Chunk<PerfectDayLog>>().toList()) }
        }

        //when
        jobLauncherTestUtils.launchStep("perfectDayLogStep", jobParameters)

        //then
        assertThat(processedItems).isNotEmpty.allMatch { it.loggedDate == loggedDate }
    }

}