package dailyquest.search.service;

import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.quest.dto.QuestSearchKeywordType;
import dailyquest.search.document.QuestDocument;
import dailyquest.search.repository.QuestIndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class QuestIndexService {
    private final QuestIndexRepository questIndexRepository;
    private final ElasticsearchOperations operations;

    public void saveDocument(QuestResponse questResponse, Long userId) {
        questIndexRepository.save(QuestDocument.mapToDocument(questResponse, userId));
    }

    public void deleteDocument(Long questId) {
        questIndexRepository.deleteById(questId);
    }

    public List<Long> searchDocuments(QuestSearchCondition searchCondition, Long userId, Pageable pageable) {

        Criteria subCriteria = null;

        for (String filedName : searchCondition.keywordType().fieldNames) {
            if(subCriteria == null) {
                subCriteria = Criteria.where(filedName).matches(searchCondition.keyword());
                if(filedName.equals(QuestSearchKeywordType.FieldType.TITLE)) {
                    subCriteria = subCriteria.boost(2.0f);
                }
            } else {
                subCriteria = subCriteria.or(filedName).matches(searchCondition.keyword());
            }
        }

        assert subCriteria != null;

        Criteria criteria = Criteria.where("userId").is(userId)
                .subCriteria(subCriteria);

        CriteriaQuery criteriaQuery = CriteriaQuery.builder(criteria).withPageable(pageable).build();

        return operations.search(criteriaQuery, QuestDocument.class)
                .stream()
                .map(h -> Long.parseLong(Objects.requireNonNull(h.getId())))
                .toList();
    }
}
