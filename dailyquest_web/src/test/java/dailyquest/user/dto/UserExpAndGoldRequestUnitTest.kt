package dailyquest.user.dto

import dailyquest.quest.entity.QuestType
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("유저 골드,경험치 획득 DTO 유닛 테스트")
class UserExpAndGoldRequestUnitTest {

    @DisplayName("계산된 경험치 요청 시")
    @Nested
    inner class TestCalculateExp {
        @DisplayName("현재 타입이 main이면 2배의 경험치가 반환된다")
        @Test
        fun `현재 타입이 main이면 2배의 경험치가 반환된다`() {
            //given
            val baseExp = 10L
            val type = QuestType.MAIN
            val request = UserExpAndGoldRequest(type, baseExp, 1L)

            //when
            val resultExp = request.calculateEarnedExp()

            //then
            assertThat(resultExp).isEqualTo(baseExp * 2)
        }
        
        @DisplayName("현재 타입이 sub면 1배의 경험치가 반환된다")
        @Test
        fun `현재 타입이 sub면 1배의 경험치가 반환된다`() {
            //given
            val baseExp = 10L
            val type = QuestType.SUB
            val request = UserExpAndGoldRequest(type, baseExp, 1L)

            //when
            val resultExp = request.calculateEarnedExp()

            //then
            assertThat(resultExp).isEqualTo(baseExp * 1)
        }
    }

}