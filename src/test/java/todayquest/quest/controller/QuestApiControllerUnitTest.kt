package todayquest.quest.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.*
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import org.mockito.ArgumentMatchers.*
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import todayquest.annotation.WithCustomMockUser
import todayquest.common.MessageUtil
import todayquest.common.RestPage
import todayquest.common.UserLevelLock
import todayquest.config.SecurityConfig
import todayquest.jwt.JwtAuthorizationFilter
import todayquest.quest.dto.*
import todayquest.quest.entity.DetailQuestType
import todayquest.quest.service.DetailQuestService
import todayquest.quest.service.QuestService
import java.math.BigInteger
import java.util.function.Supplier
import java.util.stream.Stream


@DisplayName("퀘스트 API 컨트롤러 유닛 테스트")
@WithCustomMockUser
@WebMvcTest(controllers = [QuestApiController::class],
    excludeFilters = [
        Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class, JwtAuthorizationFilter::class]
        )
    ]
)
class QuestApiControllerUnitTest {

    companion object {
        const val URI_PREFIX = "/api/quests"

    }

    class InvalidIntegerSources : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of("text"),
                Arguments.of("-100"),
                Arguments.of(Long.MAX_VALUE),
            )
        }
    }

    class InvalidLongSources : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
            return Stream.of(
                Arguments.of("0"),
                Arguments.of("text"),
                Arguments.of("-100"),
                Arguments.of(BigInteger("1234567890123456789012345678901234567890"))
            )
        }
    }

    class ValidQuestRequest: ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return Stream.of(
                Arguments.of(QuestRequest("title", "desc")),
            )
        }
    }

    class InValidQuestRequest: ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            val details = mutableListOf(DetailRequest("", DetailQuestType.COUNT, 0))
            return Stream.of(
                Arguments.of(QuestRequest("", "", details)),
                Arguments.of(null),
            )
        }
    }

    @Autowired
    lateinit var mvc: MockMvc

    @MockBean
    lateinit var questService: QuestService

    @MockBean
    lateinit var detailService: DetailQuestService

    @MockBean
    lateinit var userLevelLock: UserLevelLock

    private lateinit var messageUtil: MockedStatic<MessageUtil>

    lateinit var questResponse: QuestResponse
    lateinit var detailResponse: DetailResponse
    val om: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())

    lateinit var questList: RestPage<QuestResponse>

    @BeforeEach
    fun init() {

        questResponse = QuestResponse(title = "title1")

        val quest2 = questResponse.copy(title = "title2")
        val list = listOf(questResponse, quest2)

        detailResponse = DetailResponse(title = "title")

        questList = RestPage(list, 0, 10, list.size.toLong())
        messageUtil = mockStatic(MessageUtil::class.java)
        `when`(MessageUtil.getMessage(anyString())).thenReturn("")
        `when`(MessageUtil.getMessage(anyString(), any())).thenReturn("")
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }

    @DisplayName("퀘스트 목록 조회 시")
    @Nested
    inner class QuestListTest {

        @DisplayName("page 번호가 없으면 200 OK가 반환된다")
        @Test
        fun `page 번호가 없으면 200 OK가 반환된다`() {
            //given

            //when
            val result = mvc.perform(
                get(URI_PREFIX)
            )

            //then
            result
                .andExpect(status().isOk)
        }

        @DisplayName("page 번호가 0보다 큰 int 범위의 숫자면 200 OK가 반환된다")
        @ValueSource(strings = ["0", "1", "10", "500"])
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다.")
        fun `page 번호가 0보다 큰 숫자면 200 OK가 반환된다`(page: String) {
            //given
            `when`(questService.getQuestList(any(), any(), any()))
                .thenReturn(questList)

            //when
            val result = mvc.perform(
                get(URI_PREFIX)
                    .queryParam("page", page)
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data.numberOfElements").value(questList.numberOfElements))
                .andExpect(jsonPath("$.data.content[0].title").value(questList.content[0].title))
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @DisplayName("page 번호가 0보다 큰 int 범위의 숫자면 아니면 400 BAD_REQUEST가 반환된다")
        @ArgumentsSource(InvalidIntegerSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 400을 반환한다")
        fun `page 번호가 숫자가 아니면 400 BAD_REQUEST가 반환된다`(page: Any) {
            //given

            //when
            val result = mvc.perform(
                get(URI_PREFIX)
                    .queryParam("page", page.toString())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").exists())
                .andExpect(jsonPath("$.errorResponse.errors").exists())
        }



    }


    @DisplayName("퀘스트 조회 시")
    @Nested
    inner class QuestGetTest {

        @DisplayName("ID 값이 유효하면 200 OK가 반환된다")
        @ValueSource(strings = ["1", "10", "500"])
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `ID 값이 유효하면 200 OK가 반환된다`(questId: Long) {
            //given
            `when`(questService.getQuestInfo(any(), any())).thenReturn(questResponse)

            //when
            val result = mvc.perform(
                get("$URI_PREFIX/$questId")
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data.title").value(questResponse.title))
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }


        @DisplayName("ID 값이 유효하지 않으면 400 BAD_REQUEST가 반환된다")
        @ArgumentsSource(InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 400을 반환한다")
        fun `ID 값이 유효하지 않으면 400 BAD_REQUEST가 반환된다`(questId: Any) {
            //given

            //when
            val result = mvc.perform(
                get("$URI_PREFIX/$questId")
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
        }

    }


    @DisplayName("퀘스트 등록 시")
    @Nested
    inner class QuestSaveTest {

        @ArgumentsSource(ValidQuestRequest::class)
        @DisplayName("DTO 필수 값이 모두 있다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `필수 값이 모두 있다면 200 OK가 반환된다`(questRequest: QuestRequest) {
            //given
            `when`(userLevelLock.executeWithLock(any(), anyInt(), any(Supplier::class.java)))
                .thenReturn(questResponse)

            //when
            val result = mvc.perform(
                post(URI_PREFIX)
                    .content(om.writeValueAsBytes(questRequest))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data.title").value(questResponse.title))
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @ArgumentsSource(InValidQuestRequest::class)
        @DisplayName("DTO 필수 값이 없다면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400이 반환한다")
        fun `필수 값이 없다면 400 BAD_REQUEST가 반환된다`(questRequest: QuestRequest?) {
            //given
            `when`(userLevelLock.executeWithLock(any(), anyInt(), any(Supplier::class.java)))
                .thenReturn(questResponse)

            //when
            val result = mvc.perform(
                post(URI_PREFIX)
                    .content(om.writeValueAsBytes(questRequest))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .characterEncoding("UTF-8")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").exists())
                .andExpect(jsonPath("$.errorResponse.errors").exists())
                .andReturn()
        }

    }

    @DisplayName("퀘스트 수정 시")
    @Nested
    inner class QuestUpdateTest {

        @ArgumentsSource(ValidQuestRequest::class)
        @DisplayName("DTO 필수 값이 모두 있다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `필수 값이 모두 있다면 200 OK가 반환된다`(questRequest: QuestRequest) {
            //given
            val questId = 1L
            `when`(questService.updateQuest(any(), any(), any()))
                .thenReturn(questResponse)

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId")
                    .content(om.writeValueAsBytes(questRequest))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data.title").value(questResponse.title))
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @ArgumentsSource(InValidQuestRequest::class)
        @DisplayName("DTO 필수 값이 없다면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400이 반환한다")
        fun `필수 값이 없다면 400 BAD_REQUEST가 반환된다`(questRequest: QuestRequest?) {
            //given
            val questId = 1L

            `when`(questService.updateQuest(any(), any(), any()))
                .thenReturn(questResponse)

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId")
                    .content(om.writeValueAsBytes(questRequest))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .characterEncoding("UTF-8")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").exists())
                .andExpect(jsonPath("$.errorResponse.errors").exists())
                .andReturn()
        }

        @ArgumentsSource(InvalidLongSources::class)
        @DisplayName("PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400이 반환한다")
        fun `path variable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다`(questId: Any) {
            //given
            val questRequest = QuestRequest("title", "desc")
            `when`(questService.updateQuest(any(), any(), any()))
                .thenReturn(questResponse)

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId")
                    .content(om.writeValueAsBytes(questRequest))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .characterEncoding("UTF-8")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").exists())
                .andExpect(jsonPath("$.errorResponse.errors").exists())
                .andReturn()
        }
    }


    @DisplayName("퀘스트 삭제 시")
    @Nested
    inner class QuestDeleteTest {

        @ValueSource(longs = [1, 5, 100, 5000])
        @DisplayName("PathVariable 값이 올바르다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `PathVariable 값이 올바르다면 200 OK가 반환된다`(questId: Long) {
            //given

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/delete")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @ArgumentsSource(InvalidLongSources::class)
        @DisplayName("PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400이 반환한다")
        fun `PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다`(questId: Any) {
            //given

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/delete")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .characterEncoding("UTF-8")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").exists())
                .andExpect(jsonPath("$.errorResponse.errors").exists())
                .andReturn()
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    inner class QuestCompleteTest {
        @ValueSource(longs = [1, 5, 100, 5000])
        @DisplayName("PathVariable 값이 올바르다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `PathVariable 값이 올바르다면 200 OK가 반환된다`(questId: Long) {
            //given

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/complete")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @ArgumentsSource(InvalidLongSources::class)
        @DisplayName("PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400이 반환한다")
        fun `PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다`(questId: Any) {
            //given

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/complete")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .characterEncoding("UTF-8")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").exists())
                .andExpect(jsonPath("$.errorResponse.errors").exists())
                .andReturn()
        }
    }

    @DisplayName("퀘스트 포기 시")
    @Nested
    inner class QuestDiscardTest {

        @ValueSource(longs = [1, 5, 100, 5000])
        @DisplayName("PathVariable 값이 올바르다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `PathVariable 값이 올바르다면 200 OK가 반환된다`(questId: Long) {
            //given

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/discard")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @ArgumentsSource(InvalidLongSources::class)
        @DisplayName("PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400이 반환한다")
        fun `PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다`(questId: Any) {
            //given

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/discard")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .characterEncoding("UTF-8")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").exists())
                .andExpect(jsonPath("$.errorResponse.errors").exists())
                .andReturn()
        }
    }

    @DisplayName("세부 퀘스트 상호작용 시")
    @Nested
    inner class DetailInteractTest {

        @ValueSource(longs = [1, 5, 100, 5000])
        @DisplayName("PathVariable 값이 올바르다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `PathVariable 값이 올바르다면 200 OK가 반환된다`(questId: Long) {
            //given
            `when`(detailService.interact(anyLong(), anyLong(), anyLong(), any()))
                .thenReturn(detailResponse)

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/details/$questId")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @ArgumentsSource(InvalidLongSources::class)
        @DisplayName("PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400이 반환한다")
        fun `PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다`(questId: Any) {
            //given

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/discard")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .characterEncoding("UTF-8")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").exists())
                .andExpect(jsonPath("$.errorResponse.errors").exists())
                .andReturn()
        }

        @ValueSource(shorts = [1, 3, 5, 255])
        @DisplayName("RequestBody 값이 유효하다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `RequestBody 값이 유효하다면 200 OK가 반환된다`(count: Short) {
            //given
            val questId = 1L
            `when`(detailService.interact(anyLong(), anyLong(), anyLong(), any()))
                .thenReturn(detailResponse)

            val interactRequest = DetailInteractRequest(count)

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/details/$questId")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(om.writeValueAsBytes(interactRequest))
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @DisplayName("RequestBody를 생략하면 200 OK가 반환된다")
        @Test
        fun `RequestBody를 생략하면 200 OK가 반환된다`() {
            //given
            val questId = 1L
            `when`(detailService.interact(anyLong(), anyLong(), anyLong(), any()))
                .thenReturn(detailResponse)

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/details/$questId")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @NullSource
        @ValueSource(strings = ["", " ", "0", "-1", "300"])
        @DisplayName("RequestBody 값이 유효하지 않다면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `RequestBody 값이 유효하지 않다면 400 BAD_REQUEST가 반환된다`(count: String?) {
            //given
            val questId = 1L
            `when`(detailService.interact(anyLong(), anyLong(), anyLong(), any()))
                .thenReturn(detailResponse)

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/details/$questId")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(om.writeValueAsBytes("{\"count\":\"$count\"}"))
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse").exists())
        }
    }


}