package dailyquest.user.service

import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import dailyquest.common.MessageUtil
import dailyquest.common.timeSinceNowAsString
import dailyquest.redis.service.RedisService
import dailyquest.user.dto.UserPrincipal
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("유저 서비스 단위 테스트")
class UserServiceUnitTest {

    @InjectMockKs
    lateinit var userService: UserService

    @MockK(relaxed = true)
    lateinit var userQueryService: UserQueryService

    @MockK(relaxed = true)
    lateinit var userCommandService: UserCommandService

    @MockK(relaxed = true)
    lateinit var redisService: RedisService

    @BeforeEach
    fun beforeEach() {
        mockkObject(UserPrincipal.Companion)
        every { UserPrincipal.create(any(), any()) } returns mockk()
    }

    @DisplayName("getOrRegisterUser 호출 시")
    @Nested
    inner class TestGetPrincipalOrRegistration {
        @DisplayName("등록된 유저 정보가 있으면 유저 정보를 새로 등록하지 않는다")
        @Test
        fun `등록된 유저 정보가 있으면 유저 정보를 새로 등록하지 않는다`() {
            //given
            every { userQueryService.findUser(any(String::class)) } returns mockk()

            //when
            userService.getOrRegisterUser("", ProviderType.GOOGLE)

            //then
            verify { userCommandService.saveUser(any()) wasNot Called }
        }

        @DisplayName("등록된 유저 정보가 없을 때 새로 유저 정보를 등록한다")
        @Test
        fun `등록된 유저 정보가 없을 때 새로 유저 정보를 등록한다`() {
            //given
            every { userQueryService.findUser(any(String::class)) } returns null

            //when
            userService.getOrRegisterUser("", ProviderType.GOOGLE)

            //then
            verify { userCommandService.saveUser(any()) }
        }

        @DisplayName("새 유저 등록에 필요한 랜덤 닉네임 중복되지 않으면 닉네임을 다시 생성하지 않는다")
        @Test
        fun `새 유저 등록에 필요한 랜덤 닉네임 중복되지 않으면 닉네임을 다시 생성하지 않는다`() {
            //given
            every { userQueryService.findUser(any(String::class)) } returns null

            //when
            userService.getOrRegisterUser("", ProviderType.GOOGLE)

            //then
            verify(exactly = 1) { redisService.createRandomNickname() }
        }

        @DisplayName("새 유저 등록에 필요한 랜덤 닉네임 중복이 발생하면 닉네임을 다시 생성한다")
        @Test
        fun `새 유저 등록에 필요한 랜덤 닉네임 중복이 발생하면 닉네임을 다시 생성한다`() {
            //given
            every { userQueryService.findUser(any(String::class)) } returns null
            val duplicateCount = 1
            every { userQueryService.isDuplicateNickname(any()) } returnsMany MutableList(duplicateCount) { true } andThen false

            //when
            userService.getOrRegisterUser("", ProviderType.GOOGLE)

            //then
            verify(exactly = duplicateCount + 1) { redisService.createRandomNickname() }
        }
    }

    @DisplayName("userUpdate 호출 시")
    @Nested
    inner class TestUserUpdate {

        @RelaxedMockK
        private lateinit var principal: UserPrincipal
        @RelaxedMockK
        private lateinit var updateRequest: UserUpdateRequest

        @BeforeEach
        fun init() {
            mockkStatic(LocalDateTime::timeSinceNowAsString)
            every { any<LocalDateTime>().timeSinceNowAsString() } returns ""

            mockkStatic(MessageUtil::class)
            every { MessageUtil.getMessage(any(), any()) } returns ""
        }

        @DisplayName("업데이트 결과가 true면 예외가 발생하지 않는다")
        @Test
        fun `업데이트 결과가 true면 예외가 발생하지 않는다`() {
            //given
            every { userCommandService.updateUser(any(), any()) } returns true

            //when
            //then
            assertDoesNotThrow { userService.updateUser(principal, updateRequest) }
        }

        @DisplayName("업데이트 결과가 false면 예외가 발생한다")
        @Test
        fun `업데이트 결과가 false면 예외가 발생한다`() {
            //given
            every { userCommandService.updateUser(any(), any()) } returns false

            //when
            //then
            assertThrows<IllegalStateException> { userService.updateUser(principal, updateRequest) }
        }
    }
}