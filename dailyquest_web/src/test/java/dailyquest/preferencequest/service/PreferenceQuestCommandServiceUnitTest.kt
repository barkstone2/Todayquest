package dailyquest.preferencequest.service

import dailyquest.common.MessageUtil
import dailyquest.preferencequest.dto.PreferenceQuestRequest
import dailyquest.preferencequest.entity.PreferenceQuest
import dailyquest.preferencequest.repository.PreferenceQuestRepository
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
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
        private val userId = 1L
        private val saveRequest = mock<PreferenceQuestRequest>()
        private val principalUser = mock<UserInfo>()
        private val savedEntity = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)

        @BeforeEach
        fun init() {
            doReturn(Optional.of(principalUser)).`when`(userRepository).findById(any())
            doReturn(savedEntity).`when`(preferenceQuestRepository).save(anyOrNull())
        }

        @DisplayName("request에 조회한 유저 정보를 사용해 mapToEntity를 호출한다")
        @Test
        fun `request에 조회한 유저 정보를 사용해 mapToEntity를 호출한다`() {
            //given
            //when
            preferenceQuestCommandService.savePreferenceQuest(saveRequest, userId)

            //then
            verify(saveRequest).mapToEntity(eq(principalUser))
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
        private val updateRequest = mock<PreferenceQuestRequest>()
        private val ownerUser = mock<UserInfo>()
        private val updateTarget = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)

        @DisplayName("updateTarget의 유저 정보를 사용해 요청 정보를 엔티티로 변환한다")
        @Test
        fun `updateTarget의 유저 정보를 사용해 요청 정보를 엔티티로 변환한다`() {
            //given
            doReturn(ownerUser).`when`(updateTarget).user

            //when
            preferenceQuestCommandService.updatePreferenceQuest(updateRequest, updateTarget)

            //then
            verify(updateRequest).mapToEntity(eq(ownerUser))
        }

        @DisplayName("요청 정보를 변환한 엔티티로 타겟 엔티티에 업데이트를 요청한다")
        @Test
        fun `요청 정보를 변환한 엔티티로 타겟 엔티티에 업데이트를 요청한다`() {
            //given
            val requestEntity = mock<PreferenceQuest>(defaultAnswer = Answers.RETURNS_SMART_NULLS)
            doReturn(requestEntity).`when`(updateRequest).mapToEntity(anyOrNull())

            //when
            preferenceQuestCommandService.updatePreferenceQuest(updateRequest, updateTarget)

            //then
            verify(updateTarget).updatePreferenceQuest(eq(requestEntity))
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