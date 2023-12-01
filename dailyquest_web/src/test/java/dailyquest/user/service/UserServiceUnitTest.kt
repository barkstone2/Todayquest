package dailyquest.user.service

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.redis.core.BoundHashOperations
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import dailyquest.common.MessageUtil
import dailyquest.exception.RedisDataNotFoundException
import dailyquest.properties.RedisKeyProperties
import dailyquest.user.dto.UserPrincipal
import dailyquest.user.dto.UserRequestDto
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("유저 서비스 단위 테스트")
class UserServiceUnitTest {

    @InjectMocks
    lateinit var userService: UserService

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var entityManager: EntityManager

    @Mock
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Mock
    lateinit var redisKeyProperties: RedisKeyProperties

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


    @DisplayName("getOrRegisterUser 호출 시 유저 정보가 없다면 신규 유저 등록 로직이 호출된다")
    @Test
    fun `getOrRegisterUser 호출 시 유저 정보가 없다면 신규 유저 등록 로직이 호출된다`() {
        //given
        val user = UserInfo("", "", ProviderType.GOOGLE)
        doReturn(null).`when`(userRepository).findByOauth2Id(any())

        val prefixKey = "prefix"
        val postfixKey = "postfix"
        val tableKey = "tableKey"

        doReturn(prefixKey).`when`(redisKeyProperties).nicknamePrefix
        doReturn(postfixKey).`when`(redisKeyProperties).nicknamePostfix
        doReturn(tableKey).`when`(redisKeyProperties).expTable

        val mockOpsForSet = mock<SetOperations<String, String>>()
        val mockOpsForHash = mock<HashOperations<String, String, Long>>()

        doReturn(mockOpsForSet).`when`(redisTemplate).opsForSet()
        doReturn(mockOpsForHash).`when`(redisTemplate).opsForHash<String, Long>()

        val nickname = "nickname"
        val mockExpTable = mock<Map<String, Long>>()
        doReturn(nickname).`when`(mockOpsForSet).randomMember(any())
        doReturn(mockExpTable).`when`(mockOpsForHash).entries(any())

        doReturn(user).`when`(userRepository).saveAndFlush(any())
        doReturn(user).`when`(userRepository).getReferenceById(any())

        //when
        val userPrincipal = userService.getOrRegisterUser("", ProviderType.GOOGLE)

        //then
        verify(mockOpsForSet, times(1)).randomMember(prefixKey)
        verify(mockOpsForSet, times(1)).randomMember(postfixKey)
        verify(userRepository, times(1)).saveAndFlush(any())
        verify(entityManager, times(1)).detach(user)
        verify(userRepository, times(1)).getReferenceById(any())

    }

    @DisplayName("getOrRegisterUser 호출 시 유저 정보가 있다면 유저 정보를 반환한다")
    @Test
    fun `getOrRegisterUser 호출 시 유저 정보가 있다면 유저 정보를 반환한다`() {
        //given
        val user = UserInfo("", "", ProviderType.GOOGLE)
        doReturn(user).`when`(userRepository).findByOauth2Id(any())

        val mockOpsForHash = mock<HashOperations<String, String, Long>>()
        doReturn(mockOpsForHash).`when`(redisTemplate).opsForHash<String, Long>()

        val mockExpTable = mock<Map<String, Long>>()
        doReturn(mockExpTable).`when`(mockOpsForHash).entries(any())

        val tableKey = "tableKey"
        doReturn(tableKey).`when`(redisKeyProperties).expTable

        //when
        val userPrincipal = userService.getOrRegisterUser("", ProviderType.GOOGLE)

        //then
        verify(userRepository, times(0)).saveAndFlush(any())
        verify(entityManager, times(0)).detach(user)
        verify(userRepository, times(0)).getReferenceById(any())

    }


    @DisplayName("getUserById 호출 시 존재하지 않는 유저 정보라면 예외가 던져진다")
    @Test
    fun `getUserById 호출 시 존재하지 않는 유저 정보라면 예외가 던져진다`() {
        //given
        doReturn(Optional.ofNullable(null)).`when`(userRepository).findById(any())

        //when
        val call: () -> Unit = { userService.getUserById(1) }

        //then
        assertThatThrownBy(call).isInstanceOf(IllegalStateException::class.java)
    }


    @DisplayName("유저 정보 변경 시")
    @Nested
    inner class UserSettingsChangeTest {

        @DisplayName("닉네임 변경 메서드가 호출된다")
        @Test
        fun `닉네임 변경 메서드가 호출된다`() {
            //given
            val mockPrincipal = mock<UserPrincipal>()
            val mockRequest = mock<UserRequestDto>()
            val mockUser = mock<UserInfo>()

            doReturn(mockUser).`when`(userRepository).getReferenceById(any())
            doReturn(true).`when`(mockUser).updateCoreTime(any(), any())
            doReturn(true).`when`(mockUser).updateResetTime(any(), any())

            //when
            userService.changeUserSettings(mockPrincipal, mockRequest)

            //then
            verify(mockUser).updateNickname(eq(mockRequest.nickname))
        }

        @DisplayName("코어 타임 변경에 실패하면 예외를 던진다")
        @Test
        fun `코어 타임 변경에 실패하면 예외를 던진다`() {
            //given
            val mockPrincipal = mock<UserPrincipal>()
            val mockRequest = mock<UserRequestDto>()
            val mockUser = mock<UserInfo>()

            doReturn(mockUser).`when`(userRepository).getReferenceById(any())
            doReturn(false).`when`(mockUser).updateCoreTime(any(), any())

            //when
            val run = { userService.changeUserSettings(mockPrincipal, mockRequest) }

            //then
            assertThatThrownBy(run).isInstanceOf(IllegalStateException::class.java)
            verify(mockUser).updateCoreTime(any(), any())
            verify(mockUser, never()).updateResetTime(any(), any())
        }

        @DisplayName("리셋 타임 변경에 실패하면 예외를 던진다")
        @Test
        fun `리셋 타임 변경에 실패하면 예외를 던진다`() {
            //given
            val mockPrincipal = mock<UserPrincipal>()
            val mockRequest = mock<UserRequestDto>()
            val mockUser = mock<UserInfo>()

            doReturn(mockUser).`when`(userRepository).getReferenceById(any())
            doReturn(true).`when`(mockUser).updateCoreTime(any(), any())
            doReturn(false).`when`(mockUser).updateResetTime(any(), any())

            //when
            val run = { userService.changeUserSettings(mockPrincipal, mockRequest) }

            //then
            assertThatThrownBy(run).isInstanceOf(IllegalStateException::class.java)
            verify(mockUser).updateResetTime(any(), any())
        }

    }

    @DisplayName("보상 획득 시")
    @Nested
    inner class EarnRewardTest {

        @DisplayName("레디스 설정값 조회에 실패하면 RedisDataNotFound 예외가 발생한다")
        @Test
        fun `레디스 설정값 조회에 실패하면 RedisDataNotFound 예외가 발생한다`() {
            //given
            val settingsKey = "key"
            doReturn(settingsKey).`when`(redisKeyProperties).settings

            val mockOps = mock<BoundHashOperations<String, String, Long>>()
            doReturn(mockOps).`when`(redisTemplate).boundHashOps<String, Long>(settingsKey)

            val expKey = "expKey"
            val goldKey = "goldKey"
            doReturn(expKey).`when`(redisKeyProperties).questClearExp
            doReturn(goldKey).`when`(redisKeyProperties).questClearGold

            doReturn(null).`when`(mockOps)[expKey]
            doReturn(null).`when`(mockOps)[goldKey]

            val mockUser = mock<UserInfo>()

            //when
            val run = { userService.earnExpAndGold(mock(), mockUser) }

            //then
            assertThatThrownBy(run).isInstanceOf(RedisDataNotFoundException::class.java)
            verify(mockUser, times(0)).updateExpAndGold(any(), any(), any())
        }


        @DisplayName("설정값 조회에 성공하면 정상 로직이 호출된다")
        @Test
        fun `설정값 조회에 성공하면 정상 로직이 호출된다`() {
            //given
            val settingsKey = "key"
            doReturn(settingsKey).`when`(redisKeyProperties).settings

            val mockOps = mock<BoundHashOperations<String, String, Long>>()
            doReturn(mockOps).`when`(redisTemplate).boundHashOps<String, Long>(settingsKey)

            val expKey = "expKey"
            val goldKey = "goldKey"
            doReturn(expKey).`when`(redisKeyProperties).questClearExp
            doReturn(goldKey).`when`(redisKeyProperties).questClearGold

            doReturn(0L).`when`(mockOps)[expKey]
            doReturn(0L).`when`(mockOps)[goldKey]

            val mockUser = mock<UserInfo>()

            //when
            val run = { userService.earnExpAndGold(mock(), mockUser) }

            //then
            assertThatCode(run).doesNotThrowAnyException()
            verify(mockUser, times(1)).updateExpAndGold(any(), any(), any())
        }

    }

}