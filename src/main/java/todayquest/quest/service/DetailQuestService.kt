package todayquest.quest.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import todayquest.quest.dto.DetailQuestResponseDto
import todayquest.quest.entity.DetailQuestState
import todayquest.quest.repository.DetailQuestRepository

@Transactional
@Service
open class DetailQuestService(
    private val detailQuestRepository: DetailQuestRepository
) {

    fun interact(userId: Long, questId: Long, detailQuestId: Long) : DetailQuestResponseDto {
        val detailQuest = detailQuestRepository.findByIdOrNull(detailQuestId) ?: throw IllegalArgumentException("")
        if(detailQuest.quest.id != questId) throw IllegalArgumentException()
        if(detailQuest.quest.user.id != userId) throw IllegalArgumentException()

        if(detailQuest.state == DetailQuestState.COMPLETE) {
            detailQuest.changeState(DetailQuestState.PROCEED)
            detailQuest.resetCount()
            return DetailQuestResponseDto.createDto(detailQuest)
        }

        if(detailQuest.addCount()) {
            detailQuest.changeState(DetailQuestState.COMPLETE)
        }

        return DetailQuestResponseDto.createDto(detailQuest)
    }
}