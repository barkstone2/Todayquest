package dailyquest.search.service;

import dailyquest.quest.dto.QuestResponse;
import dailyquest.search.document.QuestDocument;
import dailyquest.search.repository.QuestIndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QuestIndexService {
    private final QuestIndexRepository questIndexRepository;

    public void saveDocument(QuestResponse questResponse, Long userId) {
        questIndexRepository.save(QuestDocument.mapToDocument(questResponse, userId));
    }

    public void deleteDocument(Long questId) {
        questIndexRepository.deleteById(questId);
    }

}
