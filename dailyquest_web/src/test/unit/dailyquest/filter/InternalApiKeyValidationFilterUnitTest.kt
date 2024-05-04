package dailyquest.filter

import dailyquest.properties.SecurityKeyProperties
import dailyquest.properties.SecurityUrlProperties
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

val securityUrlProperties = mockk<SecurityUrlProperties>(relaxed = true)
val securityKeyProperties = mockk<SecurityKeyProperties>(relaxed = true)

@ExtendWith(MockKExtension::class)
@DisplayName("내부 API 키 검증 필터 유닛 테스트")
class InternalApiKeyValidationFilterUnitTest: InternalApiKeyValidationFilter(securityUrlProperties, securityKeyProperties) {

    private val internalApiKeyValidationFilter = this
    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val filterChain = mockk<FilterChain>(relaxed = true)

    @DisplayName("shouldNotFilter 호출 시")
    @Nested
    inner class TestShouldNotFilter {
        @DisplayName("internalUrl 중 요청 URI와 매칭되는 것이 없으면 true를 반환한다")
        @Test
        fun `internalUrl 중 요청 URI와 매칭되는 것이 없으면 true를 반환한다`() {
            //given
            every { request.requestURI } returns "A"
            every { securityUrlProperties.internalUrl } returns arrayOf("B")

            //when
            val result = internalApiKeyValidationFilter.shouldNotFilter(request)

            //then
            assertThat(result).isTrue
        }

        @DisplayName("internalUrl 중 요청 URI와 일치하는 것이 있으면 false를 반환한다")
        @Test
        fun `internalUrl 중 요청 URI와 일치하는 것이 있으면 false를 반환한다`() {
            //given
            every { request.requestURI } returns "A"
            every { securityUrlProperties.internalUrl } returns arrayOf("A")

            //when
            val result = internalApiKeyValidationFilter.shouldNotFilter(request)

            //then
            assertThat(result).isFalse()
        }

        @DisplayName("internalUrl 중 요청 URI와 AntPattern이 일치하는 것이 있으면 false를 반환한다")
        @Test
        fun `internalUrl 중 요청 URI와 AntPattern이 일치하는 것이 있으면 false를 반환한다`() {
            //given
            every { request.requestURI } returns "/A/B"
            every { securityUrlProperties.internalUrl } returns arrayOf("/A/**")

            //when
            val result = internalApiKeyValidationFilter.shouldNotFilter(request)

            //then
            assertThat(result).isFalse()
        }
    }

    @DisplayName("doFilterInternal 호출 시")
    @Nested
    inner class TestDoFilterInternal {
        @DisplayName("apiKey 헤더 값이 null이면 sendError가 호출된다")
        @Test
        fun `apiKey 헤더 값이 null이면 sendError가 호출된다`() {
            //given
            every { request.getHeader(any()) } returns null

            //when
            internalApiKeyValidationFilter.doFilterInternal(request, response, filterChain)

            //then
            verify { response.sendError(HttpStatus.SC_FORBIDDEN, any()) }
        }

        @DisplayName("apiKey 헤더 값이 internalApiKey와 다르면 sendError가 호출된다")
        @Test
        fun `apiKey 헤더 값이 internalApiKey와 다르면 sendError가 호출된다`() {
            //given
            every { request.getHeader(any()) } returns "api-key1"
            every { securityKeyProperties.internalApiKey } returns "api-key2"

            //when
            internalApiKeyValidationFilter.doFilterInternal(request, response, filterChain)

            //then
            verify { response.sendError(HttpStatus.SC_FORBIDDEN, any()) }
        }

        @DisplayName("apiKey 헤더 값이 internalApiKey와 같으면 doFilter가 호출된다")
        @Test
        fun `apiKey 헤더 값이 internalApiKey와 같으면 doFilter가 호출된다`() {
            //given
            every { request.getHeader(any()) } returns "api-key"
            every { securityKeyProperties.internalApiKey } returns "api-key"

            //when
            internalApiKeyValidationFilter.doFilterInternal(request, response, filterChain)

            //then
            verify { filterChain.doFilter(any(), any()) }
        }
    }
}