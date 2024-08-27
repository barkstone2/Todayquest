package dailyquest.preferencequest.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dailyquest.annotation.WebMvcUnitTest
import dailyquest.common.UserLevelLock
import dailyquest.common.unitTestDefaultConfiguration
import dailyquest.preferencequest.dto.WebPreferenceDetailRequest
import dailyquest.preferencequest.dto.WebPreferenceQuestRequest
import dailyquest.preferencequest.service.PreferenceQuestService
import dailyquest.quest.dto.QuestResponse
import dailyquest.search.service.QuestIndexService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.Answers
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.math.BigInteger
import java.util.function.Supplier
import java.util.stream.Stream

@DisplayName("선호 퀘스트 API 컨트롤러 유닛 테스트")
@WebMvcUnitTest([PreferenceQuestApiController::class])
class PreferenceQuestApiControllerUnitTest {

    @Autowired
    lateinit var mvc: MockMvc

    @MockBean
    lateinit var preferenceQuestService: PreferenceQuestService

    @MockBean
    lateinit var userLevelLock: UserLevelLock

    @MockBean
    lateinit var questIndexService: QuestIndexService

    @MockBean(answer = Answers.RETURNS_SMART_NULLS)
    lateinit var messageSourceAccessor: MessageSourceAccessor

    val om: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule()).registerKotlinModule()

    @DisplayName("전체 선호 퀘스트 목록 조회 시 서비스 메서드가 호출된다")
    @Test
    fun `전체 선호 퀘스트 목록 조회 시 서비스 메서드가 호출된다`() {
        //given
        val uri = ""

        //when
        val result = mvc.get(URI_PREFIX + uri)

        //then
        result.andExpect { status { isOk() } }
        verify(preferenceQuestService, times(1)).getActivePreferenceQuests(any())
    }

    @DisplayName("단일 선호 퀘스트 조회 시")
    @Nested
    inner class TestSingleGetPreferenceQuest {
        @DisplayName("요청 ID가 유효하지 않으면 400 BAD_REQUEST가 반환된다")
        @ArgumentsSource(InvalidLongSources::class)
        @ParameterizedTest(name = "요청 ID가 {0}이면 BAD_REQUEST가 반환한다.")
        fun `요청 ID가 유효하지 않으면 400 BAD_REQUEST가 반환된다`(id: Any) {
            //given
            val uri = "/$id"

            //when
            val result = mvc.get(URI_PREFIX + uri)

            //then
            result.andExpect { status { isBadRequest() } }
        }

        @DisplayName("요청 ID가 유효하면 200이 반환된다")
        @Test
        fun `요청 ID가 유효하면 200이 반환된다`() {
            //given
            val uri = "/1"

            //when
            val result = mvc.get(URI_PREFIX + uri)

            //then
            result.andExpect { status { isOk() } }
        }

        @DisplayName("서비스 메서드가 호출된다")
        @Test
        fun `서비스 메서드가 호출된다`() {
            //given
            val uri = "/1"

            //when
            mvc.get(URI_PREFIX + uri)

            //then
            verify(preferenceQuestService, times(1)).getPreferenceQuest(any(), any())
        }
    }


    @DisplayName("선호 퀘스트 저장 시")
    @Nested
    inner class TestSavePreferenceQuest {

        @ArgumentsSource(InValidPreferenceQuestRequest::class)
        @DisplayName("DTO Validation 실패 시 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{1} BAD_REQUEST가 반환된다.")
        fun `DTO Validation 실패 시 BAD_REQUEST가 반환된다`(requestDto: WebPreferenceQuestRequest, message: String) {
            //given
            val uri = ""

            //when
            val result = mvc.post(URI_PREFIX + uri,
                dsl = {
                    content = om.writeValueAsString(requestDto)
                    contentType = MediaType.APPLICATION_JSON
                    with(csrf())
                }
            )

            //then
            result.andExpect { status { isBadRequest() } }
            verify(preferenceQuestService, times(0)).savePreferenceQuest(any(), any())
        }

        @DisplayName("요청 DTO로 서비스 메서드가 호출된다")
        @Test
        fun `요청 DTO로 서비스 메서드가 호출된다`() {
            //given
            val uri = ""

            val requestDto = WebPreferenceQuestRequest(
                "title",
                "Desc",
                listOf(WebPreferenceDetailRequest("detail-title"))
            )

            //when
            mvc.post(URI_PREFIX + uri,
                dsl = {
                    content = om.writeValueAsString(requestDto)
                    contentType = MediaType.APPLICATION_JSON
                    with(csrf())
                }
            )

            //then
            verify(preferenceQuestService, times(1)).savePreferenceQuest(eq(requestDto), any())
        }
    }

    @DisplayName("선호 퀘스트 수정 시")
    @Nested
    inner class TestUpdatePreferenceQuest {

        @DisplayName("요청 ID가 유효하지 않으면 400 BAD_REQUEST가 반환된다")
        @ArgumentsSource(InvalidLongSources::class)
        @ParameterizedTest(name = "요청 ID가 {0}이면 BAD_REQUEST가 반환한다.")
        fun `요청 ID가 유효하지 않으면 400 BAD_REQUEST가 반환된다`(id: Any) {
            //given
            val uri = "/$id"

            val requestDto = WebPreferenceQuestRequest(
                "title",
                "Desc",
                listOf(WebPreferenceDetailRequest("detail-title"))
            )

            //when
            val result = mvc.patch(URI_PREFIX + uri,
                dsl = {
                    content = om.writeValueAsString(requestDto)
                    contentType = MediaType.APPLICATION_JSON
                    with(csrf())
                }
            )

            //then
            result.andExpect { status { isBadRequest() } }
        }

        @ArgumentsSource(InValidPreferenceQuestRequest::class)
        @DisplayName("DTO Validation 실패 시 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{1} BAD_REQUEST가 반환된다.")
        fun `DTO Validation 실패 시 BAD_REQUEST가 반환된다`(requestDto: WebPreferenceQuestRequest, message: String) {
            //given
            val preferenceQuestId = 1L
            val uri = "/$preferenceQuestId"

            //when
            val result = mvc.patch(URI_PREFIX + uri,
                dsl = {
                    content = om.writeValueAsString(requestDto)
                    contentType = MediaType.APPLICATION_JSON
                    with(csrf())
                }
            )

            //then
            result.andExpect { status { isBadRequest() } }
            verify(preferenceQuestService, times(0)).updatePreferenceQuest(any(), any(), any())
        }

        @DisplayName("ID와 DTO 모두 유효하면 서비스 메서드가 호출된다")
        @Test
        fun `ID와 DTO 모두 유효하면 서비스 메서드가 호출된다`() {
            //given
            val preferenceQuestId = 1L
            val uri = "/$preferenceQuestId"

            val requestDto = WebPreferenceQuestRequest(
                "타이틀",
                "Desc",
                listOf(WebPreferenceDetailRequest("detail-title"))
            )

            //when
            val result = mvc.patch(URI_PREFIX + uri,
                dsl = {
                    content = om.writeValueAsString(requestDto)
                    contentType = MediaType.APPLICATION_JSON
                    with(csrf())
                }
            )

            //then
            result.andExpect { status { isOk() } }
            verify(preferenceQuestService, times(1)).updatePreferenceQuest(eq(requestDto), eq(preferenceQuestId), any())
        }
    }


    @DisplayName("선호 퀘스트 삭제 시")
    @Nested
    inner class TestDeletePreferenceQuest {
        @DisplayName("요청 ID가 유효하지 않으면 400 BAD_REQUEST가 반환된다")
        @ArgumentsSource(InvalidLongSources::class)
        @ParameterizedTest(name = "요청 ID가 {0}이면 BAD_REQUEST가 반환한다.")
        fun `요청 ID가 유효하지 않으면 400 BAD_REQUEST가 반환된다`(id: Any) {
            //given
            val uri = "/$id/delete"

            //when
            val result = mvc.patch(URI_PREFIX + uri,
                dsl = {
                    with(csrf())
                }
            )

            //then
            result.andExpect { status { isBadRequest() } }
        }

        @DisplayName("요청 ID가 유효하면 서비스 메서드가 호출된다")
        @Test
        fun `요청 ID가 유효하면 서비스 메서드가 호출된다`() {
            //given
            val preferenceQuestId = 1L
            val uri = "/$preferenceQuestId/delete"

            //when
            val result = mvc.patch(URI_PREFIX + uri,
                dsl = {
                    with(csrf())
                }
            )

            //then
            result.andExpect { status { isOk() } }
            verify(preferenceQuestService, times(1)).deletePreferenceQuest(eq(preferenceQuestId), any())
        }
    }


    @DisplayName("퀘스트 등록 요청 시")
    @Nested
    inner class TestRegisterQuestByPreferenceQuest {
        @DisplayName("요청 ID가 유효하지 않으면 400 BAD_REQUEST가 반환된다")
        @ArgumentsSource(InvalidLongSources::class)
        @ParameterizedTest(name = "요청 ID가 {0}이면 BAD_REQUEST가 반환한다.")
        fun `요청 ID가 유효하지 않으면 400 BAD_REQUEST가 반환된다`(id: Any) {
            //given
            val uri = "/$id/register"

            //when
            val result = mvc.post(URI_PREFIX + uri,
                dsl = {
                    with(csrf())
                }
            )

            //then
            result.andExpect { status { isBadRequest() } }
        }

        @DisplayName("요청 ID가 유효하면 named lock을 통해 서비스 메서드가 호출된다")
        @Test
        fun `요청 ID가 유효하면 named lock을 통해 서비스 메서드가 호출된다`() {
            //given
            val preferenceQuestId = 1L
            val uri = "/$preferenceQuestId/register"
            doReturn(mock<QuestResponse>(defaultAnswer = Answers.RETURNS_DEEP_STUBS)).`when`(preferenceQuestService).registerQuestByPreferenceQuest(any(), any())
            doAnswer { (it.getArgument(2) as Supplier<*>).get() }.`when`(userLevelLock).executeWithLock(any(), any(), any<Supplier<*>>())

            //when
            val result = mvc.post(URI_PREFIX + uri,
                dsl = {
                    with(csrf())
                }
            )

            //then
            result.andExpect { status { isOk() } }
            verify(userLevelLock, times(1)).executeWithLock(any(), any(), any<Supplier<*>>())
            verify(preferenceQuestService, times(1)).registerQuestByPreferenceQuest(eq(preferenceQuestId), any())
        }

        @DisplayName("서비스 메서드 호출 시 named lock을 통해 호출된다")
        @Test
        fun `서비스 메서드 호출 시 named lock을 통해 호출된다`() {
            //given
            val preferenceQuestId = 1L
            val uri = "/$preferenceQuestId/register"

            doReturn(mock<QuestResponse>(defaultAnswer = Answers.RETURNS_DEEP_STUBS)).`when`(userLevelLock).executeWithLock(any(), any(), any<Supplier<*>>())

            //when
            val result = mvc.post(URI_PREFIX + uri,
                dsl = {
                    with(csrf())
                }
            )

            //then
            result.andExpect { status { isOk() } }
            verify(userLevelLock, times(1)).executeWithLock(any(), any(), any<Supplier<*>>())
        }


        @DisplayName("엘라스틱서치 문서 저장 로직이 호출된다")
        @Test
        fun `엘라스틱서치 문서 저장 로직이 호출된다`() {
            //given
            val questResponse = mock<QuestResponse>(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
            doReturn(questResponse).`when`(userLevelLock).executeWithLock(any(), any(), any<Supplier<*>>())

            //when
            mvc.post("$URI_PREFIX/1/register") {
                unitTestDefaultConfiguration()
            }

            //then
            verify(questIndexService).saveDocument(eq(questResponse), any())
        }
    }

    class InvalidLongSources : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of("0"),
                Arguments.of("text"),
                Arguments.of("-100"),
                Arguments.of(BigInteger("1234567890123456789012345678901234567890")),
            )
        }
    }

    class InValidPreferenceQuestRequest: ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            val validTitle = "title"

            val overRangeTitle = "t".repeat(100)
            val overRangeDesc = "d".repeat(301)

            val validDetail = WebPreferenceDetailRequest(validTitle, targetCount = 3)
            val validDetails = listOf(validDetail)

            val emptyTitleDetails = listOf(WebPreferenceDetailRequest("", targetCount = 3))
            val blankTitleDetails = listOf(WebPreferenceDetailRequest("        ", targetCount = 3))
            val rangeOutTitleDetails = listOf(WebPreferenceDetailRequest(overRangeTitle, targetCount = 3))
            val rangeOutDetails = listOf(WebPreferenceDetailRequest(validTitle, targetCount = 0))
            val countOutDetails = mutableListOf<WebPreferenceDetailRequest>().also { list -> repeat(6) { list.add(validDetail) } }

            return Stream.of(
                Arguments.of(WebPreferenceQuestRequest("", "", validDetails), "제목 길이가 0이면"),
                Arguments.of(WebPreferenceQuestRequest("   ", "", validDetails), "제목에 공백만 있으면"),
                Arguments.of(WebPreferenceQuestRequest(overRangeTitle, "", validDetails), "제목 길이가 초과하면"),
                Arguments.of(WebPreferenceQuestRequest(validTitle, overRangeDesc, validDetails), "설명 길이가 초과하면"),
                Arguments.of(WebPreferenceQuestRequest(validTitle, "", emptyTitleDetails), "세부 제목 길이가 0이면"),
                Arguments.of(WebPreferenceQuestRequest(validTitle, "", blankTitleDetails), "세부 제목에 공백만 있으면"),
                Arguments.of(WebPreferenceQuestRequest(validTitle, "", rangeOutTitleDetails), "세부 제목 길이가 초과하면"),
                Arguments.of(WebPreferenceQuestRequest(validTitle, "", rangeOutDetails), "세부 설명 길이가 초과하면"),
                Arguments.of(WebPreferenceQuestRequest(validTitle, "", countOutDetails), "세부 퀘스트 개수가 초과하면"),
            )
        }
    }

    companion object {
        const val URI_PREFIX = "/api/v1/preference/quests"
    }
}
