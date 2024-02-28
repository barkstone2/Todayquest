package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.common.RestPage;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    public List<QuestResponse> getCurrentQuests(Long userId, QuestState state) {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        LocalDateTime prevReset = now.withHour(6).withMinute(0);
        LocalDateTime nextReset = now.withHour(6).withMinute(0);

        if(now.isBefore(nextReset)) {
            prevReset = prevReset.minusDays(1L);
        } else {
            nextReset = nextReset.plusDays(1L);
        }

        return questRepository
                .getCurrentQuests(userId, state, prevReset, nextReset)
                .stream()
                .map(QuestResponse::createDto).toList();
    }

    public RestPage<QuestResponse> getQuestsByCondition(Long userId, QuestSearchCondition condition, Pageable pageable) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        return new RestPage<>(
                questRepository
                        .findQuestsByCondition(userId, condition, pageRequest)
                        .map(QuestResponse::createDto)
        );
    }

    public Quest getProceedEntityOfUser(Long questId, Long userId) throws IllegalStateException {
        Quest quest = getEntityOfUser(questId, userId);
        if(!quest.isProceed())
            throw new IllegalStateException(MessageUtil.getMessage("quest.error.not-proceed"));
        return quest;
    }

    public Quest getEntityOfUser(Long questId, Long userId) throws EntityNotFoundException {
        Quest findQuest = questRepository.findByIdAndUserId(questId, userId);
        if (findQuest == null) {
            String entityNotFoundMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"));
            throw new EntityNotFoundException(entityNotFoundMessage);
        }
        return findQuest;
    }

    public RestPage<QuestResponse> getSearchedQuests(List<Long> searchedIds,Pageable pageable) {
        return new RestPage<>(
                questRepository.getSearchedQuests(searchedIds, pageable)
                        .map(QuestResponse::createDto)
        );
    }
}
