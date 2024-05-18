package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.entity.AchievementType.QUEST_REGISTRATION
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.batch.listener.step.CheckAndAchieveStepListener
import dailyquest.context.MockSqsClientTestContextConfig
import dailyquest.user.entity.User
import dailyquest.user.repository.BatchUserRepository
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.MetaDataInstanceFactory
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl


@Import(CheckAndAchieveStepConfig::class, MockSqsClientTestContextConfig::class)
@EnableAutoConfiguration
@SpringBatchTest
@ExtendWith(MockKExtension::class)
class CheckAndAchieveStepComponentUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val checkAndAchieveStep: Step,
    private val checkAndAchieveReader: ItemReader<Long>,
    private val checkAndAchieveProcessor: ItemProcessor<Long, AchievementAchieveLog>,
    private val checkAndAchieveWriter: ItemWriter<AchievementAchieveLog>,
) {
    @MockkBean(relaxed = true)
    private lateinit var batchUserRepository: BatchUserRepository

    @MockkBean(relaxed = true)
    private lateinit var achievementAchieveLogRepository: AchievementAchieveLogRepository
    @MockkBean(relaxed = true)
    private lateinit var checkAndAchieveStepListener: CheckAndAchieveStepListener

    @RelaxedMockK
    private lateinit var targetAchievement: Achievement
    private val targetAchievementKey = "targetAchievement"
    private val stepName = "checkAndAchieveStep"
    private lateinit var jobExecutionContext: ExecutionContext

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(checkAndAchieveStep)
        jobLauncherTestUtils.job = simpleJob

        val jobExecution = MetaDataInstanceFactory.createJobExecution()
        jobExecutionContext = jobExecution.executionContext
        jobExecutionContext.put(targetAchievementKey, targetAchievement)
    }

    @DisplayName("리더 동작 시")
    @Nested
    inner class TestReader {
        @DisplayName("유저 ID 조회 메서드가 호출된다")
        @Test
        fun `유저 ID 조회 메서드가 호출된다`() {
            //given
            //when
            val jobExecution = jobLauncherTestUtils.launchStep(stepName, jobExecutionContext)

            //then
            verify { batchUserRepository.getAllUserIdWhoCanAchieveOf(any(), any()) }
        }
    }

    @DisplayName("프로세서 동작 시")
    @Nested
    inner class TestProcessor {
        @DisplayName("JobExecutionContext에 담긴 값이 제대로 전달된다")
        @Test
        fun `JobExecutionContext에 담긴 값이 제대로 전달된다`() {
            //given
            mockkObject(AchievementAchieveLog)
            val user = mockk<User>(relaxed = true)
            every { targetAchievement.type } returns QUEST_REGISTRATION
            every { batchUserRepository.getAllUserIdWhoCanAchieveOf(any(), any()) } returns PageImpl(listOf(user.id)) andThen Page.empty()

            //when
            jobLauncherTestUtils.launchStep(stepName, jobExecutionContext)

            //then
            verify { AchievementAchieveLog.of(eq(targetAchievement), any()) }
        }
    }

    @DisplayName("라이터 동작시")
    @Nested
    inner class TestWriter {
        @DisplayName("저장 로직이 정상적으로 호출된다")
        @Test
        fun `저장 로직이 정상적으로 호출된다`() {
            //given
            mockkObject(AchievementAchieveLog)
            val user = mockk<User>(relaxed = true)
            every { targetAchievement.type } returns QUEST_REGISTRATION
            every { batchUserRepository.getAllUserIdWhoCanAchieveOf(any(), any()) } returns PageImpl(listOf(user.id)) andThen Page.empty()

            //when
            jobLauncherTestUtils.launchStep(stepName, jobExecutionContext)

            //then
            verify { achievementAchieveLogRepository.saveAll<AchievementAchieveLog>(any()) }
        }
    }
}