package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "redis.key")
class RedisKeyProperties {

    var expTable: String = ""

    var nicknamePrefix: String = ""
    var nicknamePostfix: String = ""

    var settings: String = ""
    var questClearExp: String = ""
    var questClearGold: String = ""
    var maxRewardCount: String = ""

}