package dailyquest.quest.service

import dailyquest.quest.dto.*
import dailyquest.quest.entity.QuestState
import dailyquest.search.service.QuestIndexService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.Pageable

@ExtendWith(MockitoExtension::class)
@DisplayName("퀘스트 서비스 유닛 테스트")
class QuestServiceUnitTest {

    @InjectMocks
    lateinit var questService: QuestService

    @Mock lateinit var questQueryService: QuestQueryService
    @Mock lateinit var questCommandService: QuestCommandService
    @Mock lateinit var questIndexService: QuestIndexService

    @DisplayName("퀘스트 검색 시")
    @Nested
    inner class QuestSearchTest {

        @DisplayName("키워드 검색인 경우 오픈서치 서비스에 검색 요청을 위임한다")
        @Test
        fun `키워드 검색인 경우 오픈서치 서비스에 검색 요청을 위임한다`() {
            //given
            val userId = 1L
            val searchCondition =
                QuestSearchCondition(0, QuestState.PROCEED, QuestSearchKeywordType.ALL, "keyword", null, null)
            val list = mutableListOf<Long>()
            val pageable = Pageable.ofSize(100)

            doReturn(list).`when`(questIndexService).searchDocuments(eq(searchCondition), eq(userId), eq(pageable))

            //when
            val result = questService.searchQuest(userId, searchCondition, pageable)

            //then
            verify(questIndexService, times(1)).searchDocuments(eq(searchCondition), eq(userId), eq(pageable))
            verify(questQueryService, times(1)).getSearchedQuests(eq(list), eq(userId), eq(pageable))
        }

        @DisplayName("키워드 검색이 아닌 경우 쿼리 서비스에 요청을 위임한다")
        @Test
        fun `키워드 검색이 아닌 경우 쿼리 서비스에 요청을 위임한다`() {
            //given
            val userId = 1L
            val searchCondition =
                QuestSearchCondition(0, QuestState.PROCEED, null, null, null, null)
            val pageable = Pageable.ofSize(100)

            //when
            val result = questService.searchQuest(userId, searchCondition, pageable)

            //then
            verify(questQueryService, times(1)).getQuestsByCondition(eq(userId), eq(searchCondition), eq(pageable));
            verifyNoInteractions(questIndexService)
        }

    }

    @DisplayName("퀘스트 저장 시")
    @Nested
    inner class QuestSaveTest {

        @DisplayName("요청이 커맨드 서비스에 위임된다")
        @Test
        fun `요청이 커맨드 서비스에 위임된다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val userId = 0L

            //when
            questService.saveQuest(mockDto, userId)

            //then
            verify(questCommandService).saveQuest(eq(mockDto), eq(userId))
        }


        @DisplayName("오픈서치 인덱스에 도큐먼트를 저장한다")
        @Test
        fun `오픈서치 인덱스에 도큐머트를 저장한다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val userId = 0L

            //when
            val saveQuest = questService.saveQuest(mockDto, userId)

            //then
            verify(questIndexService).saveDocument(eq(saveQuest), eq(userId))
        }
    }

    @DisplayName("퀘스트 수정 시")
    @Nested
    inner class QuestUpdateTest {

        @DisplayName("요청이 커맨드 서비스에 위임된다")
        @Test
        fun `요청이 커맨드 서비스에 위임된다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val questId = 0L
            val userId = 0L

            //when
            questService.updateQuest(mockDto, questId, userId)

            //then
            verify(questCommandService).updateQuest(eq(mockDto), eq(questId), eq(userId))
        }


        @DisplayName("오픈서치 인덱스에 도큐먼트를 저장한다")
        @Test
        fun `오픈서치 인덱스에 도큐머트를 저장한다`() {
            //given
            val mockDto = mock<QuestRequest>()
            val questId = 0L

            val userId = 0L

            //when
            val updatedQuest = questService.updateQuest(mockDto, questId, userId)

            //then
            verify(questIndexService).saveDocument(eq(updatedQuest), eq(userId))
        }

    }

    @DisplayName("퀘스트 삭제 시")
    @Nested
    inner class QuestDeleteTest {

        @DisplayName("요청이 커맨드 서비스에 위임된다")
        @Test
        fun `요청이 커맨드 서비스에 위임된다`() {
            //given
            val questId = 0L
            val userId = 0L

            //when
            questService.deleteQuest(questId, userId)

            //then
            verify(questCommandService).deleteQuest(eq(questId), eq(userId))
        }

        @DisplayName("커맨드 서비스의 반환 결과로 엘라스틱서치 업데이트를 요청한다")
        @Test
        fun `커맨드 서비스의 반환 결과로 엘라스틱서치 업데이트를 요청한다`() {
            //given
            val questId = 0L
            val userId = 0L
            val deletedQuest = mock<QuestResponse>()
            doReturn(deletedQuest).`when`(questCommandService).deleteQuest(any(), any())

            //when
            questService.deleteQuest(questId, userId)

            //then
            verify(questIndexService).deleteDocument(eq(deletedQuest))
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    inner class QuestCompleteTest {

        @DisplayName("요청이 커맨드 서비스에 위임된다")
        @Test
        fun `요청이 커맨드 서비스에 위임된다`() {
            //given
            val questId = 0L
            val userId = 0L

            //when
            questService.completeQuest(questId, userId)

            //then
            verify(questCommandService).completeQuest(eq(questId), eq(userId))
        }

        @DisplayName("커맨드 서비스의 반환 결과로 엘라스틱서치 업데이트를 요청한다")
        @Test
        fun `커맨드 서비스의 반환 결과로 엘라스틱서치 업데이트를 요청한다`() {
            //given
            val questId = 0L
            val userId = 0L
            val completedQuest = mock<QuestResponse>()
            doReturn(completedQuest).`when`(questCommandService).completeQuest(any(), any())

            //when
            questService.completeQuest(questId, userId)

            //then
            verify(questIndexService).updateQuestStateOfDocument(eq(completedQuest), eq(userId))
        }
    }

    @DisplayName("퀘스트 포기 시")
    @Nested
    inner class QuestDiscardTest {

        @DisplayName("요청이 커맨드 서비스에 위임된다")
        @Test
        fun `요청이 커맨드 서비스에 위임된다`() {
            //given
            val questId = 0L
            val userId = 0L

            //when
            questService.discardQuest(questId, userId)

            //then
            verify(questCommandService).discardQuest(eq(questId), eq(userId))
        }

        @DisplayName("커맨드 서비스의 반환 결과로 엘라스틱서치 업데이트를 요청한다")
        @Test
        fun `커맨드 서비스의 반환 결과로 엘라스틱서치 업데이트를 요청한다`() {
            //given
            val questId = 0L
            val userId = 0L
            val discardedQuest = mock<QuestResponse>()
            doReturn(discardedQuest).`when`(questCommandService).discardQuest(any(), any())

            //when
            questService.discardQuest(questId, userId)

            //then
            verify(questIndexService).updateQuestStateOfDocument(eq(discardedQuest), eq(userId))
        }
    }

    @DisplayName("세부 퀘스트 상호 작용 시")
    @Nested
    inner class DetailQuestInteractTest {

        @DisplayName("요청이 커맨드 서비스에 위임된다")
        @Test
        fun `요청이 커맨드 서비스에 위임된다`() {
            //given
            val questId = 0L
            val userId = 0L
            val detailQuestId = 1L
            val detailInteractRequest = DetailInteractRequest()

            //when
            questService.interactWithDetailQuest(userId, questId, detailQuestId, detailInteractRequest)

            //then
            verify(questCommandService).interactWithDetailQuest(eq(userId), eq(questId), eq(detailQuestId), eq(detailInteractRequest))
        }
    }


}