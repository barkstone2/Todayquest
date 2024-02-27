package dailyquest.user.service

import dailyquest.user.dto.UserExpAndGoldRequest
import dailyquest.user.dto.UserSaveRequest
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserCommandService(
    private val userRepository: UserRepository,
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
        updateTarget.addExpAndGold(updateRequest.calculateEarnedExp(), updateRequest.calculateEarnedGold())
    }
}