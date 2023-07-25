package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "security.origin")
class SecurityOriginProperties {
    var allowedOrigin: List<String> = listOf()
}