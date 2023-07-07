package todayquest.user.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import todayquest.common.ResponseData
import todayquest.jwt.JwtTokenProvider
import todayquest.user.dto.UserPrincipal
import todayquest.user.dto.UserRequestDto
import todayquest.user.entity.ProviderType
import todayquest.user.entity.UserInfo
import todayquest.user.repository.UserRepository
import todayquest.user.service.UserService
import java.time.LocalTime

@Suppress("DEPRECATION")
@DisplayName("유저 API 컨트롤러 통합 테스트")
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
)
class UserControllerTest @Autowired constructor(
    var context: WebApplicationContext,
    var userService: UserService,
    var userRepository: UserRepository,
) {

    companion object {
        const val SERVER_ADDR = "http://localhost:"
        const val URI_PREFIX = "/api/v1/users"
    }

    @LocalServerPort
    var port = 0

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    lateinit var mvc: MockMvc
    lateinit var testUser: UserPrincipal
    lateinit var token: Cookie
    val om: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())

    @BeforeEach
    fun setUp() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()

        testUser = userService.getOrRegisterUser("user1", ProviderType.GOOGLE)

        val accessToken = jwtTokenProvider.createAccessToken(testUser.id)
        token = jwtTokenProvider.createAccessTokenCookie(accessToken)
    }

    @Nested
    @DisplayName("유저 설정 수정 시")
    open inner class UserSettingsUpdateTest {

        /**
         * Table Unique Constraints 를 사용하므로 트랜잭션 사용 불가
         */
        @DisplayName("닉네임 중복이 발생하는 경우 409가 반환된다")
        @Test
        fun `닉네임 중복이 발생하는 경우 409가 반환된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"
            val anotherUser = userRepository.saveAndFlush(UserInfo("", "duplicate", ProviderType.GOOGLE))

            val userRequest = UserRequestDto()
            userRequest.nickname = anotherUser.nickname

            val requestBody = om.writeValueAsString(userRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isConflict)
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object : TypeReference<ResponseData<Void>>() {})
            val data = result.data

            assertThat(data).isNull()
        }

        @Transactional
        @DisplayName("설정값 변경 후 24시간이 지나지 않았다면 오류가 발생한다")
        @Test
        open fun `설정값 변경 후 24시간이 지나지 않았다면 오류가 발생한다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"

            val firstDto = UserRequestDto(
                LocalTime.of(testUser.resetTime, 0).plusHours(1).hour,
                LocalTime.of(testUser.coreTime, 0).plusHours(1).hour
            )

            //when
            val firstRequest = mvc
                .perform(
                    patch(url)
                        .contentType(APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(om.writeValueAsString(firstDto))
                )

            val secondDto = UserRequestDto(
                LocalTime.of(testUser.resetTime, 0).minusHours(1).hour,
                LocalTime.of(testUser.coreTime, 0).minusHours(1).hour
            )

            val secondRequest = mvc
                .perform(
                    patch(url)
                        .contentType(APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(om.writeValueAsString(secondDto))
                )

            //then
            firstRequest.andExpect(status().isOk)
            secondRequest.andExpect(status().isBadRequest)
            val afterUser = userRepository.findById(testUser.id).get()
            assertThat(afterUser.getResetHour()).isEqualTo(firstDto.resetTime)
            assertThat(afterUser.getCoreHour()).isEqualTo(firstDto.coreTime)
            assertThat(afterUser.getResetHour()).isNotEqualTo(secondDto.resetTime)
            assertThat(afterUser.getCoreHour()).isNotEqualTo(secondDto.coreTime)
        }

    }
}