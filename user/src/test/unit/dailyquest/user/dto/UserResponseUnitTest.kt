package dailyquest.user.dto

import dailyquest.user.entity.ProviderType
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("유저 응답 DTO 유닛 테스트")
class UserResponseUnitTest {

    @DisplayName("레벨 계산 로직 호출시")
    @Nested
    inner class CalculateLevelTest {

        @DisplayName("경험치 테이블 기반으로 계산된 결과가 반환된다")
        @Test
        fun `경험치 테이블 기반으로 계산된 결과가 반환된다`() {
            //given
            val expTable = mapOf(1 to 10L, 2 to 20L, 3 to 30L, 4 to 40L)
            val remain = 10L
            val user = UserResponse(1, "", ProviderType.GOOGLE, exp = (expTable[1]?.plus(expTable[2]!!)!! + remain))

            //when
            val (level, remainExp, requireExp) = user.calculateLevel(expTable)

            //then
            assertThat(level).isEqualTo(3)
            assertThat(remainExp).isEqualTo(remain)
            assertThat(requireExp).isEqualTo(expTable[3])
        }
    }
}