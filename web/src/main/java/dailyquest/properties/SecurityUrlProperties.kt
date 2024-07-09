package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "security.url")
class SecurityUrlProperties {
    var allowedUrl: Array<String> = arrayOf()
    var adminUrl: Array<String> = arrayOf()
}