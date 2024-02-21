package dailyquest.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.quest.service.QuestQueryService;
import dailyquest.search.document.QuestDocument;
import dailyquest.search.repository.QuestIndexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*;

@RequiredArgsConstructor
@Service
public class QuestIndexService {
    private final QuestIndexRepository questIndexRepository;
    private final QuestQueryService questQueryService;
    private final ElasticsearchOperations operations;

    @Retryable(retryFor = RuntimeException.class)
    @Async
    public void saveDocument(QuestResponse questResponse, Long userId) {
        questIndexRepository.save(questResponse.mapToDocument(userId));
    }

    @Retryable(retryFor = RuntimeException.class)
    @Async
    public void deleteDocument(QuestResponse deletedQuest) {
        questIndexRepository.deleteById(deletedQuest.getId());
    }

    @Retryable(retryFor = RuntimeException.class)
    @Async
    public void updateQuestStateOfDocument(QuestResponse questResponse, Long userId) {
        questIndexRepository.save(questResponse.mapToDocument(userId));
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

        LocalDateTime startDateTime = searchCondition.getStartResetTime();
        LocalDateTime endDateTime = searchCondition.getEndResetTime();

        if(startDateTime != null && endDateTime != null) {
            rangeQuery = range()
                    .field("createdDate")
                    .from(startDateTime.toString())
                    .to(endDateTime.toString())
                    .build()._toQuery();
        }
        if(startDateTime != null && endDateTime == null) {
            rangeQuery = range()
                    .field("createdDate")
                    .from(startDateTime.toString())
                    .build()._toQuery();
        }
        if(startDateTime == null && endDateTime != null) {
            rangeQuery = range()
                    .field("createdDate")
                    .to(endDateTime.toString())
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
