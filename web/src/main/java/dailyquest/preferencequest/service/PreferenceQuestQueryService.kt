package dailyquest.preferencequest.service

import dailyquest.preferencequest.dto.PreferenceQuestResponse
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.preferencequest.repository.PreferenceQuestRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class PreferenceQuestQueryService @Autowired constructor(
    private val preferenceQuestRepository: PreferenceQuestRepository,
    private val messageSourceAccessor: MessageSourceAccessor
) {
    fun getActivePreferenceQuests(userId: Long): List<PreferenceQuestResponse> {
        val preferenceQuests = preferenceQuestRepository.getActivePrefQuests(userId)
        val usedCounts = preferenceQuestRepository.getUsedCountOfActivePrefQuests(userId)
        val mappedPrefQuests = preferenceQuests.mapIndexed { index, preferenceQuest ->
            PreferenceQuestResponse.of(preferenceQuest, usedCounts[index])
        }
        return mappedPrefQuests
    }

     fun getPreferenceQuest(preferenceQuestId: Long, userId: Long): PreferenceQuest {
         return preferenceQuestRepository.findByIdAndUserIdAndDeletedDateIsNull(preferenceQuestId, userId) ?: throw EntityNotFoundException(
                 messageSourceAccessor.getMessage(
                     "exception.entity.notfound", messageSourceAccessor.getMessage("preference_quest"),
                 )
             )
    }
}