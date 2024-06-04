package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "elastic")
class ElasticProperties(
    private val host: String,
    private val port: Int = 9200,
    val username: String,
    val password: String,
    val connectionTimeoutMillis: Long = 5000,
    val socketTimeoutMillis: Long = 5000,
) {

    fun getElasticAddress(): String {
        return "$host:$port"
    }
}