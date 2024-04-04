package dailyquest.user.entity

import dailyquest.user.dto.UserUpdateRequest
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
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

    @DisplayName("코어타임 확인 로직 호출시")
    @Nested
    inner class IsNowCoreTimeTest {

        @DisplayName("현재 시간이 코어 타임 범위에 속하지 않는 경우 false를 반환한다")
        @Test
        fun `현재 시간이 코어 타임 범위에 속하지 않는 경우 false를 반환한다`() {
            //given
            val now = LocalTime.now()
            val user = User("", "", ProviderType.GOOGLE)

            val coreTime = now.minusHours(1).hour

            user.updateCoreTime(coreTime)

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
            val user = User("", "", ProviderType.GOOGLE)

            val coreTime = now.hour

            user.updateCoreTime(coreTime)

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
            val user = User("", "", ProviderType.GOOGLE)
            val remain = 10L
            user.addExpAndGold((expTable[1]?.plus(expTable[2]!!)!! + remain), 0)

            //when
            val (level, remainExp, requireExp) = user.calculateLevel(expTable)

            //then
            assertThat(level).isEqualTo(3)
            assertThat(remainExp).isEqualTo(remain)
            assertThat(requireExp).isEqualTo(expTable[3])
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

    @DisplayName("퀘스트 등록수 증가 시")
    @Nested
    inner class TestIncreaseQuestRegistrationCount {
        @DisplayName("퀘스트 등록일이 마지막 등록일 하루 전이면 연속 등록일을 증가시킨다")
        @Test
        fun `퀘스트 등록일이 마지막 등록일 하루 전이면 연속 등록일을 증가시킨다`() {
            //given
            val registrationDate = LocalDate.of(2000, 12, 12)
            val beforeContinuousRegistrationDays = 0
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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
            val achievementCurrentValue = User(
                "id", "nickname", ProviderType.GOOGLE,
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