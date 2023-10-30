package dailyquest.search.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.dto.QuestSearchCondition;
import dailyquest.quest.dto.QuestSearchKeywordType;
import dailyquest.quest.entity.QuestState;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.time.LocalDateTime;

import static co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("퀘스트 인덱스 서비스 단위 테스트")
public class QuestIndexServiceUnitTest {

    @InjectMocks
    QuestIndexService questIndexService;

    @Mock
    QuestIndexRepository questIndexRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ElasticsearchOperations operations;

    @DisplayName("퀘스트 상태 변경 메서드 호출 시 문서 업데이트 전에 상태가 변경된다")
    @Test
    public void setStateBeforeUpdateDocument() throws Exception {
        //given
        QuestResponse response = mock(QuestResponse.class, Answers.RETURNS_DEEP_STUBS);
        QuestState state = QuestState.COMPLETE;
        Long userId = 1L;

        //when
        questIndexService.updateQuestStateOfDocument(response, state, userId);

        //then
        verify(response).setState(eq(state));
        verify(questIndexRepository).save(any());
    }

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

            ArgumentCaptor<NativeQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

            Query query = bool()
                    .must(multiMatch()
                            .fields(searchCondition.keywordType().fieldNames)
                            .query(searchCondition.keyword())
                            .build()._toQuery())
                    .filter(term()
                            .field("userId")
                            .value(userId)
                            .build()._toQuery())
                    .build()._toQuery();

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

            ArgumentCaptor<NativeQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

            Query query = bool()
                    .must(multiMatch()
                            .fields(searchCondition.keywordType().fieldNames)
                            .query(searchCondition.keyword())
                            .build()._toQuery())
                    .filter(term()
                            .field("userId")
                            .value(userId)
                            .build()._toQuery())
                    .filter(term()
                            .field("state")
                            .value(searchCondition.state().name())
                            .build()._toQuery())
                    .build()._toQuery();

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
            LocalDateTime startDate = LocalDateTime.of(2022, 12, 12, 12, 0, 0);
            QuestSearchCondition searchCondition = new QuestSearchCondition(0, null, QuestSearchKeywordType.ALL, "keyword", startDate, null);
            Pageable pageable = PageRequest.of(0, 10);

            ArgumentCaptor<NativeQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

            Query query = bool()
                    .must(multiMatch()
                            .fields(searchCondition.keywordType().fieldNames)
                            .query(searchCondition.keyword())
                            .build()._toQuery())
                    .filter(term()
                            .field("userId")
                            .value(userId)
                            .build()._toQuery())
                    .filter(range()
                            .field("createdDate")
                            .from(startDate.toString())
                            .build()._toQuery())
                    .build()._toQuery();

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
            LocalDateTime endDate = LocalDateTime.of(2022, 12, 12, 12, 0, 0);
            QuestSearchCondition searchCondition = new QuestSearchCondition(0, null, QuestSearchKeywordType.ALL, "keyword", null, endDate);
            Pageable pageable = PageRequest.of(0, 10);

            ArgumentCaptor<NativeQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

            Query query = bool()
                    .must(multiMatch()
                            .fields(searchCondition.keywordType().fieldNames)
                            .query(searchCondition.keyword())
                            .build()._toQuery())
                    .filter(term()
                            .field("userId")
                            .value(userId)
                            .build()._toQuery())
                    .filter(range()
                            .field("createdDate")
                            .to(endDate.toString())
                            .build()._toQuery())
                    .build()._toQuery();

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
            LocalDateTime startDate = LocalDateTime.of(2022, 12, 12, 12, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2022, 12, 12, 12, 0, 0);
            QuestSearchCondition searchCondition = new QuestSearchCondition(0, null, QuestSearchKeywordType.ALL, "keyword", startDate, endDate);
            Pageable pageable = PageRequest.of(0, 10);

            ArgumentCaptor<NativeQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

            Query query = bool()
                    .must(multiMatch()
                            .fields(searchCondition.keywordType().fieldNames)
                            .query(searchCondition.keyword())
                            .build()._toQuery())
                    .filter(term()
                            .field("userId")
                            .value(userId)
                            .build()._toQuery())
                    .filter(range()
                            .field("createdDate")
                            .from(startDate.toString())
                            .to(endDate.toString())
                            .build()._toQuery())
                    .build()._toQuery();

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
            ArgumentCaptor<NativeQuery> nativeQueryCaptor = ArgumentCaptor.forClass(NativeQuery.class);

            //when
            questIndexService.searchDocuments(searchCondition, userId, pageable);

            //then
            verify(operations).search(nativeQueryCaptor.capture(), eq(QuestDocument.class));
            assertThat(nativeQueryCaptor.getValue().getPageable()).isEqualTo(pageable);
        }
    }

}
