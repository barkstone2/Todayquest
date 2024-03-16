package dailyquest.achievement.service

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.achievement.service.AchievementAchieveLogCommandService
//import dailyquest.notification.service.NotificationService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("업적 달성 로그 커맨드 서비스 단위 테스트")
class AchievementAchieveLogCommandServiceUnitTest {

    @InjectMockKs
    private lateinit var achievementAchieveLogCommandService: AchievementAchieveLogCommandService

    @RelaxedMockK
    private lateinit var achieveLogRepository: AchievementAchieveLogRepository
//    @RelaxedMockK
//    private lateinit var notificationService: NotificationService

    @DisplayName("업적 달성 로그 저장 시")
    @Nested
    inner class TestSaveAchieveLog {

        @RelaxedMockK
        private lateinit var achievement: Achievement

        @BeforeEach
        fun init() {
            every { achieveLogRepository.save(any()) } answers { nothing }
//            every { notificationService.saveNotification(any(), any()) } answers { nothing }
        }

        @DisplayName("달성 로그를 저장하고 알림도 저장한다")
        @Test
        fun `달성 로그를 저장하고 알림도 저장한다`() {
            //given

            //when
            achievementAchieveLogCommandService.achieve(achievement, 1L)

            //then
            verifyAll {
                achieveLogRepository.save(any())
//                notificationService.saveNotification(any(), any())
            }
        }
    }
}