package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api.batch")
class BatchApiProperties(
    private val serverProtocol: String,
    private val serverAddress: String,
    private val serverPort: String,
    val key: String,
    val keyHeaderName: String,
    val url: BatchApiUrlProperties,
) {
    private fun getServerAddr(): String {
        return "$serverProtocol://$serverAddress:$serverPort"
    }

    fun getCheckAndAchieveUrl(): String {
        return this.getServerAddr() + url.checkAndAchieve
    }

    class BatchApiUrlProperties(val checkAndAchieve: String)
}