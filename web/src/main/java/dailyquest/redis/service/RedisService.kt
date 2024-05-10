package dailyquest.redis.service

import dailyquest.redis.repository.RedisRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class RedisService(
    private val redisTemplate: RedisRepository,
) {

    fun getExpTable(): Map<Int, Long> {
        return redisTemplate.getExpTable()
    }

    fun getQuestClearExp(): Long {
        return redisTemplate.getQuestClearExp()
    }

    fun getQuestClearGold(): Long {
        return redisTemplate.getQuestClearGold()
    }

    fun createRandomNickname(): String {
        val nicknamePrefix = redisTemplate.getRandomNicknamePrefix()
        val nicknamePostfix = redisTemplate.getRandomNicknamePostfix()
        return nicknamePrefix + nicknamePostfix + Random().nextInt(1000000000)
    }
}