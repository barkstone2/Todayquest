package dailyquest.log.gold.earn.service

import dailyquest.log.gold.earn.dto.GoldEarnLogRequest
import dailyquest.log.gold.earn.entity.GoldEarnLog
import dailyquest.log.gold.earn.entity.GoldEarnSource
import dailyquest.log.gold.earn.repository.GoldEarnLogRepository
import dailyquest.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class GoldEarnLogService(
    val goldEarnLogRepository: GoldEarnLogRepository,
    val userRepository: UserRepository
) {

    fun getTotalGoldEarnOfUser(userId: Long): Int {
        return goldEarnLogRepository.getTotalGoldEarnOfUser(userId)
    }

    fun getTotalGoldEarnOfUserFromSource(userId: Long, source: GoldEarnSource): Int {
        return goldEarnLogRepository.getTotalGoldEarnOfUserFromSource(userId, source)
    }

    fun saveGoldEarnLog(userId: Long, request: GoldEarnLogRequest) {
        val user = userRepository.findById(userId).get()
        val goldEarnLog = GoldEarnLog(user, request.amount, request.source)
        goldEarnLogRepository.save(goldEarnLog)
    }

}