package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "security.key")
@Component
class SecurityKeyProperties {
    var internalApiKey: String = ""
}