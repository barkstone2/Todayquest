package dailyquest.log.gold.earn.dto

import dailyquest.log.gold.earn.entity.GoldEarnLog
import dailyquest.log.gold.earn.entity.GoldEarnSource

class GoldEarnLogRequest(
    val userId: Long,
    val amount: Long,
    val source: GoldEarnSource
) {

    fun mapToEntity(): GoldEarnLog {
        return GoldEarnLog(userId, amount, source)
    }
}