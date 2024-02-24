package dailyquest.achivement.service

import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementLogService
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.achievement.service.AchievementService
import dailyquest.quest.service.QuestLogService
import dailyquest.user.service.UserService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
@DisplayName("업적 서비스 단위 테스트")
class AchievementServiceUnitTest {

    @Mock
    lateinit var achievementQueryService: AchievementQueryService
    @Mock
    lateinit var questLogService: QuestLogService
    @Mock
    lateinit var achievementLogService: AchievementLogService
    @Mock
    lateinit var userService: UserService

    @InjectMocks
    lateinit var achievementService: AchievementService

    @DisplayName("업적 확인 후 달성 요청 시")
    @Nested
    inner class TestCheckAndAchieveAchievements {

        @DisplayName("QUEST_TOTAL_REGISTRATION 타입인 경우 questLogService를 통해 현재 값을 조회한다")
        @Test
        fun `QUEST_TOTAL_REGISTRATION 타입인 경우 questLogService를 통해 현재 값을 조회한다`() {
            //given
            val achievementType = AchievementType.QUEST_TOTAL_REGISTRATION
            val userId = 1L

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            verify(questLogService).getTotalRegistrationCount(eq(userId))
        }

        @DisplayName("QUEST_TOTAL_COMPLETION 타입인 경우 questLogService를 통해 현재 값을 조회한다")
        @Test
        fun `QUEST_TOTAL_COMPLETION 타입인 경우 questLogService를 통해 현재 값을 조회한다`() {
            //given
            val achievementType = AchievementType.QUEST_TOTAL_COMPLETION
            val userId = 1L

            //when
            achievementService.checkAndAchieveAchievements(achievementType, userId)

            //then
            verify(questLogService).getTotalCompletionCount(eq(userId))
        }
    }
}