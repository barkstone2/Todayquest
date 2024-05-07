package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.StringTokenizer

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtTokenProperties {
    var accessTokenValidationMillisecondString = ""
    var refreshTokenValidationMillisecondString = ""
    var accessTokenName = ""
    var refreshTokenName = ""
    var sameSite = ""
    var useSecure = true
    var domain = ""

    val accessTokenValidationMillisecond
        get() = parseToMillisecond(accessTokenValidationMillisecondString)
    val refreshTokenValidationMillisecond
        get() = parseToMillisecond(refreshTokenValidationMillisecondString)

    private fun parseToMillisecond(millisecondString: String): Long {
        var millisecond = 0L
        val tokenizer = StringTokenizer(millisecondString, " * ")
        while(tokenizer.hasMoreTokens()) {
            val token = tokenizer.nextToken().toLong()
            millisecond = if(millisecond == 0L) token else millisecond * token
        }
        return millisecond
    }
}