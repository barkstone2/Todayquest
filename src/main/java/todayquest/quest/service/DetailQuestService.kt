package todayquest.quest.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import todayquest.quest.dto.DetailQuestResponseDto
import todayquest.quest.entity.DetailQuestState
import todayquest.quest.entity.QuestState
import todayquest.quest.repository.DetailQuestRepository
import todayquest.quest.repository.QuestRepository

@Transactional
@Service
class DetailQuestService(
    private val detailQuestRepository: DetailQuestRepository,
    private val questRepository: QuestRepository
) {

    fun interact(userId: Long, questId: Long, detailQuestId: Long) : DetailQuestResponseDto {
        val detailQuest = detailQuestRepository.findByIdOrNull(detailQuestId) ?: throw IllegalArgumentException("비정상적인 접근입니다.")
        if(detailQuest.quest.id != questId) throw IllegalArgumentException("비정상적인 접근입니다.")
        if(detailQuest.quest.user.id != userId) throw IllegalArgumentException("비정상적인 접근입니다.")

        val parentQuest = questRepository.getById(questId)

        if(parentQuest.state != QuestState.PROCEED) throw IllegalArgumentException("이미 완료된 퀘스트입니다.")

        if(detailQuest.state == DetailQuestState.COMPLETE) {
            detailQuest.resetCount()
            return DetailQuestResponseDto.createDto(detailQuest)
        }

        detailQuest.addCount()

        return DetailQuestResponseDto.createDto(detailQuest)
    }
}