package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "elastic")
class ElasticProperties {
    var username: String = ""
    var password: String = ""
    var host: String = ""
    var port: String = ""

    fun combineHostAndPort(): String {
        return "$host:$port";
    }
}