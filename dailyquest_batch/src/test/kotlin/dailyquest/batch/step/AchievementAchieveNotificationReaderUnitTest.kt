package dailyquest.batch.step

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.batch.listener.step.AchievementAchieveNotificationStepListener
import dailyquest.notification.entity.Notification
import dailyquest.notification.repository.NotificationRepository
import io.mockk.junit5.MockKExtension
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
import org.springframework.batch.test.MetaDataInstanceFactory
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import

@ExtendWith(MockKExtension::class)
@Import(AchievementAchieveNotificationStepConfig::class)
@EnableAutoConfiguration
@SpringBatchTest
@DisplayName("업적 달성 알림 스텝 리더 유닛 테스트")
class AchievementAchieveNotificationReaderUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val achievementAchieveNotificationStep: Step,
    private val achievementAchieveLogReader: ItemReader<AchievementAchieveLog>,
) {
    @MockkBean(name = "achievementAchieveNotificationProcessor", relaxed = true)
    private lateinit var achievementAchieveNotificationProcessor: FunctionItemProcessor<AchievementAchieveLog, Notification>
    @MockkBean(name = "achievementAchieveNotificationWriter", relaxed = true)
    private lateinit var achievementAchieveNotificationWriter: ItemWriter<Notification>
    @MockkBean(relaxed = true)
    private lateinit var achievementNotificationStepListener: AchievementAchieveNotificationStepListener
    @MockkBean(relaxed = true)
    private lateinit var notificationRepository: NotificationRepository

    @MockkBean(relaxed = true)
    private lateinit var achievement: Achievement
    private lateinit var job: Job

    @BeforeEach
    fun init() {
        val simpleJob = SimpleJob()
        simpleJob.addStep(achievementAchieveNotificationStep)
        job = simpleJob
        jobLauncherTestUtils.job = job
    }

    @DisplayName("JobExecutionContext에 담긴 값이 Reader에 제대로 전달된다")
    @Test
    fun `JobExecutionContext에 담긴 값이 Reader에 제대로 전달된다`() {
        //given
        val jobExecution = MetaDataInstanceFactory.createJobExecution()
        val jobExecutionContext = jobExecution.executionContext
        val achieveLogs = mutableListOf<AchievementAchieveLog>()
        val listSize = 30
        val chunkSize = 10
        for (i in 1..listSize) {
            achieveLogs.add(AchievementAchieveLog(achievement, i.toLong()))
        }
        jobExecutionContext.put("achievedLogs", achieveLogs)

        //when
        jobLauncherTestUtils.launchStep("achievementAchieveNotificationStep", jobExecutionContext)

        //then
        verify(exactly = listSize) {
            achievementAchieveNotificationProcessor.process(match { it.achievement == achievement })
        }
        verify(exactly = (listSize / chunkSize)) {
            achievementAchieveNotificationWriter.write(any())
        }
    }
}