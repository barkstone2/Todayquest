package dailyquest.preferencequest.service

import dailyquest.common.MessageUtil
import dailyquest.preferencequest.dto.PreferenceQuestResponse
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.preferencequest.repository.PreferenceQuestRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class PreferenceQuestQueryService @Autowired constructor(
    private val preferenceQuestRepository: PreferenceQuestRepository
) {
    fun getAllPreferenceQuests(userId: Long): List<PreferenceQuestResponse> {
        return preferenceQuestRepository.findAllByUserIdAndDeletedDateIsNull(userId).map { PreferenceQuestResponse.createDto(it) }
    }

     fun getPreferenceQuest(preferenceQuestId: Long, userId: Long): PreferenceQuest {
         return preferenceQuestRepository.findByIdAndUserIdAndDeletedDateIsNull(preferenceQuestId, userId) ?: throw EntityNotFoundException(
                 MessageUtil.getMessage(
                     "exception.entity.notfound", MessageUtil.getMessage("preference_quest"),
                 )
             )
    }
}