package dailyquest.config

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@ConfigurationPropertiesScan("dailyquest.properties")
@Configuration
class PropertiesConfig