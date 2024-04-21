package dailyquest.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.ninjasquad.springmockk.MockkBean
import dailyquest.annotation.WithCustomMockUser
import dailyquest.config.SecurityConfig
import dailyquest.filter.InternalApiKeyValidationFilter
import dailyquest.jwt.JwtAuthorizationFilter
import dailyquest.notification.service.NotificationService
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.service.UserService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.MessageSource
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
@Suppress("DEPRECATION")
@DisplayName("유저 API 컨트롤러 유닛 테스트")
@WithCustomMockUser
@WebMvcTest(controllers = [UserApiController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class, JwtAuthorizationFilter::class, InternalApiKeyValidationFilter::class]
        )
    ]
)
class UserControllerUnitTest {

    companion object {
        const val URI_PREFIX = "/api/v1/users"
    }

    @Autowired
    lateinit var mvc: MockMvc

    @MockkBean(relaxed = true)
    lateinit var userService: UserService

    @MockkBean(relaxed = true)
    lateinit var notificationService: NotificationService

    @MockkBean(relaxed = true)
    lateinit var messageSource: MessageSource

    val om: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())

    class InValidUserSettingRequest: ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            val invalidPattern = "ㄱㅂㄷㄴinvalidpattern"
            val invalidLength = "overnicknamelengthlimit123456789012345678901"
            val startWithWhiteSpace = " startWhiteSpace"
            val endWithWhiteSpace = "endWhiteSpace "
            val specialCharacter = "contain_special_chracter"
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
        fun `올바른 닉네임 패턴 요청이 아닌 경우 400이 반환된다`(nickname: String) {
            //given
            val userUpdateRequest = UserUpdateRequest(nickname)

            //when
            val result = mvc.perform(
                patch(URI_PREFIX)
                    .content(om.writeValueAsBytes(userUpdateRequest))
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
            val dto = UserUpdateRequest("newNickname")

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
            val dto = UserUpdateRequest()

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
            val dto = UserUpdateRequest()
            every { userService.updateUser(any(), any()) } throws DataIntegrityViolationException("")

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

    @DisplayName("유저 Principal 조회 시")
    @Nested
    inner class TestGetUserPrincipal {
        @DisplayName("현재 알림 수를 조회해 같이 반환한다")
        @Test
        fun `현재 알림 수를 조회해 같이 반환한다`() {
            //given
            val notificationCount = 1
            every { notificationService.getNotConfirmedNotificationCount(any()) } returns notificationCount

            //when
            mvc.get(URI_PREFIX) {
                with(csrf())
            }.andExpect {
                status { isOk() }
                jsonPath("$.data.notificationCount") {
                    value(notificationCount)
                }
            }

            //then
            verify {
                notificationService.getNotConfirmedNotificationCount(any())
            }
        }
    }
}