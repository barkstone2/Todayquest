package dailyquest.user.service

import dailyquest.user.dto.UserResponse
import dailyquest.user.dto.UserSaveRequest
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.entity.User
import dailyquest.user.record.service.UserRecordService
import dailyquest.user.repository.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("유저 서비스 단위 테스트")
class UserServiceUnitTest {

    @InjectMockKs
    private lateinit var userService: UserService
    @RelaxedMockK
    private lateinit var userRepository: UserRepository
    @RelaxedMockK
    private lateinit var userRecordService: UserRecordService
    @RelaxedMockK
    private lateinit var messageSourceAccessor: MessageSourceAccessor
    @RelaxedMockK
    private lateinit var user: User

    @BeforeEach
    fun init() {
        every { userRepository.findByIdOrNull(any()) } returns user
    }

    @DisplayName("findUserByOauthId 호출 시")
    @Nested
    inner class TestFindUserByOauthId {
        @DisplayName("리포지토리 반환 결과가 null이면 null을 반환한다")
        @Test
        fun `리포지토리 반환 결과가 null이면 null을 반환한다`() {
            //given
            every { userRepository.findByOauth2Id(any()) } returns null

            //when
            val result = userService.findUserByOauthId("")

            //then
            assertThat(result).isNull()
        }

        @DisplayName("리포지토리 반환 결과가 null이 아니면 UserResponse를 반환한다")
        @Test
        fun `리포지토리 반환 결과가 null이 아니면 UserResponse를 반환한다`() {
            //given
            every { userRepository.findByOauth2Id(any()) } returns user

            //when
            val result = userService.findUserByOauthId("")

            //then
            assertThat(result).isNotNull.isInstanceOf(UserResponse::class.java)
        }
    }

    @DisplayName("getUserById 호출 시")
    @Nested
    inner class TestGetUserById {
        @DisplayName("리포지토리 반환 결과가 null이면 EntityNotFound 예외가 발생한다")
        @Test
        fun `리포지토리 반환 결과가 null이면 EntityNotFound 예외가 발생한다`() {
            //given
            every { userRepository.findByIdOrNull(any()) } returns null

            //when
            val function = { userService.getUserById(1L) }

            //then
            assertThrows<EntityNotFoundException> { function.invoke() }
        }

        @DisplayName("리포지토리 반환 결과가 null이 아니면 UserResponse가 반환된다")
        @Test
        fun `리포지토리 반환 결과가 null이 아니면 UserResponse가 반환된다`() {
            //given
            every { userRepository.findByIdOrNull(any()) } returns user

            //when
            val result = userService.getUserById(1L)

            //then
            assertThat(result).isNotNull.isInstanceOf(UserResponse::class.java)
        }
    }

    @DisplayName("saveUser 호출 시")
    @Nested
    inner class TestSaveUser {
        @DisplayName("유저 기록값 엔티티를 등록 요청 한다")
        @Test
        fun `유저 기록값 엔티티를 등록 요청 한다`() {
            //given
            val userId = 1L
            val user = mockk<User>()
            val saveRequest = mockk<UserSaveRequest>()
            every { saveRequest.mapToEntity() } returns user
            every { userRepository.save(any()) } returns user
            every { user.id } returns userId

            //when
            userService.saveUser(saveRequest)
            
            //then
            verify { userRecordService.saveNewRecordEntity(eq(userId)) }
        }
        
    }


    @DisplayName("updateUser 호출 시")
    @Nested
    inner class TestUpdateUser {

        @RelaxedMockK
        private lateinit var updateRequest: UserUpdateRequest

        @BeforeEach
        fun init() {
            every { user.getUpdateAvailableDateTimeOfCoreTime() } returns LocalDateTime.now()
        }

        @DisplayName("user 엔티티의 updateUser 반환 결과가 true면 예외가 발생하지 않는다")
        @Test
        fun `user 엔티티의 updateUser 반환 결과가 true면 예외가 발생하지 않는다`() {
            //given
            every { user.updateUser(any()) } returns true

            //when
            val function = { userService.updateUser(1L, updateRequest) }

            //then
            assertDoesNotThrow { function.invoke() }
        }

        @DisplayName("user 엔티티의 updateUser 반환 결과가 false면 IllegalStateException 예외가 발생한다")
        @Test
        fun `user 엔티티의 updateUser 반환 결과가 false면 IllegalStateException 예외가 발생한다`() {
            //given
            every { user.updateUser(any()) } returns false

            //when
            val function = { userService.updateUser(1L, updateRequest) }

            //then
            assertThrows<IllegalStateException> { function.invoke() }
        }
    }

    
    @DisplayName("유저 경험치 골드 추가 로직 호출 시")
    @Nested
    inner class TestAddUserExpAndGold {
        @RelaxedMockK
        private lateinit var updateRequest: UserUpdateRequest

        @DisplayName("조회한 유저의 경험치 골드 추가 로직이 호출된다")
        @Test
        fun `조회한 유저의 경험치 골드 추가 로직이 호출된다`() {
            //given
            val userId = 1L
            val earnedGold = 1L
            every { updateRequest.earnedGold } returns earnedGold

            //when
            userService.addUserExpAndGold(userId, updateRequest)
            
            //then
            verify { user.addExpAndGold(any(), eq(earnedGold)) }
        }
    }
}