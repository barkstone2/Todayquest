package dailyquest.achivement.service

import dailyquest.achievement.dto.AchievementResponse
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.achievement.service.AchievementQueryService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("업적 쿼리 서비스 유닛 테스트")
class AchievementQueryServiceUnitTest {

    @InjectMockKs
    lateinit var achievementQueryService: AchievementQueryService

    @MockK
    lateinit var achievementRepository: AchievementRepository

    @DisplayName("달성 정보와 함께 업적 조회 시 달성일이 null이 아니고 오름차순, 목표값 오름차순으로 정렬된다")
    @Test
    fun `달성 정보와 함께 업적 조회 시 달성일이 null이 아니고 오름차순, 목표값 오름차순으로 정렬된다`() {
        //given
        val type = AchievementType.QUEST_REGISTRATION
        val userId = 1L
        val achievedDate = LocalDateTime.of(2022, 12, 1, 12, 0)
        val sortedAchievements = listOf(
            AchievementResponse(title= "1", type = type, targetValue = 20, achievedDate = achievedDate),
            AchievementResponse(title= "2", type = type, targetValue = 10, achievedDate = achievedDate.plusDays(1)),
            AchievementResponse(title= "3", type = type, targetValue = 1),
            AchievementResponse(title= "4", type = type, targetValue = 2),
        )
        every { achievementRepository.getAchievementsWithAchieveInfo(any(), any()) } returns sortedAchievements

        //when
        val result = achievementQueryService.getAchievementsWithAchieveInfo(type, userId)

        //then
        Assertions.assertThat(result).containsExactlyElementsOf(sortedAchievements)
    }
}