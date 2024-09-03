package dailyquest.preferencequest.service

import dailyquest.preferencequest.dto.WebPreferenceQuestRequest
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.dto.WebQuestRequest
import dailyquest.quest.dto.QuestResponse
import dailyquest.quest.service.QuestService
import dailyquest.user.entity.User
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
@DisplayName("선호 퀘스트 서비스 유닛 테스트")
class PreferenceQuestServiceUnitTest {

    @InjectMocks
    lateinit var preferenceQuestService: PreferenceQuestService

    @Mock
    lateinit var preferenceQuestCommandService: PreferenceQuestCommandService

    @Mock
    lateinit var preferenceQuestQueryService: PreferenceQuestQueryService

    @Mock
    lateinit var questService: QuestService

    @DisplayName("개별 선호 퀘스트 조회 시 쿼리 서비스에 요청이 위임된다")
    @Test
    fun `개별 선호 퀘스트 조회 시 쿼리 서비스에 요청이 위임된다`() {
        //given
        val preferenceId = 1L
        val userId = 1L
        val mockEntity = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)
        doReturn(mockEntity).`when`(preferenceQuestQueryService).getPreferenceQuest(any(), any())

        //when
        preferenceQuestService.getPreferenceQuest(preferenceId, userId)

        //then
        verify(preferenceQuestQueryService, times(1)).getPreferenceQuest(eq(preferenceId), eq(userId))
    }

    @DisplayName("선호 퀘스트 저장 시 커맨드 서비스에 요청이 위임된다")
    @Test
    fun `선호 퀘스트 저장 시 커맨드 서비스에 요청이 위임된다`() {
        //given
        val mockRequest = mock<WebPreferenceQuestRequest>()
        val userId = 1L

        //when
        preferenceQuestService.savePreferenceQuest(mockRequest, userId)

        //then
        verify(preferenceQuestCommandService, times(1)).savePreferenceQuest(eq(mockRequest), eq(userId))
    }

    @DisplayName("선호 퀘스트 수정 시")
    @Nested
    inner class TestUpdatePreferenceQuest {

        @DisplayName("쿼리 서비스에서 엔티티를 조회한다")
        @Test
        fun `쿼리 서비스에서 엔티티를 조회한다`() {
            //given
            val mockRequest = mock<WebPreferenceQuestRequest>()
            val preferenceQuestId = 1L
            val userId = 1L

            //when
            preferenceQuestService.updatePreferenceQuest(mockRequest, preferenceQuestId, userId)

            //then
            verify(preferenceQuestQueryService, times(1)).getPreferenceQuest(eq(preferenceQuestId), eq(userId))
        }

        @DisplayName("커맨드 서비스에 업데이트 요청이 위임된다")
        @Test
        fun `커맨드 서비스에 업데이트 요청이 위임된다`() {
            //given
            val preferenceQuestId = 1L
            val userId = 1L
            val mockRequest = mock<WebPreferenceQuestRequest>()
            val updateTarget = mock<PreferenceQuest>()
            doReturn(updateTarget).`when`(preferenceQuestQueryService).getPreferenceQuest(any(), any())

            //when
            preferenceQuestService.updatePreferenceQuest(mockRequest, preferenceQuestId, userId)

            //then
            verify(preferenceQuestCommandService, times(1)).updatePreferenceQuest(eq(mockRequest), eq(updateTarget))
        }
    }

    @DisplayName("선호 퀘스트 삭제 시")
    @Nested
    inner class TestDeletePreferenceQuest {

        @DisplayName("쿼리 서비스에서 엔티티를 조회한다")
        @Test
        fun `쿼리 서비스에서 엔티티를 조회한다`() {
            //given
            val preferenceQuestId = 1L
            val userId = 1L

            //when
            preferenceQuestService.deletePreferenceQuest(preferenceQuestId, userId)

            //then
            verify(preferenceQuestQueryService, times(1)).getPreferenceQuest(eq(preferenceQuestId), eq(userId))
        }


        @DisplayName("커맨드 서비스에 삭제 요청이 위임된다")
        @Test
        fun `커맨드 서비스에 삭제 요청이 위임된다`() {
            //given
            val preferenceQuestId = 1L
            val userId = 1L
            val deleteTarget = mock<PreferenceQuest>()

            doReturn(deleteTarget).`when`(preferenceQuestQueryService).getPreferenceQuest(eq(preferenceQuestId), eq(userId))

            //when
            preferenceQuestService.deletePreferenceQuest(preferenceQuestId, userId)

            //then
            verify(preferenceQuestCommandService, times(1)).deletePreferenceQuest(eq(deleteTarget))
        }
    }




    @DisplayName("퀘스트 등록 요청 시 조회된 선호 퀘스트를 사용해 등록 요청을 위임한다")
    @Test
    fun `퀘스트 등록 요청 시 조회된 선호 퀘스트를 사용해 등록 요청을 위임한다`() {
        //given
        val preferenceQuestId = 1L
        val userId = 1L
        val mockUser = mock<User>()
        val preferenceQuest = PreferenceQuest.of("pq-save-title", "pq-save-desc", userId = mockUser.id)
        val questRequest = WebQuestRequest.from(preferenceQuest)
        doReturn(preferenceQuest).`when`(preferenceQuestQueryService).getPreferenceQuest(any(), any())
        doReturn(mock<QuestResponse>()).`when`(questService).saveQuest(any(), any())

        //when
        preferenceQuestService.registerQuestByPreferenceQuest(preferenceQuestId, userId)

        //then
        verify(preferenceQuestQueryService, times(1)).getPreferenceQuest(eq(preferenceQuestId), eq(userId))
        verify(questService).saveQuest(eq(questRequest), eq(userId))
    }
}