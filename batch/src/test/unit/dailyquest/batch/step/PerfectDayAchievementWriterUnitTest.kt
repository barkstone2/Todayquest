package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.batch.listener.step.PerfectDayAchievementStepListener
import dailyquest.context.MockSqsClientTestContextConfig
import dailyquest.user.dto.UserPerfectDayCount
import io.mockk.every
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
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.context.annotation.Import


@ExtendWith(MockKExtension::class)
@Import(PerfectDayAchievementStepConfig::class, MockSqsClientTestContextConfig::class)
@EnableAutoConfiguration(exclude = [ElasticsearchDataAutoConfiguration::class])
@SpringBatchTest
@DisplayName("완벽한 하루 업적 스텝 라이터 유닛 테스트")
class PerfectDayAchievementWriterUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val perfectDayAchievementStep: Step,
) {
    @MockkBean(name = "perfectDayCountReader", relaxed = true)
    private lateinit var perfectDayCountReader: ItemReader<UserPerfectDayCount>
    @MockkBean(name = "perfectDayAchievementProcessor", relaxed = true)
    private lateinit var perfectDayAchievementProcessor: FunctionItemProcessor<UserPerfectDayCount, AchievementAchieveLog>
    @MockkBean(relaxed = true)
    private lateinit var perfectDayAchievementStepListener: PerfectDayAchievementStepListener

    @MockkBean(relaxed = true)
    private lateinit var achievementAchieveLogRepository: AchievementAchieveLogRepository
    private lateinit var job: Job

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(perfectDayAchievementStep)
        job = simpleJob
        every { perfectDayCountReader.read() } returns UserPerfectDayCount(1L, 1) andThen null
    }

    @DisplayName("writer 동작 시 repository의 save가 호출된다")
    @Test
    fun `writer 동작 시 repository의 save가 호출된다`() {
        //given
        jobLauncherTestUtils.job = job
        every { perfectDayAchievementProcessor.process(any()) } returns mockk() andThen null

        //when
        jobLauncherTestUtils.launchStep("perfectDayAchievementStep")

        //then
        verify { achievementAchieveLogRepository.saveAll<AchievementAchieveLog>(any()) }
    }
}