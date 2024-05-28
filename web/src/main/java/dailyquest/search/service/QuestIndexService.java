package dailyquest.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.search.document.QuestDocument;
import dailyquest.sqs.dto.ElasticSyncMessage;
import dailyquest.sqs.dto.ElasticSyncRequestType;
import dailyquest.sqs.service.SqsService;
import lombok.RequiredArgsConstructor;
import org.opensearch.data.client.orhlc.NativeSearchQuery;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.index.query.TermQueryBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchOperations;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.opensearch.index.query.QueryBuilders.*;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@RequiredArgsConstructor
@Service
public class QuestIndexService {
    private final SearchOperations operations;
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
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(multiMatchQuery(searchCondition.keyword(), searchCondition.keywordType().fieldNames));
        boolQueryBuilder.filter(termQuery("userId", userId));
        if(searchCondition.state() != null) {
            boolQueryBuilder.filter(termQuery("state", searchCondition.state().name()));
        }
        RangeQueryBuilder rangeQuery = this.getCreateDateRangeQueryIfNotNull(searchCondition);
        if(rangeQuery != null) {
            boolQueryBuilder.filter(rangeQuery);
        }

        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(boolQueryBuilder);
        nativeSearchQuery.setPageable(pageable);

        return operations.search(nativeSearchQuery, QuestDocument.class)
                .stream()
                .map(h -> Long.parseLong(Objects.requireNonNull(h.getId())))
                .toList();
    }

    private RangeQueryBuilder getCreateDateRangeQueryIfNotNull(QuestSearchCondition searchCondition) {
        RangeQueryBuilder rangeQuery = null;
        LocalDateTime startDateTime = searchCondition.getStartResetTime();
        LocalDateTime endDateTime = searchCondition.getEndResetTime();
        if(startDateTime != null && endDateTime != null) {
            rangeQuery = rangeQuery("createdDate")
                    .from(startDateTime.toString())
                    .to(endDateTime.toString());
        }
        if(startDateTime != null && endDateTime == null) {
            rangeQuery = rangeQuery("createdDate")
                    .from(startDateTime.toString());
        }
        if(startDateTime == null && endDateTime != null) {
            rangeQuery = rangeQuery("createdDate")
                    .to(endDateTime.toString());
        }
        return rangeQuery;
    }
}
