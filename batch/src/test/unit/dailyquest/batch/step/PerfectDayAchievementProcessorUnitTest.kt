package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.batch.listener.step.PerfectDayAchievementStepListener
import dailyquest.context.MockSqsClientTestContextConfig
import dailyquest.user.dto.UserPerfectDayCount
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import


@ExtendWith(MockKExtension::class)
@Import(PerfectDayAchievementStepConfig::class, MockSqsClientTestContextConfig::class)
@EnableAutoConfiguration
@SpringBatchTest
@DisplayName("완벽한 하루 업적 프로세서 유닛 테스트")
class PerfectDayAchievementProcessorUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val perfectDayAchievementStep: Step,
) {
    @MockkBean(name = "perfectDayCountReader", relaxed = true)
    private lateinit var perfectDayCountReader: ItemReader<UserPerfectDayCount>
    @MockkBean(name = "perfectDayAchievementWriter", relaxed = true)
    private lateinit var perfectDayAchievementWriter: RepositoryItemWriter<AchievementAchieveLog>
    @MockkBean(relaxed = true)
    private lateinit var perfectDayAchievementStepListener: PerfectDayAchievementStepListener

    private lateinit var job: Job
    private val achievement = mockk<Achievement>(relaxed = true)
    private val userPerfectDayCount = mockk<UserPerfectDayCount>(relaxed = true)

    @BeforeEach
    fun init() {
        every { perfectDayCountReader.read() } returns userPerfectDayCount andThen null
        every { perfectDayAchievementStepListener.beforeStep(any()) } answers {
            val stepExecution = this.arg<StepExecution>(0)
            val stepExecutionContext = stepExecution.executionContext
            stepExecutionContext.put("perfectDayAchievements", listOf(achievement))
        }

        val simpleJob = SimpleJob()
        simpleJob.addStep(perfectDayAchievementStep)
        job = simpleJob
        jobLauncherTestUtils.job = job
    }

    @DisplayName("StepExecutionContext에 저장된 업적을 읽어온다")
    @Test
    fun `StepExecutionContext에 저장된 업적을 읽어온다`() {
        //given
        //when
        jobLauncherTestUtils.launchStep("perfectDayAchievementStep")

        //then
        verify {
            achievement.canAchieve(any())
        }
    }

    @DisplayName("process 요청 시")
    @Nested
    inner class TestProcess {
        private val perfectDayAchievementProcessor = PerfectDayAchievementStepConfig().perfectDayAchievementProcessor(listOf(achievement))

        @DisplayName("저장된 업적 중 목표값이 현재값과 일치하는 것이 있으면 업적 달성 로그 엔티티를 반환한다")
        @Test
        fun `저장된 업적 중 목표값이 현재값과 일치하는 것이 있으면 업적 달성 로그 엔티티를 반환한다`() {
            //given
            every { achievement.canAchieve(any()) } returns true

            //when
            val result = perfectDayAchievementProcessor.process(userPerfectDayCount)

            //then
            assertThat(result).isNotNull()
        }

        @DisplayName("저장된 업적 중 목표값이 현재값과 일치하는 것이 없으면 null을 반환한다")
        @Test
        fun `저장된 업적 중 목표값이 현재값과 일치하는 것이 없으면 null을 반환한다`() {
            //given
            every { achievement.canAchieve(any()) } returns false

            //when
            val result = perfectDayAchievementProcessor.process(userPerfectDayCount)

            //then
            assertThat(result).isNull()
        }
    }
}