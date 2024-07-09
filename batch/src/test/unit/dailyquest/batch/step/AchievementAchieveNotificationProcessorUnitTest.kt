package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.context.MockSqsClientTestContextConfig
import dailyquest.notification.dto.AchieveNotificationSaveRequest
import dailyquest.notification.entity.Notification
import dailyquest.notification.repository.NotificationRepository
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.context.annotation.Import

@ExtendWith(MockKExtension::class)
@Import(AchievementAchieveNotificationStepConfig::class, MockSqsClientTestContextConfig::class)
@EnableAutoConfiguration(exclude = [ElasticsearchDataAutoConfiguration::class])
@SpringBatchTest
@DisplayName("업적 달성 알림 스텝 프로세서 유닛 테스트")
class AchievementAchieveNotificationProcessorUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val achievementAchieveNotificationStep: Step,
    private val achievementAchieveNotificationProcessor: FunctionItemProcessor<AchievementAchieveLog, Notification>
) {
    @MockkBean(name = "achievementAchieveLogReader", relaxed = true)
    private lateinit var achievementAchieveLogReader: ItemReader<AchievementAchieveLog>
    @MockkBean(name = "achievementAchieveNotificationWriter", relaxed = true)
    private lateinit var achievementAchieveNotificationWriter: ItemWriter<Notification>
    @MockkBean(relaxed = true)
    private lateinit var notificationRepository: NotificationRepository

    @RelaxedMockK
    private lateinit var achievement: Achievement
    private lateinit var job: Job
    private val stepName = "achievementAchieveNotificationStep"

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(achievementAchieveNotificationStep)
        job = simpleJob
        jobLauncherTestUtils.job = job
    }

    @DisplayName("컴포넌트가 정상 동작한다")
    @Test
    fun `컴포넌트가 정상 동작한다`() {
        //given
        val achieveLog = AchievementAchieveLog(achievement, 1L)
        every { achievementAchieveLogReader.read() } returns achieveLog andThen null
        mockkObject(AchieveNotificationSaveRequest)

        //when
        jobLauncherTestUtils.launchStep(stepName)

        //then
        verify {
            AchieveNotificationSaveRequest.of(any(), eq(achievement))
        }
    }

}