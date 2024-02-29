package dailyquest.achivement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.service.AchievementCommandService
import dailyquest.achievement.service.AchievementLogCommandService
import dailyquest.achievement.service.AchievementQueryService
import dailyquest.quest.service.QuestLogService
import dailyquest.user.service.UserService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("업적 커맨드 서비스 단위 테스트")
class AchievementCommandServiceUnitTest {

    @InjectMockKs
    lateinit var achievementCommandService: AchievementCommandService
    @RelaxedMockK
    lateinit var achievementQueryService: AchievementQueryService
    @RelaxedMockK
    lateinit var achievementLogService: AchievementLogCommandService
    @RelaxedMockK
    lateinit var questLogService: QuestLogService
    @RelaxedMockK
    lateinit var userService: UserService

    @DisplayName("업적 확인 후 달성 요청 시")
    @Nested
    inner class TestCheckAndAchieveAchievements {

        @RelaxedMockK
        private lateinit var targetAchievement: Achievement

        @RelaxedMockK
        private lateinit var achieveRequest: AchievementAchieveRequest

        private val savedAchievements = mutableListOf<Achievement>()

        @BeforeEach
        fun init() {
            savedAchievements.clear()
            every { achievementQueryService.getNotAchievedAchievement(any(), any()) } returns targetAchievement
            every { targetAchievement.canAchieve(any()) } returns true
        }

        @DisplayName("업적 달성 가능 여부가 true면 achieve가 호출된다")
        @Test
        fun `업적 달성 가능 여부가 true면 achieve가 호출된다`() {
            //given
            every { targetAchievement.canAchieve(any()) } returns true

            //when
            achievementCommandService.checkAndAchieveAchievement(achieveRequest)

            //then
            verify { achievementLogService.achieve(eq(targetAchievement), any()) }
        }

        @DisplayName("업적 달성 가능 여부가 false면 achieve가 호출되지 않는다")
        @Test
        fun `업적 달성 가능 여부가 false면 achieve가 호출되지 않는다`() {
            //given
            every { targetAchievement.canAchieve(any()) } returns false

            //when
            achievementCommandService.checkAndAchieveAchievement(achieveRequest)

            //then
            verify { achievementLogService wasNot Called }
        }
        
        @DisplayName("요청 타입이 QUEST_REGISTRATION이면 전체 등록 수를 현재 값으로 사용한다")
        @Test
        fun `요청 타입이 QUEST_REGISTRATION이면 전체 등록 수를 현재 값으로 사용한다`() {
            //given
            every { achieveRequest.type } returns AchievementType.QUEST_REGISTRATION

            //when
            achievementCommandService.checkAndAchieveAchievement(achieveRequest)
            
            //then
            verify { questLogService.getTotalRegistrationCount(any()) }
        }
        
        @DisplayName("요청 타입이 QUEST_COMPLETION이면 전체 완료 수를 현재 값으로 사용한다")
        @Test
        fun `요청 타입이 QUEST_COMPLETION이면 전체 완료 수를 현재 값으로 사용한다`() {
            //given
            every { achieveRequest.type } returns AchievementType.QUEST_COMPLETION

            //when
            achievementCommandService.checkAndAchieveAchievement(achieveRequest)

            //then
            verify { questLogService.getTotalCompletionCount(any()) }
        }

        @DisplayName("요청 타입이 QUEST_CONTINUOUS_REGISTRATION_DAYS이면 기간 동안의 등록일수를 현재값으로 사용한다")
        @Test
        fun `요청 타입이 QUEST_CONTINUOUS_REGISTRATION_DAYS이면 기간 동안의 등록일수를 현재값으로 사용한다`() {
            //given
            every { achieveRequest.type } returns AchievementType.QUEST_CONTINUOUS_REGISTRATION_DAYS

            //when
            achievementCommandService.checkAndAchieveAchievement(achieveRequest)

            //then
            verify { questLogService.getRegDaysFrom(any(), any()) }
        }

        @DisplayName("요청 타입이 EMPTY면 0이 현재값으로 사용된다")
        @Test
        fun `요청 타입이 EMPTY면 0이 현재값으로 사용된다`() {
            //given
            every { achieveRequest.type } returns AchievementType.EMPTY

            //when
            achievementCommandService.checkAndAchieveAchievement(achieveRequest)

            //then
            verify { targetAchievement.canAchieve(eq(0)) }
        }
    }
}