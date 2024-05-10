package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.batch.listener.step.PerfectDayAchievementStepListener
import dailyquest.context.MockSqsClientTestContextConfig
import dailyquest.user.dto.UserPerfectDayCount
import dailyquest.user.repository.UserRepository
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.MetaDataInstanceFactory
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import


@ExtendWith(MockKExtension::class)
@Import(PerfectDayAchievementStepConfig::class, MockSqsClientTestContextConfig::class)
@EnableAutoConfiguration
@SpringBatchTest
@DisplayName("완벽한 하루 업적 스텝 리더 유닛 테스트")
class PerfectDayAchievementReaderUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val perfectDayAchievementStep: Step,
    private val perfectDayCountReader: ItemReader<UserPerfectDayCount>,
) {
    @MockkBean(name = "perfectDayAchievementProcessor", relaxed = true)
    private lateinit var perfectDayAchievementProcessor: FunctionItemProcessor<UserPerfectDayCount, AchievementAchieveLog>
    @MockkBean(name = "perfectDayAchievementWriter", relaxed = true)
    private lateinit var perfectDayAchievementWriter: RepositoryItemWriter<AchievementAchieveLog>
    @MockkBean(relaxed = true)
    private lateinit var perfectDayAchievementStepListener: PerfectDayAchievementStepListener
    @MockkBean(relaxed = true)
    private lateinit var userRepository: UserRepository
    private lateinit var job: Job

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(perfectDayAchievementStep)
        job = simpleJob
    }

    @DisplayName("JobExecutionContext에 담긴 값이 Reader에 제대로 전달된다")
    @Test
    fun `JobExecutionContext에 담긴 값이 Reader에 제대로 전달된다`() {
        //given
        jobLauncherTestUtils.job = job
        val jobExecution = MetaDataInstanceFactory.createJobExecution()
        val jobExecutionContext = jobExecution.executionContext
        val userPerfectDayCount = mockk<UserPerfectDayCount>(relaxed = true)
        val userPerfectDayCounts = mutableListOf<UserPerfectDayCount>()
        val listSize = 30
        for (i in 1..listSize) {
            userPerfectDayCounts.add(userPerfectDayCount)
        }
        jobExecutionContext.put("userPerfectDayCounts", userPerfectDayCounts)

        //when
        jobLauncherTestUtils.launchStep("perfectDayAchievementStep", jobExecutionContext)

        //then
        verify(exactly = listSize) {
            perfectDayAchievementProcessor.process(eq(userPerfectDayCount))
        }
    }
}