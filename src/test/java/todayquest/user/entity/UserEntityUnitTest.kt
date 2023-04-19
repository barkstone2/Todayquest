package todayquest.user.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import todayquest.common.MessageUtil
import todayquest.quest.entity.QuestType
import todayquest.user.dto.UserRequestDto
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
@DisplayName("유저 엔티티 유닛 테스트")
class UserEntityUnitTest {

    private lateinit var messageUtil: MockedStatic<MessageUtil>

    @BeforeEach
    fun beforeEach() {

        messageUtil = Mockito.mockStatic(MessageUtil::class.java)
        Mockito.`when`(MessageUtil.getMessage(any())).thenReturn("")
        Mockito.`when`(MessageUtil.getMessage(any(), any())).thenReturn("")
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }

    @DisplayName("유저 세팅 변경시")
    @Nested
    inner class ChangeUserSettingsTest {

        @DisplayName("닉네임이 null이 아닌경우 닉네임을 업데이트 한다")
        @Test
        fun `닉네임이 null이 아닌경우 닉네임을 업데이트 한다`() {
            //given
            val user = UserInfo("", "beforeNickname", ProviderType.GOOGLE)
            val mockDto = mock<UserRequestDto>()

            val newNickname = "afterNickname"
            doReturn(newNickname).`when`(mockDto).nickname

            //when
            user.changeUserSettings(mockDto)

            //then
            assertThat(user.nickname).isEqualTo(newNickname)
        }

        @DisplayName("닉네임이 null이면 닉네임을 변경하지 않는다")
        @Test
        fun `닉네임이 null이면 닉네임을 변경하지 않는다`() {
            //given
            val user = UserInfo("", "before", ProviderType.GOOGLE)
            val mockDto = mock<UserRequestDto>()
            val beforeNickname = user.nickname

            doReturn(null).`when`(mockDto).nickname

            //when
            user.changeUserSettings(mockDto)

            //then
            assertThat(user.nickname).isEqualTo(beforeNickname)
        }

        @DisplayName("변경 제한 시간이 경과하지 않은 경우 오류가 발생한다")
        @Test
        fun `변경 제한 시간이 경과하지 않은 경우 오류가 발생한다`() {
            //given
            val user = UserInfo("", "", ProviderType.GOOGLE)
            val mockDto = mock<UserRequestDto>()
            user.changeUserSettings(mockDto)

            //when
            val call = { user.changeUserSettings(mockDto) }

            //then
            assertThatThrownBy(call).isInstanceOf(IllegalStateException::class.java)
        }

        @DisplayName("dto 값이 null인 경우 업데이트 하지 않는다")
        @Test
        fun `dto 값이 null인 경우 업데이트 하지 않는다`() {
            //given
            val user = UserInfo("", "", ProviderType.GOOGLE)
            val mockDto = mock<UserRequestDto>()

            doReturn(null).`when`(mockDto).resetTime
            doReturn(null).`when`(mockDto).coreTime

            //when
            user.changeUserSettings(mockDto)

            //then
            assertThat(user.resetTimeLastModifiedDate).isNull()
            assertThat(user.coreTimeLastModifiedDate).isNull()
        }

        @DisplayName("dto 값이 기존값과 동일한 경우 업데이트 하지 않는다")
        @Test
        fun `dto 값이 기존값과 동일한 경우 업데이트 하지 않는다`() {
            //given
            val user = UserInfo("", "", ProviderType.GOOGLE)
            val mockDto = mock<UserRequestDto>()

            doReturn(user.getResetHour()).`when`(mockDto).resetTime
            doReturn(user.getCoreHour()).`when`(mockDto).coreTime

            //when
            user.changeUserSettings(mockDto)

            //then
            assertThat(user.resetTimeLastModifiedDate).isNull()
            assertThat(user.coreTimeLastModifiedDate).isNull()
        }

        @DisplayName("dto 값이 기존값과 다른 경우 업데이트 한다")
        @Test
        fun `dto 값이 기존값과 다른 경우 업데이트 한다`() {
            //given
            val user = UserInfo("", "", ProviderType.GOOGLE)
            val mockDto = mock<UserRequestDto>()

            val newResetHour = user.getResetHour()+1
            val newCoreHour = user.getCoreHour()+1

            doReturn(newResetHour).`when`(mockDto).resetTime
            doReturn(newCoreHour).`when`(mockDto).coreTime

            //when
            user.changeUserSettings(mockDto)

            //then
            assertThat(user.resetTimeLastModifiedDate).isNotNull()
            assertThat(user.coreTimeLastModifiedDate).isNotNull()

            assertThat(user.getResetHour()).isEqualTo(newResetHour)
            assertThat(user.getCoreHour()).isEqualTo(newCoreHour)
        }
    }

    @DisplayName("경험치 골드 획득 로직 호출시")
    @Nested
    inner class UpdateExpAndGoldTest {

        @DisplayName("퀘스트 타입이 메인이면 2배의 보상을 획득한다")
        @Test
        fun `퀘스트 타입이 메인이면 2배의 보상을 획득한다`() {
            //given
            val earnedExp = 1L
            val earnedGold = 1L
            val questType = QuestType.MAIN
            val user = UserInfo("", "", ProviderType.GOOGLE)

            //when
            user.updateExpAndGold(questType, earnedExp, earnedGold)

            //then
            assertThat(user.exp).isEqualTo(earnedExp*2)
            assertThat(user.gold).isEqualTo(earnedGold*2)
        }

        @DisplayName("퀘스트 타입이 서브면 1배의 보상을 획득한다")
        @Test
        fun `퀘스트 타입이 서브면 1배의 보상을 획득한다`() {
            //given
            val earnedExp = 1L
            val earnedGold = 1L
            val questType = QuestType.SUB
            val user = UserInfo("", "", ProviderType.GOOGLE)

            //when
            user.updateExpAndGold(questType, earnedExp, earnedGold)

            //then
            assertThat(user.exp).isEqualTo(earnedExp)
            assertThat(user.gold).isEqualTo(earnedGold)
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

            val dto = UserRequestDto()
            dto.coreTime = now.minusHours(1).hour

            user.changeUserSettings(dto)

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

            val dto = UserRequestDto()
            dto.coreTime = now.hour

            user.changeUserSettings(dto)

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
            user.updateExpAndGold(QuestType.SUB, (expTable[1]?.plus(expTable[2]!!)!! + remain), 0)

            //when
            val (level, remainExp, requireExp) = user.calculateLevel(expTable)

            //then
            assertThat(level).isEqualTo(3)
            assertThat(remainExp).isEqualTo(remain)
            assertThat(requireExp).isEqualTo(expTable[3])
        }
    }

}