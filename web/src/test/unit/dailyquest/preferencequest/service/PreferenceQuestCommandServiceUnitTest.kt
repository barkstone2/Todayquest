package dailyquest.preferencequest.service

import dailyquest.preferencequest.dto.WebPreferenceQuestRequest
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.preferencequest.repository.PreferenceQuestRepository
import dailyquest.user.entity.User
import dailyquest.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.context.support.MessageSourceAccessor
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("선호 퀘스트 커맨드 서비스 유닛 테스트")
class PreferenceQuestCommandServiceUnitTest {

    @InjectMocks
    lateinit var preferenceQuestCommandService: PreferenceQuestCommandService

    @Mock
    lateinit var preferenceQuestRepository: PreferenceQuestRepository

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    lateinit var messageSourceAccessor: MessageSourceAccessor

    @DisplayName("선호 퀘스트 등록 시")
    @Nested
    inner class TestSavePreferenceQuest {
        private val userId = 1L
        private val saveRequest = mock<WebPreferenceQuestRequest>()
        private val savedEntity = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)

        @BeforeEach
        fun init() {
            doReturn(savedEntity).`when`(preferenceQuestRepository).save(anyOrNull())
        }

        @DisplayName("request에 조회한 유저 정보를 사용해 mapToEntity를 호출한다")
        @Test
        fun `request에 조회한 유저 정보를 사용해 mapToEntity를 호출한다`() {
            //given
            //when
            preferenceQuestCommandService.savePreferenceQuest(saveRequest, userId)

            //then
            verify(saveRequest).mapToEntity(eq(userId))
        }

        @DisplayName("mapToEntity 결과를 저장 요청에 사용한다")
        @Test
        fun `mapToEntity 결과를 저장 요청에 사용한다`() {
            //given
            val mappedEntity = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)
            doReturn(mappedEntity).`when`(saveRequest).mapToEntity(anyOrNull())

            //when
            preferenceQuestCommandService.savePreferenceQuest(saveRequest, userId)

            //then
            verify(preferenceQuestRepository).save(eq(mappedEntity))
        }
    }

    @DisplayName("선호 퀘스트 수정 시")
    @Nested
    inner class TestUpdatePreferenceQuest {
        private val updateRequest = mock<WebPreferenceQuestRequest>()
        private val updateTarget = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)

        @DisplayName("요청 DTO로 타겟 엔티티에 업데이트를 요청한다")
        @Test
        fun `요청 DTO로 타겟 엔티티에 업데이트를 요청한다`() {
            //given
            //when
            preferenceQuestCommandService.updatePreferenceQuest(updateRequest, updateTarget)

            //then
            verify(updateTarget).updatePreferenceQuest(eq(updateRequest))
        }
    }

    @DisplayName("선호 퀘스트 삭제 시")
    @Nested
    inner class TestDeletePreferenceQuest {

        @DisplayName("대상 엔티티에 삭제 요청을 위임한다")
        @Test
        fun `대상 엔티티에 삭제 요청을 위임한다`() {
            //given
            val deleteTarget = mock<PreferenceQuest>()

            //when
            preferenceQuestCommandService.deletePreferenceQuest(deleteTarget)

            //then
            verify(deleteTarget).deletePreferenceQuest()
        }
    }

}