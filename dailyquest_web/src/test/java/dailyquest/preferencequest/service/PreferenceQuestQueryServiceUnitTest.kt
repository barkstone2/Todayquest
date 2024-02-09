package dailyquest.preferencequest.service

import dailyquest.common.MessageUtil
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.preferencequest.repository.PreferenceQuestRepository
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
@DisplayName("선호 퀘스트 쿼리 서비스 유닛 테스트")
class PreferenceQuestQueryServiceUnitTest {
    @InjectMocks
    lateinit var preferenceQuestQueryService: PreferenceQuestQueryService

    @Mock
    lateinit var preferenceQuestRepository: PreferenceQuestRepository

    private lateinit var messageUtil: MockedStatic<MessageUtil>

    @BeforeEach
    fun beforeEach() {
        messageUtil = Mockito.mockStatic(MessageUtil::class.java, Answers.RETURNS_SMART_NULLS)
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }

    @DisplayName("getAllPreferenceQuests 호출 시 findAllWithUsedCount 메서드를 호출한다")
    @Test
    fun `getAllPreferenceQuests 호출 시 findAllWithUsedCount 메서드를 호출한다`() {
        //given
        val userId = 1L

        //when
        preferenceQuestQueryService.getAllPreferenceQuests(userId)

        //then
        verify(preferenceQuestRepository, times(1)).findAllWithUsedCount(eq(userId))
    }

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
            val lambda = { preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId) }

            //then
            assertThatExceptionOfType(EntityNotFoundException::class.java).isThrownBy { lambda.invoke() }
        }

        @DisplayName("리포지토리 반환 값이 null이 아니면 정상 반환된다")
        @Test
        fun `리포지토리 반환 값이 null이 아니면 정상 반환된다`() {
            //given
            val userId = 1L
            val preferenceQuestId = 1L

            doReturn(mock<PreferenceQuest>()).`when`(preferenceQuestRepository).findByIdAndUserIdAndDeletedDateIsNull(any(), any())

            //when
            val lambda = { preferenceQuestQueryService.getPreferenceQuest(preferenceQuestId, userId) }

            //then
            assertThatNoException().isThrownBy { lambda.invoke() }
        }
    }

}