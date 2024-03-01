package dailyquest.user.service

import dailyquest.log.gold.earn.dto.GoldEarnLogRequest
import dailyquest.log.gold.earn.entity.GoldEarnSource
import dailyquest.log.gold.earn.service.GoldEarnLogService
import dailyquest.user.dto.UserExpAndGoldRequest
import dailyquest.user.dto.UserSaveRequest
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class UserCommandService(
    private val userRepository: UserRepository,
    private val goldEarnLogService: GoldEarnLogService,
) {
    fun saveUser(saveRequest: UserSaveRequest): UserInfo {
        val requestEntity = saveRequest.mapToEntity()
        return userRepository.save(requestEntity)
    }

    fun updateUser(updateTarget: UserInfo, updateRequest: UserUpdateRequest): Boolean {
        updateTarget.updateNickname(updateRequest.nickname)
        val updateSucceed = updateTarget.updateCoreTime(updateRequest.coreTime)
        return updateSucceed
    }

    fun updateUserExpAndGold(updateTarget: UserInfo, updateRequest: UserExpAndGoldRequest) {
        val earnedExp = updateRequest.calculateEarnedExp()
        val earnedGold = updateRequest.calculateEarnedGold()
        updateTarget.addExpAndGold(earnedExp, earnedGold)
        val goldEarnLogRequest = GoldEarnLogRequest(earnedGold, GoldEarnSource.QUEST_COMPLETION)
        goldEarnLogService.saveGoldEarnLog(updateTarget.id, goldEarnLogRequest)
    }
}