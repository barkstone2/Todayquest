package dailyquest.quest.dto

import dailyquest.quest.entity.QuestType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("퀘스트 완료 요청 DTO 유닛 테스트")
class QuestCompletionRequestUnitTest {

    @DisplayName("획득한 경험치 요청 시")
    @Nested
    inner class TestCalculateExp {
        @DisplayName("현재 타입이 main이면 2배의 경험치가 반환된다")
        @Test
        fun `현재 타입이 main이면 2배의 경험치가 반환된다`() {
            //given
            val baseExp = 10L
            val type = QuestType.MAIN
            val request = QuestCompletionUserUpdateRequest(earnedExp = baseExp, type = type)

            //when
            val resultExp = request.earnedExp

            //then
            Assertions.assertThat(resultExp).isEqualTo(baseExp * 2)
        }

        @DisplayName("현재 타입이 sub면 1배의 경험치가 반환된다")
        @Test
        fun `현재 타입이 sub면 1배의 경험치가 반환된다`() {
            //given
            val baseExp = 10L
            val type = QuestType.SUB
            val request = QuestCompletionUserUpdateRequest(earnedExp = baseExp, type = type)

            //when
            val resultExp = request.earnedExp

            //then
            Assertions.assertThat(resultExp).isEqualTo(baseExp * 1)
        }
    }

    @DisplayName("획득한 골드 요청 시")
    @Nested
    inner class TestCalculateGold {
        @DisplayName("현재 타입이 main이면 2배의 골드가 반환된다")
        @Test
        fun `현재 타입이 main이면 2배의 골드가 반환된다`() {
            //given
            val baseGold = 10L
            val type = QuestType.MAIN
            val request = QuestCompletionUserUpdateRequest(earnedGold = baseGold, type = type)

            //when
            val resultExp = request.earnedGold

            //then
            Assertions.assertThat(resultExp).isEqualTo(baseGold * 2)
        }

        @DisplayName("현재 타입이 sub면 1배의 골드가 반환된다")
        @Test
        fun `현재 타입이 sub면 1배의 골드가 반환된다`() {
            //given
            val baseGold = 10L
            val type = QuestType.SUB
            val request = QuestCompletionUserUpdateRequest(earnedGold = baseGold, type = type)

            //when
            val resultExp = request.earnedGold

            //then
            Assertions.assertThat(resultExp).isEqualTo(baseGold * 1)
        }
    }
}