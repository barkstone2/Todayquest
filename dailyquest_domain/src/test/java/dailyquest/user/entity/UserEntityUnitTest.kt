package dailyquest.user.entity

import dailyquest.quest.entity.QuestType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
@DisplayName("유저 엔티티 유닛 테스트")
class UserEntityUnitTest {

    @DisplayName("유저 닉네임 변경시")
    @Nested
    inner class UpdateNicknameTest {
        @DisplayName("닉네임이 null이 아닌경우 닉네임을 업데이트 한다")
        @Test
        fun `닉네임이 null이 아닌경우 닉네임을 업데이트 한다`() {
            //given
            val user = UserInfo("", "beforeNickname", ProviderType.GOOGLE)

            val newNickname = "afterNickname"

            //when
            user.updateNickname(newNickname)

            //then
            assertThat(user.nickname).isEqualTo(newNickname)
        }

        @DisplayName("닉네임이 null이면 닉네임을 변경하지 않는다")
        @Test
        fun `닉네임이 null이면 닉네임을 변경하지 않는다`() {
            //given
            val user = UserInfo("", "before", ProviderType.GOOGLE)
            val beforeNickname = user.nickname

            //when
            user.updateNickname(null)

            //then
            assertThat(user.nickname).isEqualTo(beforeNickname)
        }
    }


    @DisplayName("코어 타임 변경시")
    @Nested
    inner class UpdateCoreTimeTest {

        @DisplayName("인자 값이 null인 경우 업데이트 하지 않고 true 를 반환한다")
        @Test
        fun `인자 값이 null인 경우 업데이트 하지 않고 true 를 반환한다`() {
            //given
            val user = UserInfo("", "", ProviderType.GOOGLE)

            //when
            val isUpdated = user.updateCoreTime(null, LocalDateTime.now())

            //then
            assertThat(user.coreTimeLastModifiedDate).isNull()
            assertThat(isUpdated).isTrue()
        }

        @DisplayName("인자 값이 기존값과 동일한 경우 업데이트 하지 않고 true 를 반환한다")
        @Test
        fun `인자 값이 기존값과 동일한 경우 업데이트 하지 않고 true 를 반환한다`() {
            //given
            val user = UserInfo("", "", ProviderType.GOOGLE)

            //when
            val isUpdated = user.updateCoreTime(user.getCoreHour(), LocalDateTime.now())

            //then
            assertThat(user.coreTimeLastModifiedDate).isNull()
            assertThat(isUpdated).isTrue()
        }

        @DisplayName("최종 수정일로부터 1일이 경과하지 않았다면, 수정하지 않고 false 를 반환한다")
        @Test
        fun `최종 수정일로부터 1일이 경과하지 않았다면, 수정하지 않고 false 를 반환한다`() {
            //given
            val user = UserInfo("", "", ProviderType.GOOGLE)
            val requestedDate = LocalDateTime.now()
            user.updateCoreTime(user.getCoreHour()+1, requestedDate)
            val newCoreHour = user.getCoreHour()+1

            //when
            val isUpdated = user.updateCoreTime(newCoreHour, requestedDate)

            //then
            assertThat(isUpdated).isFalse()
        }

        @DisplayName("최종 수정일이 null이라면, 수정하고 true 를 반환한다")
        @Test
        fun `최종 수정일이 null이라면, 수정하고 true 를 반환한다`() {
            //given
            val user = UserInfo("", "", ProviderType.GOOGLE)
            val requestedDate = LocalDateTime.now()
            val newCoreHour = user.getCoreHour()+1

            //when
            val isUpdated = user.updateCoreTime(newCoreHour, requestedDate)

            //then
            assertThat(user.coreTimeLastModifiedDate).isEqualTo(requestedDate)
            assertThat(user.getCoreHour()).isEqualTo(newCoreHour)
            assertThat(isUpdated).isTrue()
        }

        @DisplayName("최종 수정일로부터 1일이 경과했다면, 수정하고 true 를 반환한다")
        @Test
        fun `최종 수정일로부터 1일이 경과했다면, 수정하고 true 를 반환한다`() {
            //given
            val user = UserInfo("", "", ProviderType.GOOGLE)
            val requestedDate = LocalDateTime.now()
            user.updateCoreTime(user.getCoreHour()+1, requestedDate.minusDays(1).minusSeconds(1))
            val newCoreHour = user.getCoreHour()+1

            //when
            val isUpdated = user.updateCoreTime(newCoreHour, requestedDate)

            //then
            assertThat(user.coreTimeLastModifiedDate).isEqualTo(requestedDate)
            assertThat(user.getCoreHour()).isEqualTo(newCoreHour)
            assertThat(isUpdated).isTrue()
        }

    }

    @DisplayName("코어타임 확인 로직 호출시")
    @Nested
    inner class IsNowCoreTimeTest {

        @DisplayName("현재 시간이 코어 타임 범위에 속하지 않는 경우 false를 반환한다")
        @Test
        fun `현재 시간이 코어 타임 범위에 속하지 않는 경우 false를 반환한다`() {
            //given
            val now = LocalTime.now()
            val user = UserInfo("", "", ProviderType.GOOGLE)

            val coreTime = now.minusHours(1).hour

            user.updateCoreTime(coreTime, LocalDateTime.now())

            //when
            val nowCoreTime = user.isNowCoreTime()

            //then
            assertThat(nowCoreTime).isFalse()
        }

        @DisplayName("현재 시간이 코어타임에 포함된 경우 true 를 반환한다")
        @Test
        fun `현재 시간이 코어타임에 포함된 경우 true 를 반환한다`() {
            //given
            val now = LocalTime.now()
            val user = UserInfo("", "", ProviderType.GOOGLE)

            val coreTime = now.hour

            user.updateCoreTime(coreTime, LocalDateTime.now())

            //when
            val nowCoreTime = user.isNowCoreTime()

            //then
            assertThat(nowCoreTime).isTrue()
        }

    }

    @DisplayName("레벨 계산 로직 호출시")
    @Nested
    inner class CalculateLevelTest {

        @DisplayName("경험치 테이블 기반으로 계산된 결과가 반환된다")
        @Test
        fun `경험치 테이블 기반으로 계산된 결과가 반환된다`() {
            //given
            val expTable = mapOf(1 to 10L, 2 to 20L, 3 to 30L, 4 to 40L)
            val user = UserInfo("", "", ProviderType.GOOGLE)
            val remain = 10L
            user.updateExpAndGold((expTable[1]?.plus(expTable[2]!!)!! + remain), 0)

            //when
            val (level, remainExp, requireExp) = user.calculateLevel(expTable)

            //then
            assertThat(level).isEqualTo(3)
            assertThat(remainExp).isEqualTo(remain)
            assertThat(requireExp).isEqualTo(expTable[3])
        }
    }

}