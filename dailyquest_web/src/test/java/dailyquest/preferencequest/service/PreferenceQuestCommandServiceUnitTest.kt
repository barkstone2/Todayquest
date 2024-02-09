package dailyquest.preferencequest.service

import dailyquest.common.MessageUtil
import dailyquest.preferencequest.dto.PreferenceDetailRequest
import dailyquest.preferencequest.dto.PreferenceQuestRequest
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.preferencequest.repository.PreferenceQuestRepository
import dailyquest.quest.entity.DetailQuestType
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("선호 퀘스트 커맨드 서비스 유닛 테스트")
class PreferenceQuestCommandServiceUnitTest {

    @InjectMocks
    lateinit var preferenceQuestCommandService: PreferenceQuestCommandService

    @Mock
    lateinit var preferenceQuestRepository: PreferenceQuestRepository

    @Mock
    lateinit var userRepository: UserRepository

    private lateinit var messageUtil: MockedStatic<MessageUtil>

    @BeforeEach
    fun beforeEach() {
        messageUtil = Mockito.mockStatic(MessageUtil::class.java, Answers.RETURNS_SMART_NULLS)
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }

    @DisplayName("선호 퀘스트 등록 시")
    @Nested
    inner class TestSavePreferenceQuest {

        @DisplayName("데드라인 확인 메서드가 호출된다")
        @Test
        fun `데드라인 확인 메서드가 호출된다`() {
            //given
            val userId = 1L
            val mockRequest = mock<PreferenceQuestRequest>()

            val mockUser = mock<UserInfo>()
            doReturn(Optional.of(mockUser)).`when`(userRepository).findById(any())

            val mockEntity = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)
            doReturn(mockEntity).`when`(preferenceQuestRepository).save(anyOrNull())

            //when
            preferenceQuestCommandService.savePreferenceQuest(mockRequest, userId)

            //then
            verify(mockRequest, times(1)).checkRangeOfDeadLine()
            verify(preferenceQuestRepository, times(1)).save(anyOrNull())
        }

        @DisplayName("조회한 유저 참조를 담아 생성한 엔티티를 등록한다")
        @Test
        fun `조회한 유저 참조를 담아 생성한 엔티티를 등록한다`() {
            //given
            val userId = 1L
            val mockRequest = mock<PreferenceQuestRequest>()

            val mockUser = mock<UserInfo>()
            doReturn(Optional.of(mockUser)).`when`(userRepository).findById(any())

            val mockEntity = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)
            doReturn(mockEntity).`when`(mockRequest).mapToEntity(any())
            doReturn(mockEntity).`when`(preferenceQuestRepository).save(any())

            //when
            preferenceQuestCommandService.savePreferenceQuest(mockRequest, userId)

            //then
            verify(userRepository, times(1)).findById(eq(userId))
            verify(mockRequest, times(1)).mapToEntity(eq(mockUser))
            verify(preferenceQuestRepository, times(1)).save(eq(mockEntity))
        }
    }

    @DisplayName("선호 퀘스트 수정 시")
    @Nested
    inner class TestUpdatePreferenceQuest {
        @DisplayName("수정하려는 엔티티 조회에 실패 시 EntityNotFound 예외가 발생한다")
        @Test
        fun `수정하려는 엔티티 조회에 실패 시 EntityNotFound 예외가 발생한다`() {
            //given
            val userId = 1L
            val preferenceQuestId = 1L

            doReturn(null).`when`(preferenceQuestRepository).findByIdAndUserIdAndDeletedDateIsNull(any(), any())

            //when
            val lambda = { preferenceQuestCommandService.updatePreferenceQuest(mock<PreferenceQuestRequest>(), preferenceQuestId, userId) }

            //then
            assertThatExceptionOfType(EntityNotFoundException::class.java).isThrownBy { lambda.invoke() }
        }

        @DisplayName("수정하려는 엔티티 조회 성공 시 엔티티에게 업데이트 요청을 위임한다")
        @Test
        fun `수정하려는 엔티티 조회 성공 시 엔티티에게 업데이트 요청을 위임한다`() {
            //given
            val userId = 1L
            val preferenceQuestId = 1L
            val request = PreferenceQuestRequest(
                title = "title",
                description = "desc",
                details = listOf(PreferenceDetailRequest(title = "detail", type = DetailQuestType.COUNT, targetCount = 15)),
                deadLine = LocalDateTime.of(2022, 10, 10, 12, 30, 0)
            )

            val mockEntity = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)
            doReturn(mockEntity).`when`(preferenceQuestRepository).findByIdAndUserIdAndDeletedDateIsNull(any(), any())

            //when
            preferenceQuestCommandService.updatePreferenceQuest(request, preferenceQuestId, userId)

            //then
            verify(mockEntity, times(1)).updatePreferenceQuest(
                eq(request.title),
                eq(request.description),
                eq(request.deadLine),
                eq(request.details.map { it.mapToEntity(mockEntity) })
            )
        }
    }

    @DisplayName("선호 퀘스트 삭제 시")
    @Nested
    inner class TestDeletePreferenceQuest {

        @DisplayName("수정하려는 엔티티 조회에 실패하면 EntityNotFound 예외를 던진다")
        @Test
        fun `수정하려는 엔티티 조회에 실패하면 EntityNotFound 예외를 던진다`() {
            //given
            val userId = 1L
            val preferenceQuestId = 1L

            doReturn(null).`when`(preferenceQuestRepository).findByIdAndUserIdAndDeletedDateIsNull(any(), any())

            //when
            val lambda = { preferenceQuestCommandService.deletePreferenceQuest(preferenceQuestId, userId) }

            //then
            assertThatExceptionOfType(EntityNotFoundException::class.java).isThrownBy { lambda.invoke() }
        }

        @DisplayName("수정하려는 엔티티 조회 성공 시 엔티티에게 삭제 처리를 위임한다")
        @Test
        fun `수정하려는 엔티티 조회 성공 시 엔티티에게 삭제 처리를 위임한다`() {
            //given
            val userId = 1L
            val preferenceQuestId = 1L

            val mockEntity = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)
            doReturn(mockEntity).`when`(preferenceQuestRepository).findByIdAndUserIdAndDeletedDateIsNull(any(), any())

            //when
            preferenceQuestCommandService.deletePreferenceQuest(preferenceQuestId, userId)

            //then
            verify(mockEntity, times(1)).deletePreferenceQuest()
        }
    }

}