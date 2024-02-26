package dailyquest.user.service

import dailyquest.common.MessageUtil
import dailyquest.user.dto.UserExpAndGoldRequest
import dailyquest.user.dto.UserSaveRequest
import dailyquest.user.dto.UserUpdateRequest
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class UserCommandService(
    private val userRepository: UserRepository,
) {
    fun saveUser(saveRequest: UserSaveRequest): UserInfo {
        val requestEntity = saveRequest.mapToEntity()
        return userRepository.save(requestEntity)
    }

    fun updateUser(updateTarget: UserInfo, updateRequest: UserUpdateRequest) {
        updateTarget.updateNickname(updateRequest.nickname)
        val requestedDate = LocalDateTime.now().withSecond(0).withNano(0)
        if (!updateTarget.updateCoreTime(updateRequest.coreTime, requestedDate)) {
            val diff: Duration = updateTarget.getRemainTimeUntilCoreTimeUpdateAvailable(requestedDate)
            val diffStr = String.format("%d시간 %d분", diff.toHours(), diff.toMinutes() % 60)
            throw IllegalStateException(
                MessageUtil.getMessage(
                    "user.settings.update_limit",
                    MessageUtil.getMessage("user.settings.core_time"),
                    diffStr
                )
            )
        }
    }

    fun updateUserExpAndGold(updateTarget: UserInfo, updateRequest: UserExpAndGoldRequest) {
        updateTarget.updateExpAndGold(updateRequest.type, updateRequest.earnedExp, updateRequest.earnedGold)
    }
}