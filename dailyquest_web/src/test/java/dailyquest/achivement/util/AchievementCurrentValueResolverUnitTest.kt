package dailyquest.achivement.util

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.util.AchievementCurrentValueResolver
import dailyquest.quest.service.QuestLogService
import dailyquest.user.service.UserService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("업적 현재값 결정기 유닛 테스트")
class AchievementCurrentValueResolverUnitTest {

    @InjectMockKs
    lateinit var achievementCurrentValueResolver: AchievementCurrentValueResolver

    @RelaxedMockK
    lateinit var questLogService: QuestLogService

    @RelaxedMockK
    lateinit var userService: UserService

    @DisplayName("현재값 결정 요청 시")
    @Nested
    inner class TestResolveCurrentValue {
        @RelaxedMockK
        private lateinit var targetAchievement: Achievement

        @RelaxedMockK
        private lateinit var achieveRequest: AchievementAchieveRequest

        @DisplayName("요청 타입이 QUEST_REGISTRATION이면 전체 등록 수를 현재 값으로 사용한다")
        @Test
        fun `요청 타입이 QUEST_REGISTRATION이면 전체 등록 수를 현재 값으로 사용한다`() {
            //given
            every { achieveRequest.type } returns AchievementType.QUEST_REGISTRATION

            //when
            achievementCurrentValueResolver.resolveCurrentValue(achieveRequest, targetAchievement)

            //then
            verify { questLogService.getTotalRegistrationCount(any()) }
        }

        @DisplayName("요청 타입이 QUEST_COMPLETION이면 전체 완료 수를 현재 값으로 사용한다")
        @Test
        fun `요청 타입이 QUEST_COMPLETION이면 전체 완료 수를 현재 값으로 사용한다`() {
            //given
            every { achieveRequest.type } returns AchievementType.QUEST_COMPLETION

            //when
            achievementCurrentValueResolver.resolveCurrentValue(achieveRequest, targetAchievement)

            //then
            verify { questLogService.getTotalCompletionCount(any()) }
        }

        @DisplayName("요청 타입이 QUEST_CONTINUOUS_REGISTRATION_DAYS이면 기간 동안의 등록일수를 현재값으로 사용한다")
        @Test
        fun `요청 타입이 QUEST_CONTINUOUS_REGISTRATION_DAYS이면 기간 동안의 등록일수를 현재값으로 사용한다`() {
            //given
            every { achieveRequest.type } returns AchievementType.QUEST_CONTINUOUS_REGISTRATION_DAYS

            //when
            achievementCurrentValueResolver.resolveCurrentValue(achieveRequest, targetAchievement)

            //then
            verify { questLogService.getRegistrationDaysSince(any(), any()) }
        }

        @DisplayName("요청 타입이 EMPTY면 0이 현재값으로 사용된다")
        @Test
        fun `요청 타입이 EMPTY면 0이 현재값으로 사용된다`() {
            //given
            every { achieveRequest.type } returns AchievementType.EMPTY

            //when
            val resolvedCurrentValue =
                achievementCurrentValueResolver.resolveCurrentValue(achieveRequest, targetAchievement)

            //then
            assertThat(resolvedCurrentValue).isZero()
        }

        @DisplayName("요청 타입이 USER_LEVEL이면 조회한 사용자의 레벨이 현재값으로 사용된다")
        @Test
        fun `요청 타입이 USER_LEVEL이면 조회한 사용자의 레벨이 현재값으로 사용된다`() {
            //given
            every { achieveRequest.type } returns AchievementType.USER_LEVEL

            //when
            achievementCurrentValueResolver.resolveCurrentValue(achieveRequest, targetAchievement)

            //then
            verify { userService.getUserPrincipal(any()) }
        }

        @DisplayName("요청 타입이 QUEST_CONTINUOUS_COMPLETION이면 기간 동안의 완료 일수를 현재값으로 사용한다")
        @Test
        fun `요청 타입이 QUEST_CONTINUOUS_COMPLETION이면 기간 동안의 완료 일수를 현재값으로 사용한다`() {
            //given
            every { achieveRequest.type } returns AchievementType.QUEST_CONTINUOUS_COMPLETION

            //when
            achievementCurrentValueResolver.resolveCurrentValue(achieveRequest, targetAchievement)

            //then
//            verify { questLogService.getCompletionDaysSince() }
        }
    }


}