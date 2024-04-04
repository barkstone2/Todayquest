package dailyquest.log.gold.earn.service

import dailyquest.log.gold.earn.dto.GoldEarnLogRequest
import dailyquest.log.gold.earn.entity.GoldEarnSource
import dailyquest.log.gold.earn.repository.GoldEarnLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class GoldEarnLogService(
    private val goldEarnLogRepository: GoldEarnLogRepository,
) {

    fun getTotalGoldEarnOfUser(userId: Long): Int {
        return goldEarnLogRepository.getTotalGoldEarnOfUser(userId)
    }

    fun getTotalGoldEarnOfUserFromSource(userId: Long, goldEarnSource: GoldEarnSource): Int {
        return goldEarnLogRepository.getTotalGoldEarnOfUserFromSource(userId, goldEarnSource)
    }

    @Transactional
    fun saveGoldEarnLog(goldEarnLogRequest: GoldEarnLogRequest) {
        val requestEntity = goldEarnLogRequest.mapToEntity()
        goldEarnLogRepository.save(requestEntity)
    }
}