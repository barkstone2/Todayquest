package dailyquest.achievement.entity

import dailyquest.achievement.dto.SimpleAchievementUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("업적 엔티티 유닛 테스트")
class AchievementEntityUnitTest {

    @DisplayName("canAchieve 호출 시")
    @Nested
    inner class TestCanAchieve {
        @DisplayName("인자 값이 targetValue보다 작으면 false를 반환한다")
        @Test
        fun `인자 값이 targetValue보다 작으면 false를 반환한다`() {
            //given
            val currentValue = 1L
            val targetValue = currentValue + 1
            val achievement = Achievement(type = AchievementType.QUEST_REGISTRATION, targetValue = targetValue)

            //when
            val result = achievement.canAchieve(currentValue)

            //then
            assertThat(result).isFalse()
        }

        @DisplayName("인자 값이 targetValue와 같으면 true를 반환한다")
        @Test
        fun `인자 값이 targetValue와 같으면 true를 반환한다`() {
            //given
            val currentValue = 1L
            val targetValue = currentValue
            val achievement = Achievement(type = AchievementType.QUEST_REGISTRATION, targetValue = targetValue)

            //when
            val result = achievement.canAchieve(currentValue)

            //then
            assertThat(result).isTrue()
        }

        @DisplayName("인자 값이 targetValue보다 크면 true를 반환한다")
        @Test
        fun `인자 값이 targetValue보다 크면 true를 반환한다`() {
            //given
            val currentValue = 1L
            val targetValue = currentValue - 1
            val achievement = Achievement(type = AchievementType.QUEST_REGISTRATION, targetValue = targetValue)

            //when
            val result = achievement.canAchieve(currentValue)

            //then
            assertThat(result).isTrue()
        }
    }

    @DisplayName("updateAchievement 호출 시")
    @Nested
    inner class TestUpdateAchievement {
        @DisplayName("엔티티의 제목 필드가 요청 인자의 제목으로 변경된다")
        @Test
        fun `엔티티의 제목 필드가 요청 인자의 제목으로 변경된다`() {
            //given
            val achievement = Achievement("create", "", AchievementType.QUEST_COMPLETION, 1L)
            val afterTitle = "update"
            val updateRequest = SimpleAchievementUpdateRequest(afterTitle, "")

            //when
            achievement.updateAchievement(updateRequest)

            //then
            assertThat(achievement.title).isEqualTo(afterTitle)
        }

        @DisplayName("엔티티의 설명 필드가 요청 인자의 설명으로 변경된다")
        @Test
        fun `엔티티의 설명 필드가 요청 인자의 설명으로 변경된다`() {
            //given
            val achievement = Achievement("create", "create", AchievementType.QUEST_COMPLETION, 1L)
            val afterDescription = "update"
            val updateRequest = SimpleAchievementUpdateRequest("", afterDescription)

            //when
            achievement.updateAchievement(updateRequest)

            //then
            assertThat(achievement.description).isEqualTo(afterDescription)
        }
    }

    @DisplayName("activateAchievement 호출 시")
    @Nested
    inner class TestActivateAchievement {
        @DisplayName("엔티티의 inactivated 필드가 false가 된다")
        @Test
        fun `엔티티의 inactivated 필드가 false가 된다`() {
            //given
            val achievement = Achievement("", "", AchievementType.QUEST_COMPLETION, 1L)

            //when
            achievement.activateAchievement()

            //then
            assertThat(achievement.inactivated).isFalse()
        }
    }

    @DisplayName("inactivateAchievement 호출 시")
    @Nested
    inner class TestInactivateAchievement {
        @DisplayName("엔티티의 inactivated 필드가 true가 된다")
        @Test
        fun `엔티티의 inactivated 필드가 true가 된다`() {
            //given
            val achievement = Achievement("", "", AchievementType.QUEST_COMPLETION, 1L)

            //when
            achievement.inactivateAchievement()

            //then
            assertThat(achievement.inactivated).isTrue()
        }
    }
}