package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.batch.listener.step.ReadPerfectDayUserIdStepListener
import dailyquest.context.MockSqsClientTestContextConfig
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
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.context.annotation.Import

import java.time.LocalDate

@ExtendWith(MockKExtension::class)
@Import(ReadPerfectDayUserIdStepConfig::class, MockSqsClientTestContextConfig::class)
@EnableAutoConfiguration(exclude = [ElasticsearchDataAutoConfiguration::class])
@SpringBatchTest
@DisplayName("완벽한 하루 로그 스텝 리더 유닛 테스트")
class ReadPerfectDayUserIdReaderUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val perfectDayLogStep: Step,
    private val perfectDayUserIdReader: RepositoryItemReader<Long>,
) {
    @MockkBean(relaxed = true)
    private lateinit var perfectDayLogStepListener: ReadPerfectDayUserIdStepListener

    @MockkBean(relaxed = true)
    private lateinit var questLogRepository: QuestLogRepository
    private lateinit var job: Job
    private val stepName = "readPerfectDayUserIdStep"

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
        jobLauncherTestUtils.launchStep(stepName, jobParameters)

        //then
        verify {
            questLogRepository.getAllUserIdsWhoAchievedPerfectDay(eq(loggedDate), any())
        }
    }
}