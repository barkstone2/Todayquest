package dailyquest.preferencequest.service

import dailyquest.preferencequest.dto.PreferenceQuestRequest
import dailyquest.preferencequest.dto.PreferenceQuestResponse
import dailyquest.quest.dto.QuestRequest
import dailyquest.quest.dto.QuestResponse
import dailyquest.quest.service.QuestService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class PreferenceQuestService(
    private val preferenceQuestQueryService: PreferenceQuestQueryService,
    private val preferenceQuestCommandService: PreferenceQuestCommandService,
    private val questService: QuestService,
) {

    fun getActivePreferenceQuests(userId: Long): List<PreferenceQuestResponse> {
        return preferenceQuestQueryService.getActivePreferenceQuests(userId)
    }

    fun getPreferenceQuest(preferenceQuestId: Long, userId: Long): PreferenceQuestResponse {
        return PreferenceQuestResponse.from(preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId))
    }

    fun savePreferenceQuest(preferenceQuestRequest: PreferenceQuestRequest, userId: Long): PreferenceQuestResponse {
        return preferenceQuestCommandService.savePreferenceQuest(preferenceQuestRequest, userId)
    }

    fun updatePreferenceQuest(preferenceQuestRequest: PreferenceQuestRequest, preferenceQuestId: Long, userId: Long): PreferenceQuestResponse {
        val updateTarget = preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId)
        return preferenceQuestCommandService.updatePreferenceQuest(preferenceQuestRequest, updateTarget)
    }

    fun deletePreferenceQuest(preferenceQuestId: Long, userId: Long) {
        val deleteTarget = preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId)
        preferenceQuestCommandService.deletePreferenceQuest(deleteTarget)
    }

    fun registerQuestByPreferenceQuest(preferenceQuestId: Long, userId: Long): QuestResponse {
        val preferenceQuest = preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId)
        val questRequest = QuestRequest.from(preferenceQuest)
        return questService.saveQuest(questRequest, userId)
    }
}