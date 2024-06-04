package dailyquest.user.record.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@DisplayName("유저 기록 엔티티 유닛 테스트")
class UserRecordEntityUnitTest {

    @DisplayName("퀘스트 등록수 증가 시")
    @Nested
    inner class TestIncreaseQuestRegistrationCount {
        @DisplayName("퀘스트 등록일이 마지막 등록일 하루 전이면 연속 등록일을 증가시킨다")
        @Test
        fun `퀘스트 등록일이 마지막 등록일 하루 전이면 연속 등록일을 증가시킨다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousRegistrationDays = 10L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousRegistrationDays = beforeContinuousRegistrationDays,
                lastQuestRegistrationDate = registrationDate.minusDays(1)
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousRegistrationDays)
                .isEqualTo(beforeContinuousRegistrationDays.plus(1))
        }

        @DisplayName("퀘스트 등록일이 마지막 등록일과 같으면 연속 등록일이 변경되지 않는다")
        @Test
        fun `퀘스트 등록일이 마지막 등록일과 같으면 연속 등록일이 변경되지 않는다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousRegistrationDays = 10L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousRegistrationDays = beforeContinuousRegistrationDays,
                lastQuestRegistrationDate = registrationDate
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousRegistrationDays)
                .isEqualTo(beforeContinuousRegistrationDays)
        }

        @DisplayName("퀘스트 등록일이 마지막 등록일과 하루 넘게 차이나면 연속 등록일이 1로 초기화된다")
        @Test
        fun `퀘스트 등록일이 마지막 등록일과 하루 넘게 차이나면 연속 등록일이 1로 초기화된다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousRegistrationDays = 10L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousRegistrationDays = beforeContinuousRegistrationDays,
                lastQuestRegistrationDate = registrationDate.minusDays(2)
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousRegistrationDays).isOne()
        }

        @DisplayName("마지막 등록일이 null이면 연속 등록일을 증가시킨다")
        @Test
        fun `마지막 등록일이 null이면 연속 등록일을 증가시킨다`() {
            //given
            val beforeContinuousRegistrationDays = 10L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousRegistrationDays = beforeContinuousRegistrationDays,
                lastQuestRegistrationDate = null
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(LocalDate.of(2000, 12, 12))

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousRegistrationDays)
                .isEqualTo(beforeContinuousRegistrationDays.plus(1))
        }

        @DisplayName("연속 등록일 증가 시 최대 연속 등록일이 현재값보다 작으면 최대값을 현재값으로 갱신한다")
        @Test
        fun `연속 등록일 증가 시 최대 연속 등록일이 현재값보다 작으면 최대값을 현재값으로 갱신한다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val beforeMaxQuestContinuousRegistrationDays = 1L
            val beforeContinuousRegistrationDays = 2L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousRegistrationDays = beforeContinuousRegistrationDays,
                maxQuestContinuousRegistrationDays = beforeMaxQuestContinuousRegistrationDays,
                lastQuestRegistrationDate = null
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.maxQuestContinuousRegistrationDays)
                .isEqualTo(achievementCurrentValue.currentQuestContinuousRegistrationDays)
        }

        @DisplayName("연속 등록일 증가 시 최대 연속 등록일이 현재값보다 크면 최대값을 현재값으로 갱신하지 않는다")
        @Test
        fun `연속 등록일 증가 시 최대 연속 등록일이 현재값보다 크면 최대값을 현재값으로 갱신하지 않는다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val beforeMaxQuestContinuousRegistrationDays = 10L
            val beforeContinuousRegistrationDays = 1L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousRegistrationDays = beforeContinuousRegistrationDays,
                maxQuestContinuousRegistrationDays = beforeMaxQuestContinuousRegistrationDays,
                lastQuestRegistrationDate = null
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.maxQuestContinuousRegistrationDays)
                .isNotEqualTo(achievementCurrentValue.currentQuestContinuousRegistrationDays)
        }

        @DisplayName("마지막 퀘스트 등록일이 null이면 요청 날짜로 마지막 등록일을 갱신한다")
        @Test
        fun `마지막 퀘스트 등록일이 null이면 요청 날짜로 마지막 등록일을 갱신한다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val achievementCurrentValue = UserRecord(1,
                lastQuestRegistrationDate = null
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.lastQuestRegistrationDate)
                .isEqualTo(registrationDate)
        }

        @DisplayName("마지막 퀘스트 등록일이 요청 날짜이전이면 요청 날짜로 마지막 등록일을 갱신한다")
        @Test
        fun `마지막 퀘스트 등록일이 요청 날짜이전이면 요청 날짜로 마지막 등록일을 갱신한다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val achievementCurrentValue = UserRecord(1,
                lastQuestRegistrationDate = registrationDate.minusDays(1L)
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.lastQuestRegistrationDate)
                .isEqualTo(registrationDate)
        }

        @DisplayName("마지막 퀘스트 등록일이 요청 날짜 이후면 마지막 등록일을 갱신하지 않는다")
        @Test
        fun `마지막 퀘스트 등록일이 요청 날짜 이후면 마지막 등록일을 갱신하지 않는다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val achievementCurrentValue = UserRecord(1,
                lastQuestRegistrationDate = registrationDate.plusDays(1L)
            )

            //when
            achievementCurrentValue.increaseQuestRegistrationCount(registrationDate)

            //then
            assertThat(achievementCurrentValue.lastQuestRegistrationDate)
                .isNotEqualTo(registrationDate)
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
            val beforeContinuousCompletionDays = 10L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousCompletionDays = beforeContinuousCompletionDays,
                lastQuestCompletionDate = completionDate.minusDays(1)
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousCompletionDays)
                .isEqualTo(beforeContinuousCompletionDays.plus(1))
        }

        @DisplayName("퀘스트 완료일이 마지막 완료일과 같으면 연속 완료일을 변경하지 않는다")
        @Test
        fun `퀘스트 완료일이 마지막 완료일과 같으면 연속 완료일을 변경하지 않는다`() {
            //given
            val completionDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousCompletionDays = 10L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousCompletionDays = beforeContinuousCompletionDays,
                lastQuestCompletionDate = completionDate
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousCompletionDays)
                .isEqualTo(beforeContinuousCompletionDays)
        }

        @DisplayName("퀘스트 완료일이 마지막 완료일과 하루 넘게 차이나면 연속 완료일이 1로 초기화된다")
        @Test
        fun `퀘스트 완료일이 마지막 완료일과 하루 넘게 차이나면 연속 완료일이 1로 초기화된다`() {
            //given
            val completionDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousCompletionDays = 10L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousCompletionDays = beforeContinuousCompletionDays,
                lastQuestCompletionDate = completionDate.minusDays(2)
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousCompletionDays).isOne()
        }

        @DisplayName("마지막 완료일이 null이면 연속 완료일을 증가시킨다")
        @Test
        fun `마지막 완료일이 null이면 연속 완료일을 증가시킨다`() {
            //given
            val beforeContinuousCompletionDays = 10L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousCompletionDays = beforeContinuousCompletionDays,
                lastQuestCompletionDate = null
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(LocalDate.of(2000, 12, 12))

            //then
            assertThat(achievementCurrentValue.currentQuestContinuousCompletionDays)
                .isEqualTo(beforeContinuousCompletionDays.plus(1))
        }

        @DisplayName("연속 완료일 증가 시 최대 연속 완료일이 현재값보다 작으면 최대값을 현재값으로 갱신한다")
        @Test
        fun `연속 완료일 증가 시 최대 연속 완료일이 현재값보다 작으면 최대값을 현재값으로 갱신한다`() {
            //given
            val completionDate = LocalDate.of(2000, 12, 12)
            val beforeMaxQuestContinuousCompletionDays = 1L
            val beforeContinuousCompletionDays = 2L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousCompletionDays = beforeContinuousCompletionDays,
                maxQuestContinuousCompletionDays = beforeMaxQuestContinuousCompletionDays,
                lastQuestCompletionDate = null
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.maxQuestContinuousCompletionDays)
                .isEqualTo(achievementCurrentValue.currentQuestContinuousCompletionDays)
        }

        @DisplayName("연속 완료일 증가 시 최대 연속 완료일이 현재값보다 크면 최대값을 현재값으로 갱신하지 않는다")
        @Test
        fun `연속 완료일 증가 시 최대 연속 완료일이 현재값보다 크면 최대값을 현재값으로 갱신하지 않는다`() {
            //given
            val completionDate = LocalDate.of(2000, 12, 12)
            val beforeMaxQuestContinuousCompletionDays = 10L
            val beforeContinuousCompletionDays = 1L
            val achievementCurrentValue = UserRecord(1,
                currentQuestContinuousCompletionDays = beforeContinuousCompletionDays,
                maxQuestContinuousCompletionDays = beforeMaxQuestContinuousCompletionDays,
                lastQuestCompletionDate = null
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.maxQuestContinuousCompletionDays)
                .isNotEqualTo(achievementCurrentValue.currentQuestContinuousCompletionDays)
        }

        @DisplayName("마지막 퀘스트 완료일이 null이면 요청 날짜로 마지막 완료일을 갱신한다")
        @Test
        fun `마지막 퀘스트 완료일이 null이면 요청 날짜로 마지막 완료일을 갱신한다`() {
            //given
            val completionDate = LocalDate.of(2000, 12, 12)
            val achievementCurrentValue = UserRecord(1,
                lastQuestCompletionDate = null
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.lastQuestCompletionDate)
                .isEqualTo(completionDate)
        }

        @DisplayName("마지막 퀘스트 완료일이 요청 날짜이전이면 요청 날짜로 마지막 완료일을 갱신한다")
        @Test
        fun `마지막 퀘스트 완료일이 요청 날짜이전이면 요청 날짜로 마지막 완료일을 갱신한다`() {
            //given
            val completionDate = LocalDate.of(2000, 12, 12)
            val achievementCurrentValue = UserRecord(1,
                lastQuestCompletionDate = completionDate.minusDays(1L)
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.lastQuestCompletionDate)
                .isEqualTo(completionDate)
        }

        @DisplayName("마지막 퀘스트 완료일이 요청 날짜 이후면 마지막 완료일을 갱신하지 않는다")
        @Test
        fun `마지막 퀘스트 완료일이 요청 날짜 이후면 마지막 완료일을 갱신하지 않는다`() {
            //given
            val completionDate = LocalDate.of(2000, 12, 12)
            val achievementCurrentValue = UserRecord(1,
                lastQuestCompletionDate = completionDate.plusDays(1L)
            )

            //when
            achievementCurrentValue.increaseQuestCompletionCount(completionDate)

            //then
            assertThat(achievementCurrentValue.lastQuestCompletionDate)
                .isNotEqualTo(completionDate)
        }
    }
}