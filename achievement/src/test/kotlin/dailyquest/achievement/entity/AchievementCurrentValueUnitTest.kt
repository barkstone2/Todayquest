package dailyquest.achievement.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

@DisplayName("업적 현재 값 엔티티 유닛 테스트")
class AchievementCurrentValueUnitTest {

    @DisplayName("퀘스트 등록수 증가 시")
    @Nested
    inner class TestIncreaseQuestRegistrationCount {
        @DisplayName("퀘스트 등록일이 마지막 등록일 하루 전이면 연속 등록일을 증가시킨다")
        @Test
        fun `퀘스트 등록일이 마지막 등록일 하루 전이면 연속 등록일을 증가시킨다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousRegistrationDays = 0
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousRegistrationDays = beforeContinuousRegistrationDays,
                lastQuestRegistrationDate = registrationDate.minusDays(1)
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousRegistrationDays)
                .isEqualTo(beforeContinuousRegistrationDays.plus(1))
        }

        @DisplayName("퀘스트 등록일이 마지막 등록일과 같으면 연속 등록일을 증가시키지 않는다")
        @Test
        fun `퀘스트 등록일이 마지막 등록일과 같으면 연속 등록일을 증가시키지 않는다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousRegistrationDays = 0
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousRegistrationDays = beforeContinuousRegistrationDays,
                lastQuestRegistrationDate = registrationDate
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousRegistrationDays)
                .isEqualTo(beforeContinuousRegistrationDays)
        }

        @DisplayName("마지막 등록일이 null이면 연속 등록일을 증가시킨다")
        @Test
        fun `마지막 등록일이 null이면 연속 등록일을 증가시킨다`() {
            //given
            val beforeContinuousRegistrationDays = 0
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousRegistrationDays = beforeContinuousRegistrationDays,
                lastQuestRegistrationDate = null
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(LocalDate.of(2000, 12, 12))

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousRegistrationDays)
                .isEqualTo(beforeContinuousRegistrationDays.plus(1))
        }
    }

    @DisplayName("퀘스트 완료수 증가 시")
    @Nested
    inner class TestIncreaseQuestCompletionCount {
        @DisplayName("퀘스트 완료일이 마지막 완료일 하루 전이면 연속 완료일을 증가시킨다")
        @Test
        fun `퀘스트 완료일이 마지막 완료일 하루 전이면 연속 완료일을 증가시킨다`() {
            //given
            val completionDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousCompletionDays = 0
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousCompletionDays = beforeContinuousCompletionDays,
                lastQuestCompletionDate = completionDate.minusDays(1)
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousCompletionDays)
                .isEqualTo(beforeContinuousCompletionDays.plus(1))
        }

        @DisplayName("퀘스트 완료일이 마지막 완료일과 같으면 연속 완료일을 증가시키지 않는다")
        @Test
        fun `퀘스트 완료일이 마지막 완료일과 같으면 연속 완료일을 증가시키지 않는다`() {
            //given
            val completionDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousCompletionDays = 0
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousCompletionDays = beforeContinuousCompletionDays,
                lastQuestCompletionDate = completionDate
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousCompletionDays)
                .isEqualTo(beforeContinuousCompletionDays)
        }

        @DisplayName("마지막 완료일이 null이면 연속 완료일을 증가시킨다")
        @Test
        fun `마지막 완료일이 null이면 연속 완료일을 증가시킨다`() {
            //given
            val beforeContinuousCompletionDays = 0
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousCompletionDays = beforeContinuousCompletionDays,
                lastQuestCompletionDate = null
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(LocalDate.of(2000, 12, 12))

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousCompletionDays)
                .isEqualTo(beforeContinuousCompletionDays.plus(1))
        }
    }

    @DisplayName("현재 연속 등록일 증가 시")
    @Nested
    inner class TestIncreaseCurrentContinuousRegistrationDays {
        @DisplayName("증가된 값이 최대 연속 등록일보다 크면 최대값을 현재값으로 변경한다")
        @Test
        fun `증가된 값이 최대 연속 등록일보다 크면 최대값을 현재값으로 변경한다`() {
            //given
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousRegistrationDays = 1,
                maxQuestContinuousRegistrationDays = 0
            )

            //when
            achievementCurrentValue.increaseCurrentQuestContinuousRegistrationDays()

            //then
            assertThat(achievementCurrentValue.maxQuestContinuousRegistrationDays)
                .isEqualTo(achievementCurrentValue.currentQuestContinuousRegistrationDays)
        }
        
        @DisplayName("증가된 값이 최대 연속 등록일보다 작으면 최대값이 변경되지 않는다")
        @Test
        fun `증가된 값이 최대 연속 등록일보다 작으면 최대값이 변경되지 않는다`() {
            //given
            val maxQuestContinuousRegistrationDays = 10
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousRegistrationDays = 1,
                maxQuestContinuousRegistrationDays = maxQuestContinuousRegistrationDays
            )

            //when
            achievementCurrentValue.increaseCurrentQuestContinuousRegistrationDays()

            //then
            assertThat(achievementCurrentValue.maxQuestContinuousRegistrationDays)
                .isEqualTo(maxQuestContinuousRegistrationDays)
        }
    }

    @DisplayName("현재 연속 완료일 증가 시")
    @Nested
    inner class TestIncreaseCurrentContinuousCompletionDays {
        @DisplayName("증가된 값이 최대 연속 완료일보다 크면 최대값을 현재값으로 변경한다")
        @Test
        fun `증가된 값이 최대 연속 완료일보다 크면 최대값을 현재값으로 변경한다`() {
            //given
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousCompletionDays = 1,
                maxQuestContinuousCompletionDays = 0
            )

            //when
            achievementCurrentValue.increaseCurrentQuestContinuousCompletionDays()

            //then
            assertThat(achievementCurrentValue.maxQuestContinuousCompletionDays)
                .isEqualTo(achievementCurrentValue.currentQuestContinuousCompletionDays)
        }

        @DisplayName("증가된 값이 최대 연속 완료일보다 작으면 최대값이 변경되지 않는다")
        @Test
        fun `증가된 값이 최대 연속 완료일보다 작으면 최대값이 변경되지 않는다`() {
            //given
            val maxQuestContinuousCompletionDays = 10
            val achievementCurrentValue = AchievementCurrentValue(
                userId = 1L,
                currentQuestContinuousCompletionDays = 1,
                maxQuestContinuousCompletionDays = maxQuestContinuousCompletionDays
            )

            //when
            achievementCurrentValue.increaseCurrentQuestContinuousCompletionDays()

            //then
            assertThat(achievementCurrentValue.maxQuestContinuousCompletionDays).isEqualTo(maxQuestContinuousCompletionDays)
        }
    }
}