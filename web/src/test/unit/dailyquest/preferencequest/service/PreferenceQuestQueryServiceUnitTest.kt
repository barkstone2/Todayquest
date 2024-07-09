package dailyquest.preferencequest.service

import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.preferencequest.repository.PreferenceQuestRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.context.support.MessageSourceAccessor

@ExtendWith(MockitoExtension::class)
@DisplayName("선호 퀘스트 쿼리 서비스 유닛 테스트")
class PreferenceQuestQueryServiceUnitTest {
    @InjectMocks
    lateinit var preferenceQuestQueryService: PreferenceQuestQueryService

    @Mock
    lateinit var preferenceQuestRepository: PreferenceQuestRepository

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    lateinit var messageSourceAccessor: MessageSourceAccessor

    @DisplayName("getPreferenceQuest 호출 시")
    @Nested
    inner class TestGetPreferenceQuest {

        @DisplayName("리포지토리 반환 값이 null이면 EntityNotFound 예외를 던진다")
        @Test
        fun `리포지토리 반환 값이 null이면 EntityNotFound 예외를 던진다`() {
            //given
            val userId = 1L
            val preferenceQuestId = 1L

            doReturn(null).`when`(preferenceQuestRepository).findByIdAndUserIdAndDeletedDateIsNull(any(), any())

            //when
            //then
            assertThrows<EntityNotFoundException> { preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId) }
        }

        @DisplayName("리포지토리 반환 값이 null이 아니면 정상 반환된다")
        @Test
        fun `리포지토리 반환 값이 null이 아니면 정상 반환된다`() {
            //given
            val userId = 1L
            val preferenceQuestId = 1L

            doReturn(mock<PreferenceQuest>()).`when`(preferenceQuestRepository).findByIdAndUserIdAndDeletedDateIsNull(any(), any())

            //when
            //then
            assertDoesNotThrow { preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId) }
        }
    }

}