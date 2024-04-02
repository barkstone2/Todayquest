package dailyquest.batch.job

import com.ninjasquad.springmockk.MockkBean
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.batch.listener.job.PerfectDayJobListener
import dailyquest.batch.listener.step.AchievementAchieveNotificationStepListener
import dailyquest.batch.listener.step.PerfectDayAchievementStepListener
import dailyquest.batch.listener.step.PerfectDayLogStepListener
import dailyquest.batch.step.AchievementAchieveNotificationStepConfig
import dailyquest.batch.step.PerfectDayAchievementStepConfig
import dailyquest.batch.step.PerfectDayLogStepConfig
import dailyquest.common.util.WebApiUtil
import dailyquest.log.perfectday.entity.PerfectDayLog
import dailyquest.notification.entity.Notification
import dailyquest.notification.repository.NotificationRepository
import dailyquest.perfectday.dto.PerfectDayCount
import dailyquest.perfectday.repository.PerfectDayLogRepository
import dailyquest.quest.repository.QuestLogRepository
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
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
@Import(
    PerfectDayBatchConfig::class,
    PerfectDayLogStepConfig::class,
    PerfectDayAchievementStepConfig::class,
    AchievementAchieveNotificationStepConfig::class,
    PerfectDayLogStepListener::class,
    PerfectDayAchievementStepListener::class,
    AchievementAchieveNotificationStepListener::class,
    PerfectDayJobListener::class
)
@EnableAutoConfiguration
@SpringBatchTest
@DisplayName("완벽한 하루 작업 유닛 테스트")
class PerfectDayJobUnitTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val jobRepositoryTestUtils: JobRepositoryTestUtils,
    private val perfectDayBatchJob: Job
) {
    @MockkBean(relaxed = true)
    private lateinit var questLogRepository: QuestLogRepository
    @MockkBean(relaxed = true)
    private lateinit var perfectDayLogRepository: PerfectDayLogRepository
    @MockkBean(relaxed = true)
    private lateinit var achievementRepository: AchievementRepository
    @MockkBean(relaxed = true)
    private lateinit var achievementAchieveLogRepository: AchievementAchieveLogRepository
    @MockkBean(relaxed = true)
    private lateinit var notificationRepository: NotificationRepository
    @MockkBean(relaxed = true)
    private lateinit var webApiUtil: WebApiUtil

    private val jobParameters: JobParameters =
        JobParametersBuilder().addLocalDate("loggedDate", LocalDate.now()).toJobParameters()
    private val perfectDayUserIds = listOf(1L, 2L, 3L)
    private val achievedUserIds = listOf(1L, 2L)
    private val achievement: Achievement = mockk(relaxed = true)

    @BeforeEach
    fun init() {
        jobRepositoryTestUtils.removeJobExecutions()
        jobLauncherTestUtils.job = perfectDayBatchJob
        every {
            questLogRepository.getAllUserIdsWhoAchievedPerfectDay(any(), any())
        } returns PageImpl(perfectDayUserIds) andThen Page.empty()
        every { achievementRepository.getAllByType(any()) } returns listOf(achievement, achievement, achievement)
        every {
            perfectDayLogRepository.countByUserIds(any(), any())
        } returns PageImpl(perfectDayUserIds.map { PerfectDayCount(it, it) }) andThen Page.empty()
        every { achievement.targetValue } returnsMany achievedUserIds.map { it.toInt() }
    }

    @DisplayName("완벽한 하루를 달성한 유저에 대해 완벽한 하루 로그를 등록한다")
    @Test
    fun `완벽한 하루를 달성한 유저에 대해 완벽한 하루 로그를 등록한다`() {
        //given
        //when
        jobLauncherTestUtils.launchJob(jobParameters)

        //then
        verify { perfectDayLogRepository.saveAll<PerfectDayLog>(match { log -> log.all { perfectDayUserIds.contains(it.userId) } }) }
    }

    @DisplayName("완벽한 하루 로그가 등록된 유저에 대해 완벽한 하루 달성 횟수를 조회한다")
    @Test
    fun `완벽한 하루 로그가 등록된 유저에 대해 완벽한 하루 달성 횟수를 조회한다`() {
        //given
        //when
        jobLauncherTestUtils.launchJob(jobParameters)

        //then
        verify { perfectDayLogRepository.countByUserIds(match { it.containsAll(perfectDayUserIds) }, any()) }
    }

    @DisplayName("완벽한 하루 달성 횟수가 완벽한 하루 업적의 목표 횟수와 일치하면 업적 달성 로그를 등록한다")
    @Test
    fun `완벽한 하루 달성 횟수가 완벽한 하루 업적의 목표 횟수와 일치하면 업적 달성 로그를 등록한다`() {
        //given
        //when
        jobLauncherTestUtils.launchJob(jobParameters)

        //then
        verify { achievementAchieveLogRepository.saveAll<AchievementAchieveLog>(match { list -> list.all { achievedUserIds.contains(it.userId) } }) }
    }

    @DisplayName("업적 달성 로그가 저장된 유저에 대해 알림을 저장한다")
    @Test
    fun `업적 달성 로그가 저장된 유저에 대해 알림을 저장한다`() {
        //given
        //when
        jobLauncherTestUtils.launchJob(jobParameters)

        //then
        verify { notificationRepository.saveAll<Notification>(match { list -> list.all { achievedUserIds.contains(it.userId) } }) }
    }

    @DisplayName("알림이 저장된 유저에 대해 SSE 전송 요청을 보낸다")
    @Test
    fun `알림이 저장된 유저에 대해 SSE 전송 요청을 보낸다`() {
        //given
        //when
        jobLauncherTestUtils.launchJob(jobParameters)

        //then
        verify { webApiUtil.postSseNotify(match { achievedUserIds.containsAll(it) }) }
    }
}