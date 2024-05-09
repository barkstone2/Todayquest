package dailyquest.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.search.document.QuestDocument;
import dailyquest.sqs.service.SqsService;
import dailyquest.sqs.dto.ElasticSyncMessage;
import dailyquest.sqs.dto.ElasticSyncRequestType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@RequiredArgsConstructor
@Service
public class QuestIndexService {
    private final ElasticsearchOperations operations;
    private final ObjectMapper objectMapper;
    private final SqsService sqsService;

    @Retryable(retryFor = RuntimeException.class)
    @Async
    public void saveDocument(QuestResponse questResponse, Long userId) {
        QuestDocument questDocument = questResponse.mapToDocument(userId);
        try {
            String documentJson = objectMapper.writeValueAsString(questDocument);
            ElasticSyncMessage elasticSyncMessage = ElasticSyncMessage.of(ElasticSyncRequestType.PERSIST, questDocument.getId(), documentJson);
            sqsService.publishElasticSyncMessage(elasticSyncMessage);
        } catch (JsonProcessingException ignored) {
        }
    }

    @Retryable(retryFor = RuntimeException.class)
    @Async
    public void deleteDocument(QuestResponse deletedQuest) {
        ElasticSyncMessage elasticSyncMessage = ElasticSyncMessage.of(ElasticSyncRequestType.DELETE, deletedQuest.getId(), "");
        sqsService.publishElasticSyncMessage(elasticSyncMessage);
    }

    @Retryable(retryFor = RuntimeException.class)
    @Async
    public void updateQuestStateOfDocument(QuestResponse questResponse, Long userId) {
        QuestDocument questDocument = questResponse.mapToDocument(userId);
        try {
            String documentJson = objectMapper.writeValueAsString(questDocument);
            ElasticSyncMessage elasticSyncMessage = ElasticSyncMessage.of(ElasticSyncRequestType.PERSIST, questDocument.getId(), documentJson);
            sqsService.publishElasticSyncMessage(elasticSyncMessage);
        } catch (JsonProcessingException ignored) {
        }
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
