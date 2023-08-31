package dailyquest.search.service;

import dailyquest.quest.dto.QuestResponse;
import dailyquest.search.document.QuestDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QuestIndexService {
    private final ElasticsearchOperations searchOperation;

    public void saveDocument(QuestResponse questResponse, Long userId) {
        searchOperation.save(QuestDocument.mapToDocument(questResponse, userId));
    }

    public void deleteDocument(Long questId) {
        searchOperation.delete(String.valueOf(questId), QuestDocument.class);
    }

}
