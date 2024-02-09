package dailyquest.preferencequest.service

import dailyquest.preferencequest.dto.PreferenceQuestRequest
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.quest.dto.QuestRequest
import dailyquest.quest.dto.QuestResponse
import dailyquest.quest.service.QuestService
import dailyquest.user.entity.UserInfo
import org.junit.jupiter.api.DisplayName
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

    @DisplayName("전체 선호 퀘스트 조회 시 쿼리 서비스에 요청이 위임된다")
    @Test
    fun `전체 선호 퀘스트 조회 시 쿼리 서비스에 요청이 위임된다`() {
        //given
        val userId = 1L

        //when
        preferenceQuestService.getAllPreferenceQuests(userId)

        //then
        verify(preferenceQuestQueryService, times(1)).getAllPreferenceQuests(eq(userId))
    }

    @DisplayName("개별 선호 퀘스트 조회 시 쿼리 서비스에 요청이 위임된다")
    @Test
    fun `개별 선호 퀘스트 조회 시 쿼리 서비스에 요청이 위임된다`() {
        //given
        val preferenceId = 1L
        val userId = 1L

        doReturn(mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)).`when`(preferenceQuestQueryService).getPreferenceQuest(any(), any())

        //when
        preferenceQuestService.getPreferenceQuest(preferenceId, userId)

        //then
        verify(preferenceQuestQueryService, times(1)).getPreferenceQuest(eq(preferenceId), eq(userId))
    }

    @DisplayName("선호 퀘스트 저장 시 커맨드 서비스에 요청이 위임된다")
    @Test
    fun `선호 퀘스트 저장 시 커맨드 서비스에 요청이 위임된다`() {
        //given
        val mockRequest = mock<PreferenceQuestRequest>()
        val userId = 1L

        //when
        preferenceQuestService.savePreferenceQuest(mockRequest, userId)

        //then
        verify(preferenceQuestCommandService, times(1)).savePreferenceQuest(eq(mockRequest), eq(userId))
    }

    @DisplayName("선호 퀘스트 수정 시 커맨드 서비스에 요청이 위임된다")
    @Test
    fun `선호 퀘스트 수정 시 커맨드 서비스에 요청이 위임된다`() {
        //given
        val mockRequest = mock<PreferenceQuestRequest>()
        val preferenceQuestId = 1L
        val userId = 1L

        //when
        preferenceQuestService.updatePreferenceQuest(mockRequest, preferenceQuestId, userId)

        //then
        verify(preferenceQuestCommandService, times(1)).updatePreferenceQuest(eq(mockRequest), eq(preferenceQuestId), eq(userId))
    }

    @DisplayName("선호 퀘스트 삭제 시 커맨드 서비스에 요청이 위임된다")
    @Test
    fun `선호 퀘스트 삭제 시 커맨드 서비스에 요청이 위임된다`() {
        //given
        val preferenceQuestId = 1L
        val userId = 1L

        //when
        preferenceQuestService.deletePreferenceQuest(preferenceQuestId, userId)

        //then
        verify(preferenceQuestCommandService, times(1)).deletePreferenceQuest(eq(preferenceQuestId), eq(userId))
    }

    @DisplayName("퀘스트 등록 요청 시 조회된 선호 퀘스트를 사용해 등록 요청을 위임한다")
    @Test
    fun `퀘스트 등록 요청 시 조회된 선호 퀘스트를 사용해 등록 요청을 위임한다`() {
        //given
        val preferenceQuestId = 1L
        val userId = 1L
        val mockUser = mock<UserInfo>()
        val preferenceQuest = PreferenceQuest("pq-save-title", "pq-save-desc", user = mockUser)
        val questRequest = QuestRequest(preferenceQuest)
        doReturn(preferenceQuest).`when`(preferenceQuestQueryService).getPreferenceQuest(any(), any())
        doReturn(mock<QuestResponse>()).`when`(questService).saveQuest(any(), any())

        //when
        preferenceQuestService.registerQuestByPreferenceQuest(preferenceQuestId, userId)

        //then
        verify(preferenceQuestQueryService, times(1)).getPreferenceQuest(eq(preferenceQuestId), eq(userId))
        verify(questService).saveQuest(eq(questRequest), eq(userId))
    }
}