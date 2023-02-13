package todayquest.quest.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import todayquest.quest.dto.DetailInteractRequest
import todayquest.quest.dto.DetailResponse
import todayquest.quest.repository.DetailQuestRepository
import todayquest.quest.repository.QuestRepository

@Transactional
@Service
class DetailQuestService(
    private val detailQuestRepository: DetailQuestRepository,
    private val questRepository: QuestRepository
) {

    fun interact(userId: Long, questId: Long, detailQuestId: Long, request: DetailInteractRequest?) : DetailResponse {
        val detailQuest = detailQuestRepository.findByIdOrNull(detailQuestId) ?: throw IllegalArgumentException("비정상적인 접근입니다.")
        detailQuest.checkIsValidRequest(questId, userId)

        val parentQuest = questRepository.getReferenceById(questId)
        parentQuest.checkIsProceedingQuest()

        if(request != null) {
            detailQuest.changeCount(request.count)
            return DetailResponse.createDto(detailQuest, parentQuest.canComplete())
        }

        if(detailQuest.isCompletedDetailQuest()) {
            detailQuest.resetCount()
            return DetailResponse.createDto(detailQuest)
        }

        detailQuest.addCount()

        return DetailResponse.createDto(detailQuest, parentQuest.canComplete())
    }
}