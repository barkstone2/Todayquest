package dailyquest.user.service

import dailyquest.common.MessageUtil
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class UserQueryService(
    val userRepository: UserRepository
) {
    @Throws(EntityNotFoundException::class)
    fun findUser(userId: Long): UserInfo {
        val foundUser = userRepository.findById(userId).getOrNull()
        val entityNotFoundMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("user"))
        return foundUser ?: throw EntityNotFoundException(entityNotFoundMessage)
    }

    fun findUser(oauth2Id: String): UserInfo? {
        return userRepository.findByOauth2Id(oauth2Id)
    }

    fun isDuplicateNickname(nickname: String): Boolean {
        return userRepository.existsByNickname(nickname)
    }
}