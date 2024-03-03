package dailyquest.notification.dto

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("업적 달성 알림 저장 요청 DTO 유닛 테스트")
class AchieveNotificationSaveRequestUnitTest {

    @RelaxedMockK
    private lateinit var achievement: Achievement
    private val userId = 1L

    @DisplayName("메타데이터 요청 시 id와 타입 정보가 반환된다")
    @Test
    fun `메타데이터 요청 시 id와 타입 정보가 반환된다`() {
        //given
        val saveRequest = AchieveNotificationSaveRequest.of(userId, achievement)
        val achievementId = 1L
        every { achievement.id } returns achievementId
        val achievementType = AchievementType.EMPTY
        every { achievement.type } returns achievementType

        //when
        val metadata = saveRequest.createNotificationMetadata()

        //then
        assertThat(metadata).containsValues(achievementId.toString(), achievementType.toString())
    }
}