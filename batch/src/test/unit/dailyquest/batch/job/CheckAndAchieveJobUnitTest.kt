package dailyquest.batch.job

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.batch.listener.job.CheckAndAchieveJobListener
import dailyquest.batch.listener.step.AchievementAchieveNotificationStepListener
import dailyquest.batch.listener.step.CheckAndAchieveStepListener
import dailyquest.batch.step.AchievementAchieveNotificationStepConfig
import dailyquest.batch.step.CheckAndAchieveStepConfig
import dailyquest.notification.entity.Notification
import dailyquest.notification.repository.NotificationRepository
import dailyquest.properties.BatchContextProperties
import dailyquest.properties.BatchParameterProperties
import dailyquest.user.entity.User
import dailyquest.user.repository.BatchUserRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.JobRepositoryTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
@Import(
    CheckAndAchieveBatchConfig::class,
    CheckAndAchieveStepConfig::class,
    CheckAndAchieveStepListener::class,
    AchievementAchieveNotificationStepConfig::class,
    AchievementAchieveNotificationStepListener::class,
    CheckAndAchieveJobListener::class,
)
@EnableAutoConfiguration
@SpringBatchTest
@DisplayName("업적 달성 확인 작업 유닛 테스트")
class CheckAndAchieveJobUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val jobRepositoryTestUtils: JobRepositoryTestUtils,
    private val checkAndAchieveJob: Job,
) {
    @MockkBean(relaxed = true)
    private lateinit var batchUserRepository: BatchUserRepository
    @MockkBean(relaxed = true)
    private lateinit var achievementRepository: AchievementRepository
    @MockkBean(relaxed = true)
    private lateinit var achievementAchieveLogRepository: AchievementAchieveLogRepository
    @MockkBean(relaxed = true)
    private lateinit var notificationRepository: NotificationRepository
    @MockkBean(relaxed = true)
    private lateinit var batchParameterProperties: BatchParameterProperties
    @MockkBean(relaxed = true)
    private lateinit var batchContextProperties: BatchContextProperties

    private val jobParameters: JobParameters =
        JobParametersBuilder().addLong("", 1L).toJobParameters()
    private val userIds = listOf(1L, 2L, 3L)
    private val achievement: Achievement = mockk(relaxed = true)
    private val user: User = mockk(relaxed = true)

    @BeforeEach
    fun init() {
        jobRepositoryTestUtils.removeJobExecutions()
        jobLauncherTestUtils.job = checkAndAchieveJob
        every {
            achievementRepository.findByIdOrNull(any())
        } returns achievement
        every {
            achievement.type
        } returns AchievementType.QUEST_REGISTRATION
        every {
            batchUserRepository.findAllByQuestRegistrationCountGreaterThanEqual(any(), any())
        } returns PageImpl(userIds.map { user }) andThen Page.empty()
        every { user.id } returnsMany userIds
        every { batchContextProperties.targetAchievementKey } returns "targetAchievement"
        every { batchContextProperties.achievedLogsKey } returns "achievedLogs"
        every { batchContextProperties.notifiedUserIdsKey } returns "notifiedUserIds"
    }

    @DisplayName("기록된 유저 레코드가 업적 목표 횟수 이상인 유저를 조회한다")
    @Test
    fun `기록된 유저 레코드가 업적 목표 횟수 이상인 유저를 조회한다`() {
        //given
        //when
        jobLauncherTestUtils.launchJob(jobParameters)

        //then
        verify { batchUserRepository.findAllByQuestRegistrationCountGreaterThanEqual(any(), any()) }
    }

    @DisplayName("조회한 유저에 대해 업적 달성 로그를 저장한다")
    @Test
    fun `조회한 유저에 대해 업적 달성 로그를 저장한다`() {
        //given
        //when
        jobLauncherTestUtils.launchJob(jobParameters)

        //then
        verify {
            achievementAchieveLogRepository.saveAll<AchievementAchieveLog>(match { list ->
                list.all {
                    userIds.contains(
                        it.userId
                    ) && it.achievement == achievement
                }
            }) }
    }

    @DisplayName("업적 달성 로그가 저장된 유저에 대해 알림을 저장한다")
    @Test
    fun `업적 달성 로그가 저장된 유저에 대해 알림을 저장한다`() {
        //given
        //when
        jobLauncherTestUtils.launchJob(jobParameters)

        //then
        verify { notificationRepository.saveAll<Notification>(match { list -> list.all { userIds.contains(it.userId) } }) }
    }
}