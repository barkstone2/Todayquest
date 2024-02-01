package dailyquest.preferencequest.service

import dailyquest.common.MessageUtil
import dailyquest.preferencequest.dto.PreferenceQuestRequest
import dailyquest.preferencequest.dto.PreferenceQuestResponse
import dailyquest.preferencequest.repository.PreferenceQuestRepository
import dailyquest.user.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class PreferenceQuestCommandService @Autowired constructor(
    private val preferenceQuestRepository: PreferenceQuestRepository,
    private val userRepository: UserRepository,
) {
    fun savePreferenceQuest(preferenceQuestRequest: PreferenceQuestRequest, userId: Long): PreferenceQuestResponse {
        val userInfo = userRepository.findById(userId).get()
        val preferenceQuest = preferenceQuestRequest.mapToEntity(userInfo)
        return PreferenceQuestResponse.createDto(preferenceQuestRepository.save(preferenceQuest))
    }

    fun updatePreferenceQuest(preferenceQuestRequest: PreferenceQuestRequest, preferenceQuestId: Long, userId: Long): PreferenceQuestResponse {
        val preferenceQuest =
            preferenceQuestRepository.findByIdAndUserIdAndDeletedDateIsNull(preferenceQuestId, userId) ?: throw EntityNotFoundException(
                MessageUtil.getMessage(
                    "exception.entity.notfound", MessageUtil.getMessage("preference_quest"),
                )
            )

        preferenceQuest.updatePreferenceQuest(
            title = preferenceQuestRequest.title,
            description = preferenceQuestRequest.description,
            deadLine = preferenceQuestRequest.deadLine,
            details = preferenceQuestRequest.details.map { Pair(it.id, it.mapToEntity(preferenceQuest)) }
        )

        return PreferenceQuestResponse.createDto(preferenceQuest)
    }

    fun deletePreferenceQuest(preferenceQuestId: Long, userId: Long) {
        preferenceQuestRepository.deleteByIdAndUserIdAndDeletedDateIsNull(preferenceQuestId, userId)
    }
}