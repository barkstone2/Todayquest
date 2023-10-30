package dailyquest.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.search.document.QuestDocument;
import dailyquest.search.repository.QuestIndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*;

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

        Query userIdtermQuery = term()
                .field("userId")
                .value(userId)
                .build()._toQuery();

        Query stateTermQuery = null;
        if(searchCondition.state() != null) {
            stateTermQuery = term()
                    .field("state")
                    .value(searchCondition.state().name())
                    .build()._toQuery();
        }

        Query mulitMatchQuery = multiMatch()
                .fields(searchCondition.keywordType().fieldNames)
                .query(searchCondition.keyword())
                .build()._toQuery();

        Query rangeQuery = null;

        LocalDateTime startDate = searchCondition.startDate();
        LocalDateTime endDate = searchCondition.endDate();

        if(startDate != null && endDate != null) {
            rangeQuery = range()
                    .field("createdDate")
                    .from(startDate.toString())
                    .to(endDate.toString())
                    .build()._toQuery();
        }
        if(startDate != null && endDate == null) {
            rangeQuery = range()
                    .field("createdDate")
                    .from(startDate.toString())
                    .build()._toQuery();
        }
        if(startDate == null && endDate != null) {
            rangeQuery = range()
                    .field("createdDate")
                    .to(endDate.toString())
                    .build()._toQuery();
        }

        BoolQuery.Builder boolQueryBuilder = bool()
                .must(mulitMatchQuery);

        boolQueryBuilder.filter(userIdtermQuery);
        if(stateTermQuery != null) boolQueryBuilder.filter(stateTermQuery);
        if(rangeQuery != null) boolQueryBuilder.filter(rangeQuery);

        Query query = boolQueryBuilder.build()._toQuery();

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .build();

        return operations.search(nativeQuery, QuestDocument.class)
                .stream()
                .map(h -> Long.parseLong(Objects.requireNonNull(h.getId())))
                .toList();
    }
}
