package dailyquest.search.service;

import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.quest.dto.QuestSearchKeywordType;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.service.QuestQueryService;
import dailyquest.search.document.QuestDocument;
import dailyquest.search.repository.QuestIndexRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.data.client.orhlc.NativeSearchQuery;
import org.opensearch.index.query.BoolQueryBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.opensearch.index.query.QueryBuilders.*;
import static org.opensearch.index.query.QueryBuilders.rangeQuery;

@ExtendWith(MockitoExtension.class)
@DisplayName("퀘스트 인덱스 서비스 단위 테스트")
public class QuestIndexServiceUnitTest {

    @InjectMocks
    QuestIndexService questIndexService;

    @Mock
    QuestIndexRepository questIndexRepository;

    @Mock
    QuestQueryService questQueryService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ElasticsearchOperations operations;

    @DisplayName("문서 검색 요청 시")
    @Nested
    class SearchDocumentTest {

        @DisplayName("모든 검색 조건이 null이면 추가 검색 조건이 쿼리에 추가되지 않는다")
        @Test
        public void doesNotAppendAdditionalQueryWhenConditionIsNull() throws Exception {
            //given
            long userId = 1L;
            QuestSearchCondition searchCondition = new QuestSearchCondition(0, null, QuestSearchKeywordType.ALL, "keyword", null, null);
            Pageable pageable = PageRequest.of(0, 10);

            ArgumentCaptor<NativeSearchQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeSearchQuery.class);

            BoolQueryBuilder query = new BoolQueryBuilder();
            query.must(multiMatchQuery(searchCondition.keyword(), searchCondition.keywordType().fieldNames));
            query.filter(termQuery("userId", userId));

            //when
            questIndexService.searchDocuments(searchCondition, userId, pageable);

            //then
            verify(operations).search(nativeQueryCaptor.capture(), eq(QuestDocument.class));
            assertThat(nativeQueryCaptor.getValue().getQuery().toString()).isEqualTo(query.toString());
        }

        @DisplayName("상태 조건이 존재하면 상태 조건 필터가 쿼리에 추가된다")
        @Test
        public void addStateFilterWhenConditionExist() throws Exception {
            //given
            long userId = 1L;
            QuestSearchCondition searchCondition = new QuestSearchCondition(0, QuestState.PROCEED, QuestSearchKeywordType.ALL, "keyword", null, null);
            Pageable pageable = PageRequest.of(0, 10);

            ArgumentCaptor<NativeSearchQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeSearchQuery.class);

            BoolQueryBuilder query = new BoolQueryBuilder();
            query.must(multiMatchQuery(searchCondition.keyword(), searchCondition.keywordType().fieldNames));
            query.filter(termQuery("userId", userId));
            query.filter(termQuery("state", searchCondition.state().name()));

            //when
            questIndexService.searchDocuments(searchCondition, userId, pageable);

            //then
            verify(operations).search(nativeQueryCaptor.capture(), eq(QuestDocument.class));
            assertThat(nativeQueryCaptor.getValue().getQuery().toString()).isEqualTo(query.toString());
        }

        @DisplayName("날짜 시작 범위 조건이 존재하면 쿼리에 추가된다")
        @Test
        public void addStartDateFilterWhenConditionExist() throws Exception {
            //given
            long userId = 1L;
            LocalDate startDate = LocalDate.of(2022, 12, 12);
            LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.of(6, 0));
            QuestSearchCondition searchCondition = new QuestSearchCondition(0, null, QuestSearchKeywordType.ALL, "keyword", startDate, null);
            Pageable pageable = PageRequest.of(0, 10);

            ArgumentCaptor<NativeSearchQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeSearchQuery.class);

            BoolQueryBuilder query = new BoolQueryBuilder();
            query.must(multiMatchQuery(searchCondition.keyword(), searchCondition.keywordType().fieldNames));
            query.filter(termQuery("userId", userId));
            query.filter(rangeQuery("createdDate").from(startDateTime.toString()));

            //when
            questIndexService.searchDocuments(searchCondition, userId, pageable);

            //then
            verify(operations).search(nativeQueryCaptor.capture(), eq(QuestDocument.class));
            assertThat(nativeQueryCaptor.getValue().getQuery().toString()).isEqualTo(query.toString());
        }

        @DisplayName("날짜 끝 범위 조건이 존재하면 쿼리에 추가된다")
        @Test
        public void addEndDateFilterWhenConditionExist() throws Exception {
            //given
            long userId = 1L;
            LocalDate endDate = LocalDate.of(2022, 12, 12);
            LocalDateTime endDateTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(6, 0));
            QuestSearchCondition searchCondition = new QuestSearchCondition(0, null, QuestSearchKeywordType.ALL, "keyword", null, endDate);
            Pageable pageable = PageRequest.of(0, 10);

            ArgumentCaptor<NativeSearchQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeSearchQuery.class);

            BoolQueryBuilder query = new BoolQueryBuilder();
            query.must(multiMatchQuery(searchCondition.keyword(), searchCondition.keywordType().fieldNames));
            query.filter(termQuery("userId", userId));
            query.filter(rangeQuery("createdDate").to(endDateTime.toString()));

            //when
            questIndexService.searchDocuments(searchCondition, userId, pageable);

            //then
            verify(operations).search(nativeQueryCaptor.capture(), eq(QuestDocument.class));
            assertThat(nativeQueryCaptor.getValue().getQuery().toString()).isEqualTo(query.toString());
        }

        @DisplayName("날짜 시작 범위와 끝 범위 조건이 둘 다 존재하면 쿼리에 추가된다")
        @Test
        public void addStartAndEndDateFilterWhenConditionExist() throws Exception {
            //given
            long userId = 1L;
            LocalDate startDate = LocalDate.of(2022, 12, 12);
            LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.of(6, 0));
            LocalDate endDate = LocalDate.of(2022, 12, 12);
            LocalDateTime endDateTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(6, 0));
            QuestSearchCondition searchCondition = new QuestSearchCondition(0, null, QuestSearchKeywordType.ALL, "keyword", startDate, endDate);
            Pageable pageable = PageRequest.of(0, 10);

            ArgumentCaptor<NativeSearchQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeSearchQuery.class);

            BoolQueryBuilder query = new BoolQueryBuilder();
            query.must(multiMatchQuery(searchCondition.keyword(), searchCondition.keywordType().fieldNames));
            query.filter(termQuery("userId", userId));
            query.filter(rangeQuery("createdDate")
                    .from(startDateTime.toString())
                    .to(endDateTime.toString())
            );

            //when
            questIndexService.searchDocuments(searchCondition, userId, pageable);

            //then
            verify(operations).search(nativeQueryCaptor.capture(), eq(QuestDocument.class));
            assertThat(nativeQueryCaptor.getValue().getQuery().toString()).isEqualTo(query.toString());
        }

        @DisplayName("페이징 정보가 쿼리에 추가된다")
        @Test
        public void addPagingInfoToQuery() throws Exception {
            //given
            long userId = 1L;
            QuestSearchCondition searchCondition = new QuestSearchCondition(0, null, QuestSearchKeywordType.ALL, "keyword", null, null);
            Pageable pageable = PageRequest.of(0, 10);
            ArgumentCaptor<NativeSearchQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeSearchQuery.class);

            //when
            questIndexService.searchDocuments(searchCondition, userId, pageable);

            //then
            verify(operations).search(nativeQueryCaptor.capture(), eq(QuestDocument.class));
            assertThat(nativeQueryCaptor.getValue().getPageable()).isEqualTo(pageable);
        }
    }

}
