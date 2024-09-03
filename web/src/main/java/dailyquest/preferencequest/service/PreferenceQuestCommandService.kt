package dailyquest.preferencequest.service

import dailyquest.preferencequest.dto.PreferenceQuestRequest
import dailyquest.preferencequest.dto.PreferenceQuestResponse
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.preferencequest.repository.PreferenceQuestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class PreferenceQuestCommandService @Autowired constructor(
    private val preferenceQuestRepository: PreferenceQuestRepository,
) {
    fun savePreferenceQuest(preferenceQuestRequest: PreferenceQuestRequest, userId: Long): PreferenceQuestResponse {
        val preferenceQuest = preferenceQuestRequest.mapToEntity(userId)
        return PreferenceQuestResponse.from(preferenceQuestRepository.save(preferenceQuest))
    }

    fun updatePreferenceQuest(preferenceQuestRequest: PreferenceQuestRequest, updateTarget: PreferenceQuest): PreferenceQuestResponse {
        updateTarget.updatePreferenceQuest(preferenceQuestRequest)
        return PreferenceQuestResponse.from(updateTarget)
    }

    fun deletePreferenceQuest(deleteTarget: PreferenceQuest) {
        deleteTarget.deletePreferenceQuest()
    }
}