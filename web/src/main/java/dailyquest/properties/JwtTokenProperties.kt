package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.StringTokenizer

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtTokenProperties {
    var accessTokenExpirationSecondsFormula = ""
    var refreshTokenExpirationSecondsFormula = ""
    var accessTokenName = ""
    var refreshTokenName = ""
    var sameSite = ""
    var useSecure = true
    var domain = ""

    val accessTokenExpirationSeconds: Int
        get() = parseToSecond(accessTokenExpirationSecondsFormula)
    val refreshTokenExpirationSeconds: Int
        get() = parseToSecond(refreshTokenExpirationSecondsFormula)
    val accessTokenExpirationMilliseconds
        get() = parseToMillisecond(accessTokenExpirationSecondsFormula)
    val refreshTokenExpirationMilliseconds
        get() = parseToMillisecond(refreshTokenExpirationSecondsFormula)

    private fun parseToSecond(secondsFormula: String): Int {
        var second = 0
        val tokenizer = StringTokenizer(secondsFormula, " * ")
        while(tokenizer.hasMoreTokens()) {
            val token = tokenizer.nextToken().toInt()
            second = if(second == 0) token else second * token
        }
        return second
    }

    private fun parseToMillisecond(secondsFormula: String): Long {
        return parseToSecond(secondsFormula) * 1000L
    }
}