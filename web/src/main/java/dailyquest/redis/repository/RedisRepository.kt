package dailyquest.redis.repository

import dailyquest.exception.RedisDataNotFoundException
import dailyquest.properties.RedisKeyProperties
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class RedisRepository(
    val redisTemplate: RedisTemplate<String, String>,
    val redisKeyProperties: RedisKeyProperties,
    private val messageSourceAccessor: MessageSourceAccessor
) {

    fun getExpTable(): Map<Int, Long> {
        return redisTemplate.opsForHash<Int, Long>().entries(redisKeyProperties.expTable)
    }

    fun getQuestClearExp(): Long {
        return this.getSettingValue(redisKeyProperties.questClearExp)
    }

    fun getQuestClearGold(): Long {
        return this.getSettingValue(redisKeyProperties.questClearExp)
    }

    private fun getSettingValue(key: String): Long {
        val boundHashOps = redisTemplate.boundHashOps<String, Long>(redisKeyProperties.settings)
        return boundHashOps.get(key) ?: throw RedisDataNotFoundException(messageSourceAccessor.getMessage("exception.server.error"))
    }

    fun getRandomNicknamePrefix(): String {
        return this.getRandomSetMember(redisKeyProperties.nicknamePrefix)
    }

    fun getRandomNicknamePostfix(): String {
        return this.getRandomSetMember(redisKeyProperties.nicknamePostfix)
    }

    private fun getRandomSetMember(setKey: String): String {
        return redisTemplate.opsForSet().randomMember(setKey)
    }



}