package todayquest.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.ArgumentMatchers
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import todayquest.annotation.WithCustomMockUser
import todayquest.common.MessageUtil
import todayquest.config.SecurityConfig
import todayquest.jwt.JwtAuthorizationFilter
import todayquest.user.dto.UserRequestDto
import todayquest.user.service.UserService
import java.util.stream.Stream

@DisplayName("유저 API 컨트롤러 유닛 테스트")
@WithCustomMockUser
@WebMvcTest(controllers = [UserApiController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class, JwtAuthorizationFilter::class]
        )
    ]
)
class UserControllerUnitTest {

    companion object {
        const val URI_PREFIX = "/api/v1/users"
    }

    @Autowired
    lateinit var mvc: MockMvc

    @MockBean
    lateinit var userService: UserService

    private lateinit var messageUtil: MockedStatic<MessageUtil>

    val om: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())

    @BeforeEach
    fun init() {
        messageUtil = Mockito.mockStatic(MessageUtil::class.java)
        Mockito.`when`(MessageUtil.getMessage(ArgumentMatchers.anyString())).thenReturn("")
        Mockito.`when`(MessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn("")
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }

    class InValidUserSettingRequest: ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            val invalidPattern = UserRequestDto()
            invalidPattern.nickname = "ㄱㅂㄷㄴinvalidpattern"

            val invalidLength = UserRequestDto()
            invalidLength.nickname = "overnicknamelengthlimit123456789012345678901"

            val startWithWhiteSpace = UserRequestDto()
            startWithWhiteSpace.nickname = " startWhiteSpace"

            val endWithWhiteSpace = UserRequestDto()
            endWithWhiteSpace.nickname = "endWhiteSpace "

            val specialCharacter = UserRequestDto()
            specialCharacter.nickname = "contain_special_chracter"

            return Stream.of(
                Arguments.of(invalidPattern),
                Arguments.of(invalidLength),
                Arguments.of(startWithWhiteSpace),
                Arguments.of(endWithWhiteSpace),
                Arguments.of(specialCharacter),
            )
        }
    }

    @DisplayName("유저 설정 수정 시")
    @Nested
    inner class UserSettingsUpdateTest {

        @ArgumentsSource(InValidUserSettingRequest::class)
        @DisplayName("올바른 닉네임 패턴 요청이 아닌 경우 400이 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400이 반환된다")
        fun `올바른 닉네임 패턴 요청이 아닌 경우 400이 반환된다`(dto: UserRequestDto) {
            //given

            //when
            val result = mvc.perform(
                patch(URI_PREFIX)
                    .content(om.writeValueAsBytes(dto))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
        }

        @DisplayName("올바른 닉네임 패턴 요청일 경우 200이 반환된다")
        @Test
        fun `올바른 닉네임 패턴 요청일 경우 200이 반환된다`() {
            //given
            val dto = UserRequestDto()
            dto.nickname = "newNickname"

            //when
            val result = mvc.perform(
                patch(URI_PREFIX)
                    .content(om.writeValueAsBytes(dto))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
        }

        @DisplayName("닉네임이 null일 경우 200이 반환된다")
        @Test
        fun `닉네임이 null일 경우 200이 반환된다`() {
            //given
            val dto = UserRequestDto()

            //when
            val result = mvc.perform(
                patch(URI_PREFIX)
                    .content(om.writeValueAsBytes(dto))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
        }

        @DisplayName("제약조건 위반 예외 발생 시 409를 반환한다")
        @Test
        fun `제약조건 위반 예외 발생 시 409를 반환한다`() {
            //given
            val dto = UserRequestDto()
            doThrow(DataIntegrityViolationException::class).`when`(userService).changeUserSettings(any(), any())

            //when
            val result = mvc.perform(
                patch(URI_PREFIX)
                    .content(om.writeValueAsBytes(dto))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isConflict)
        }
    }


}