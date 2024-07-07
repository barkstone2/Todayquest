package dailyquest.quest.service;

import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class QuestQueryService {

    private final QuestRepository questRepository;
    private final MessageSourceAccessor messageSourceAccessor;

    public List<QuestResponse> getCurrentQuests(Long userId, QuestState state) {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        LocalDateTime prevReset = now.withHour(6);
        LocalDateTime nextReset = now.withHour(6);
        if(now.isBefore(nextReset)) {
            prevReset = prevReset.minusDays(1L);
        } else {
            nextReset = nextReset.plusDays(1L);
        }
        return questRepository.getCurrentQuests(userId, state, prevReset, nextReset)
                .stream()
                .map(QuestResponse::createDto)
                .toList();
    }

    public Page<QuestResponse> getQuestsByCondition(Long userId, QuestSearchCondition condition, Pageable pageable) {
        return questRepository.findQuestsByCondition(userId, condition, pageable)
                .map(QuestResponse::createDto);
    }

    public Page<QuestResponse> getSearchedQuests(List<Long> searchedIds,Pageable pageable) {
        return questRepository.getSearchedQuests(searchedIds, pageable)
                .map(QuestResponse::createDto);
    }

    public QuestResponse getQuestInfo(Long questId, Long userId) {
        Quest foundQuest = questRepository.findByIdAndUserId(questId, userId);
        if (foundQuest == null) {
            String entityNotFoundMessage = messageSourceAccessor.getMessage("exception.entity.notfound", new Object[]{messageSourceAccessor.getMessage("quest")});
            throw new EntityNotFoundException(entityNotFoundMessage);
        }
        return QuestResponse.createDto(foundQuest);
    }
}
