package dailyquest.annotation

import dailyquest.config.MessageSourceConfig
import dailyquest.config.SecurityConfig
import dailyquest.jwt.JwtAuthorizationFilter
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

@Import(MessageSourceConfig::class)
@WithCustomMockUser
@WebMvcTest(
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class, JwtAuthorizationFilter::class]
        )
    ]
)
annotation class WebMvcUnitTest(
    @get:AliasFor(
        annotation = WebMvcTest::class,
        attribute = "controllers"
    ) val value: Array<KClass<*>>
)