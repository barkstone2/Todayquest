package dailyquest.notification.dto

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("업적 달성 알림 저장 요청 DTO 유닛 테스트")
class AchieveNotificationSaveRequestUnitTest {

    @RelaxedMockK
    private lateinit var achievement: Achievement
    private val userId = 1L

    @DisplayName("createNotificationContent 호출 시")
    @Nested
    inner class TestCreateNotificationContent {
        @DisplayName("업적 목표값을 업적 타입 표현 형식으로 변환하고 업적 제목과 함께 반환한다")
        @Test
        fun `업적 목표값을 업적 타입 표현 형식으로 변환하고 업적 제목과 함께 반환한다`() {
            //given
            val type = AchievementType.QUEST_REGISTRATION
            val title = "업적 제목"
            val targetValue = 1L
            every { achievement.type } returns type
            every { achievement.title } returns title
            every { achievement.targetValue } returns targetValue
            val saveRequest = AchieveNotificationSaveRequest.of(1L, achievement)

            //when
            val result = saveRequest.createNotificationContent()

            //then
            assertThat(result).contains(type.representationFormat.format(targetValue)).contains(title)
        }
    }

    @DisplayName("createNotificationMetadata 호출 시")
    @Nested
    inner class TestCreateNotificationMetadata {
        @DisplayName("업적 ID와 업적 타입이 담긴 맵을 반환한다")
        @Test
        fun `업적 ID와 업적 타입이 담긴 맵을 반환한다`() {
            //given
            val achievementId = 1L
            val achievementType = AchievementType.QUEST_REGISTRATION
            every { achievement.id } returns achievementId
            every { achievement.type } returns achievementType
            val saveRequest = AchieveNotificationSaveRequest.of(userId, achievement)

            //when
            val metadata = saveRequest.createNotificationMetadata()

            //then
            assertThat(metadata).containsValues(achievementId.toString(), achievementType.toString())
        }

        @DisplayName("업적 ID 프로퍼티 이름을 키로 업적 ID가 담긴 맵이 반환된다")
        @Test
        fun `업적 ID 프로퍼티 이름을 키로 업적 ID가 담긴 맵이 반환된다`() {
            //given
            val achievementId = 1L
            val achievementType = AchievementType.QUEST_REGISTRATION
            every { achievement.id } returns achievementId
            every { achievement.type } returns achievementType
            val saveRequest = AchieveNotificationSaveRequest.of(userId, achievement)

            //when
            val metadata = saveRequest.createNotificationMetadata()

            //then
            assertThat(metadata["achievementId"]).isEqualTo(achievementId.toString())
        }

        @DisplayName("업적 타입 프로퍼티 이름을 키로 업적 타입이 담긴 맵이 반환된다")
        @Test
        fun `업적 타입 프로퍼티 이름을 키로 업적 타입이 담긴 맵이 반환된다`() {
            //given
            val achievementId = 1L
            val achievementType = AchievementType.QUEST_REGISTRATION
            every { achievement.id } returns achievementId
            every { achievement.type } returns achievementType
            val saveRequest = AchieveNotificationSaveRequest.of(userId, achievement)

            //when
            val metadata = saveRequest.createNotificationMetadata()

            //then
            assertThat(metadata["achievementType"]).isEqualTo(achievementType.toString())
        }
    }
}