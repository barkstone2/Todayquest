package dailyquest.common

import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

fun MockHttpServletRequestBuilder.unitTestDefaultConfiguration(): MockHttpServletRequestBuilder {
    return this.contentType(MediaType.APPLICATION_JSON)
        .with(SecurityMockMvcRequestPostProcessors.csrf())
}

fun MockHttpServletRequestDsl.unitTestDefaultConfiguration() {
    this.contentType = MediaType.APPLICATION_JSON
    this.with(SecurityMockMvcRequestPostProcessors.csrf())
}