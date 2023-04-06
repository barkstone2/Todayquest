package todayquest.admin.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import todayquest.admin.dto.SystemSettingsRequest
import todayquest.admin.dto.SystemSettingsResponse
import todayquest.common.MessageUtil
import todayquest.properties.RedisKeyProperties

@Service
class AdminService(
    private val redisKeyProperties: RedisKeyProperties,
    private val redisTemplate: RedisTemplate<String, String>
) {

    fun getSystemSettings(): SystemSettingsResponse {
        val systemSettings = redisTemplate.opsForHash<String, Int>().entries(redisKeyProperties.settings)

        return SystemSettingsResponse(
            questClearExp = systemSettings[redisKeyProperties.questClearExp]!!,
            questClearGold = systemSettings[redisKeyProperties.questClearGold]!!,
            maxRewardCount = systemSettings[redisKeyProperties.maxRewardCount]!!
        )
    }

    fun updateSystemSettings(settingsRequest: SystemSettingsRequest) {
        val ops = redisTemplate.boundHashOps<String, Int>(redisKeyProperties.settings)

        ops.put(redisKeyProperties.questClearGold, settingsRequest.questClearGold)
        ops.put(redisKeyProperties.questClearExp, settingsRequest.questClearExp)
        ops.put(redisKeyProperties.maxRewardCount, settingsRequest.maxRewardCount)
    }

    fun getExpTable(): Map<String, Long> {
        return redisTemplate.opsForHash<String, Long>().entries(redisKeyProperties.expTable)
    }

    fun updateExpTable(expTable: Map<String, Long>) {
        val keys = expTable.keys.mapNotNull { it.toIntOrNull() }.sorted()
        val expectedKeys = (1..keys.size).toList()
        require(keys == expectedKeys) { MessageUtil.getMessage("admin.exception.exp_table.invalid") }

        // 마지막 값을 제외한 value가 0인 경우 오류 발생
        val exceptLastKeys = keys.subList(0, keys.size-1)
        require(exceptLastKeys.none { expTable[it.toString()] == 0L }) { MessageUtil.getMessage("admin.exception.exp_table.zero_value") }

        redisTemplate.delete(redisKeyProperties.expTable)
        redisTemplate.opsForHash<String, Long>().putAll(redisKeyProperties.expTable, expTable)
    }


}