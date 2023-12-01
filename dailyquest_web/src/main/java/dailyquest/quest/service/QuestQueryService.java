package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.common.RestPage;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.user.entity.UserInfo;
import dailyquest.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class QuestQueryService {

    private final QuestRepository questRepository;
    private final UserRepository userRepository;

    public List<QuestResponse> getCurrentQuests(Long userId, QuestState state) {
        UserInfo findUser = userRepository.getReferenceById(userId);
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        LocalDateTime resetDate = now.withHour(findUser.getResetHour());

        LocalDateTime prevReset;
        LocalDateTime nextReset;

        if(now.isBefore(resetDate)) {
            nextReset = resetDate;
            prevReset = nextReset.minusDays(1L);
        } else {
            prevReset = resetDate;
            nextReset = prevReset.plusDays(1L);
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

    public QuestResponse getQuestInfo(Long questId, Long userId) {
        Quest quest = findByIdOrThrow(questId);
        if(!quest.isQuestOfUser(userId)) throw new AccessDeniedException(MessageUtil.getMessage("exception.access.denied"));

        return QuestResponse.createDto(quest);
    }

    Quest findByIdOrThrow(Long questId) {
        Optional<Quest> findQuest = questRepository.findById(questId);
        return findQuest.orElseThrow(() -> new EntityNotFoundException(
                MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))));
    }

    public RestPage<QuestResponse> getSearchedQuests(List<Long> searchedIds, Long userId, Pageable pageable) {
        return new RestPage<>(
                questRepository.getSearchedQuests(userId, searchedIds, pageable)
                        .map(QuestResponse::createDto)
        );
    }
}
