package dailyquest.properties

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "internal.api")
@Component
class InternalApiProperties @Autowired constructor(
    private val urlProperties: InternalApiUrlProperties
) {
    var serverProtocol: String = ""
    var serverAddress: String = ""
    var serverPort: String = "0"
    var key: String = ""
    var keyHeaderName: String = ""

    fun getServerAddr(): String {
        return "$serverProtocol://$serverAddress:$serverPort"
    }

    fun getSseNotifyUrl(): String {
        return this.getServerAddr() + urlProperties.sseNotify
    }

    @ConfigurationProperties(prefix = "internal.api.url")
    @Component
    class InternalApiUrlProperties {
        var sseNotify = ""
    }
}