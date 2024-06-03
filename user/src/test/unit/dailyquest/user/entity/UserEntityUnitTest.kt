package dailyquest.user.entity

import dailyquest.user.dto.UserUpdateRequest
import io.mockk.every
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

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
            val user = User("", "beforeNickname", ProviderType.GOOGLE)

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
            val user = User("", "before", ProviderType.GOOGLE)
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

        @DisplayName("인자 값이 null인 경우 true가 반환된다")
        @Test
        fun `인자 값이 null인 경우 true가 반환된다`() {
            //given
            val user = User("", "", ProviderType.GOOGLE)

            //when
            val updateSucceed = user.updateCoreTime(null)

            //then
            assertThat(updateSucceed).isTrue()
        }

        @DisplayName("인자 값이 기존값과 동일한 경우 true가 반환된다")
        @Test
        fun `인자 값이 기존값과 동일한 경우 true가 반환된다`() {
            //given
            val user = User("", "", ProviderType.GOOGLE)

            //when
            val updateSucceed = user.updateCoreTime(user.getCoreHour())

            //then
            assertThat(updateSucceed).isTrue()
        }

        @DisplayName("인자값이 null이 아니고 기존과 다르면서 최종 수정일로부터 1일이 경과하지 않았다면 false가 반환된다")
        @Test
        fun `인자값이 null이 아니고 기존과 다르면서 최종 수정일로부터 1일이 경과하지 않았다면 false가 반환된다`() {
            //given
            val user = User("", "", ProviderType.GOOGLE)
            user.updateCoreTime(user.getCoreHour()+1)
            val newCoreHour = user.getCoreHour()+1

            //when
            val updateSucceed = user.updateCoreTime(newCoreHour)

            //then
            assertThat(updateSucceed).isFalse()
        }

        @DisplayName("인자값이 null이 아니고 기존과 다르면서 최종 수정일이 null이라면 true가 반환된다")
        @Test
        fun `인자값이 null이 아니고 기존과 다르면서 최종 수정일이 null이라면 true가 반환된다`() {
            //given
            val user = User("", "", ProviderType.GOOGLE)
            val newCoreHour = user.getCoreHour()+1

            //when
            val isUpdated = user.updateCoreTime(newCoreHour)

            //then
            assertThat(isUpdated).isTrue()
        }

        @DisplayName("인자값이 null이 아니고 기존과 다르면서 최종 수정일로부터 1일이 경과했다면 true 를 반환한다")
        @Test
        fun `인자값이 null이 아니고 기존과 다르면서 최종 수정일로부터 1일이 경과했다면 true 를 반환한다`() {
            //given
            val user = spyk(User("", "", ProviderType.GOOGLE))
            val yesterday = LocalDateTime.now().minusDays(1)
            every { user.coreTimeLastModifiedDate } returns yesterday
            val newCoreHour = user.getCoreHour() + 1

            //when
            val isUpdated = user.updateCoreTime(newCoreHour)

            //then
            assertThat(isUpdated).isTrue()
        }
    }

    @DisplayName("유저 업데이트 시")
    @Nested
    inner class TestUserUpdate {
        @DisplayName("코어 타임 변경에 실패할 경우 닉네임을 변경하지 않는다")
        @Test
        fun `코어 타임 변경에 실패할 경우 닉네임을 변경하지 않는다`() {
            //given
            val beforeNickname = "before"
            val user = User("id", beforeNickname, ProviderType.GOOGLE, coreTimeLastModifiedDate = LocalDateTime.now())
            val newNickname = "new"
            val userUpdateRequest = UserUpdateRequest(nickname = newNickname, coreTime = 10)


            //when
            user.updateUser(userUpdateRequest)

            //then
            assertThat(user.nickname).isEqualTo(beforeNickname)
        }

        @DisplayName("코어 타임 변경 결과가 성공한 경우에 닉네임을 변경한다")
        @Test
        fun `코어 타임 변경 결과가 성공한 경우에 닉네임을 변경한다`() {
            //given
            val beforeNickname = "before"
            val user = User("id", beforeNickname, ProviderType.GOOGLE)
            val newNickname = "new"
            val userUpdateRequest = UserUpdateRequest(nickname = newNickname, coreTime = 10)

            //when
            user.updateUser(userUpdateRequest)

            //then
            assertThat(user.nickname).isEqualTo(newNickname)
        }
    }
}