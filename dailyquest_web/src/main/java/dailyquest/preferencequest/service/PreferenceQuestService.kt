package dailyquest.preferencequest.service

import dailyquest.preferencequest.dto.PreferenceQuestRequest
import dailyquest.preferencequest.dto.PreferenceQuestResponse
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

    fun getAllPreferenceQuests(userId: Long): List<PreferenceQuestResponse> {
        return preferenceQuestQueryService.getAllPreferenceQuests(userId)
    }

    fun getPreferenceQuest(preferenceQuestId: Long, userId: Long): PreferenceQuestResponse {
        return preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId)
    }

    fun savePreferenceQuest(preferenceQuestRequest: PreferenceQuestRequest, userId: Long): PreferenceQuestResponse {
        preferenceQuestRequest.checkRangeOfDeadLine()
        return preferenceQuestCommandService.savePreferenceQuest(preferenceQuestRequest, userId)
    }

    fun updatePreferenceQuest(preferenceQuestRequest: PreferenceQuestRequest, preferenceQuestId: Long, userId: Long): PreferenceQuestResponse {
        return preferenceQuestCommandService.updatePreferenceQuest(preferenceQuestRequest, preferenceQuestId, userId)
    }

    fun deletePreferenceQuest(preferenceQuestId: Long, userId: Long) {
        preferenceQuestCommandService.deletePreferenceQuest(preferenceQuestId, userId)
    }

    fun registerQuestByPreferenceQuest(preferenceQuestId: Long, userId: Long): QuestResponse {
        val preferenceQuest = preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId)
        val questRequest = preferenceQuest.mapToQuestRequest()
        return questService.saveQuest(questRequest, userId)
    }


}