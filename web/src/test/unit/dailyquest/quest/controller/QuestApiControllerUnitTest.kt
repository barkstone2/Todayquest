package dailyquest.quest.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dailyquest.annotation.WebMvcUnitTest
import dailyquest.common.UserLevelLock
import dailyquest.common.unitTestDefaultConfiguration
import dailyquest.quest.dto.*
import dailyquest.quest.entity.DetailQuestType
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import dailyquest.quest.service.QuestService
import dailyquest.redis.service.RedisService
import dailyquest.search.service.QuestIndexService
import dailyquest.user.dto.UserPrincipal
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Answers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigInteger
import java.time.LocalDateTime
import java.util.function.Supplier
import java.util.stream.Stream

@DisplayName("퀘스트 API 컨트롤러 유닛 테스트")
@WebMvcUnitTest([QuestApiController::class])
class QuestApiControllerUnitTest {
    @Autowired
    lateinit var mvc: MockMvc

    @MockBean
    lateinit var questService: QuestService

    @MockBean
    lateinit var userLevelLock: UserLevelLock

    @MockBean
    lateinit var questIndexService: QuestIndexService

    @MockBean
    lateinit var redisService: RedisService

    @SpyBean
    lateinit var questApiController: QuestApiController
    @MockBean(answer = Answers.RETURNS_SMART_NULLS)
    lateinit var messageSourceAccessor: MessageSourceAccessor

    lateinit var questResponse: QuestResponse
    lateinit var detailResponse: DetailResponse
    val om: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())

    lateinit var currentQuests: List<QuestResponse>
    lateinit var searchedQuests: Page<QuestResponse>

    @BeforeEach
    fun init() {

        questResponse = QuestResponse(title = "title1")

        val quest2 = questResponse.copy(title = "title2")
        currentQuests = listOf(questResponse, quest2)
        searchedQuests = PageImpl(currentQuests)
        doReturn(searchedQuests).`when`(questService).searchQuest(any(), any(), any())
        detailResponse = DetailResponse(title = "title")
    }

    @DisplayName("퀘스트 목록 조회 시")
    @Nested
    inner class QuestListTest {

        @DisplayName("상태 조건 파라미터가 제대로 처리된다")
        @Test
        fun `상태 조건 파라미터가 제대로 처리된다`() {
            //given
            val page = "1"
            val state = QuestState.PROCEED.name

            `when`(questService.getCurrentQuests(any(), any())).thenReturn(currentQuests)

            //when
            val result = mvc.perform(
                get(URI_PREFIX)
                    .queryParam("page", page)
                    .queryParam("state", state)
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data.size()").value(currentQuests.size))
                .andExpect(jsonPath("$.data[0].title").value(currentQuests[0].title))
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

    }

    @DisplayName("퀘스트 검색 시")
    @Nested
    inner class QuestSearchTest {

        @DisplayName("page 번호가 없으면 200 OK가 반환된다")
        @Test
        fun `page 번호가 없으면 200 OK가 반환된다`() {
            //given
            val url = "$URI_PREFIX/search"

            //when
            val result = mvc.perform(
                get(url)
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
            val url = "$URI_PREFIX/search"

            `when`(questService.searchQuest(any(), any(), any()))
                .thenReturn(searchedQuests)

            //when
            val result = mvc.perform(
                get(url)
                    .queryParam("page", page)
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @DisplayName("page 번호가 0보다 큰 int 범위의 숫자가 아니면 400 BAD_REQUEST가 반환된다")
        @ArgumentsSource(InvalidIntegerSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 400을 반환한다")
        fun `page 번호가 숫자가 아니면 400 BAD_REQUEST가 반환된다`(page: Any) {
            //given
            val url = "$URI_PREFIX/search"

            //when
            val result = mvc.perform(
                get(url)
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

        @DisplayName("검색 조건 파라미터가 제대로 처리된다")
        @Test
        fun `검색 조건 파라미터가 제대로 처리된다`() {
            //given
            val url = "$URI_PREFIX/search"
            val page = "1"
            val state = QuestState.PROCEED.name
            val keywordType = QuestSearchKeywordType.ALL.name
            val keyword = "keyword"
            val startDate = "2021-12-12"
            val endDate = "2022-12-12"

            `when`(questService.searchQuest(any(), any())).thenReturn(searchedQuests)

            //when
            val result = mvc.perform(
                get(url)
                    .queryParam("page", page)
                    .queryParam("state", state)
                    .queryParam("keywordType", keywordType)
                    .queryParam("keyword", keyword)
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate)
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.data.content.size()").value(currentQuests.size))
                .andExpect(jsonPath("$.data.content[0].title").value(currentQuests[0].title))
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
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
        private val now = LocalDateTime.of(2020, 12, 12, 15, 0)
        private val nextReset = now.plusDays(1).withHour(6)

        @BeforeEach
        fun init() {
            mockkStatic(LocalDateTime::class)
            every { LocalDateTime.now() } returns now
            `when`(userLevelLock.executeWithLock(any(), anyInt(), any(Supplier::class.java)))
                .thenReturn(questResponse)
        }

        @AfterEach
        fun destroy() {
            unmockkStatic(LocalDateTime::class)
        }

        @ArgumentsSource(ValidQuestRequest::class)
        @DisplayName("DTO 필수 값이 모두 있다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `필수 값이 모두 있다면 200 OK가 반환된다`(questRequest: WebQuestRequest) {
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
        fun `필수 값이 없다면 400 BAD_REQUEST가 반환된다`(questRequest: WebQuestRequest?) {
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

        @DisplayName("현재 시간이 유저의 코어타임이라면 요청 DTO가 main 타입으로 변경된다")
        @Test
        fun `현재 시간이 유저의 코어타임이라면 요청 DTO가 main 타입으로 변경된다`() {
            //given
            val principal = SecurityContextHolder.getDeferredContext().get().authentication.principal as UserPrincipal
            doReturn(true).`when`(principal).isNowCoreTime()
            val questRequest = WebQuestRequest("t", "d")

            //when
            mvc.post(URI_PREFIX) {
                content = om.writeValueAsString(questRequest)
                unitTestDefaultConfiguration()
            }

            //then
            val argumentCaptor = argumentCaptor<WebQuestRequest>()
            verify(questApiController).saveQuest(argumentCaptor.capture(), any())
            assertThat(argumentCaptor.firstValue.type).isEqualTo(QuestType.MAIN)
        }

        @DisplayName("현재 시간이 유저의 코어타임이 아니면 요청 DTO가 서브 타입으로 유지된다")
        @Test
        fun `현재 시간이 유저의 코어타임이 아니면 요청 DTO가 서브 타입으로 유지된다`() {
            //given
            val principal = SecurityContextHolder.getDeferredContext().get().authentication.principal as UserPrincipal
            doReturn(false).`when`(principal).isNowCoreTime()
            val questRequest = WebQuestRequest("t", "d")

            //when
            mvc.post(URI_PREFIX) {
                content = om.writeValueAsString(questRequest)
                unitTestDefaultConfiguration()
            }

            //then
            val argumentCaptor = argumentCaptor<WebQuestRequest>()
            verify(questApiController).saveQuest(argumentCaptor.capture(), any())
            assertThat(argumentCaptor.firstValue.type).isEqualTo(QuestType.SUB)
        }

        @DisplayName("다른 값이 모두 유효하고 데드라인이 null이면 200이 반환된다")
        @Test
        fun `다른 값이 모두 유효하고 데드라인이 null이면 200이 반환된다`() {
            //given
            val deadLine = null
            val questRequest = WebQuestRequest("t", "d", deadLine = deadLine)

            //when
            val result = mvc.post(URI_PREFIX) {
                content = om.writeValueAsString(questRequest)
                unitTestDefaultConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
            }
        }

        @DisplayName("데드라인 값이 현재 시간 + 5분 이전이면 400이 반환된다")
        @Test
        fun `데드라인 값이 현재 시간 + 5분 이전이면 400이 반환된다`() {
            //given
            val deadLine = now.plusMinutes(5).minusSeconds(1)
            val questRequest = WebQuestRequest("t", "d", deadLine = deadLine)

            //when
            val result = mvc.post(URI_PREFIX) {
                content = om.writeValueAsString(questRequest)
                unitTestDefaultConfiguration()
            }

            //then
            result.andExpect {
                status { isBadRequest() }
            }
        }

        @DisplayName("데드라인 값이 현재 시간 + 5분과 동일하면 400이 반환된다")
        @Test
        fun `데드라인 값이 현재 시간 + 5분과 동일하면 400이 반환된다`() {
            //given
            val deadLine = now.plusMinutes(5)
            val questRequest = WebQuestRequest("t", "d", deadLine = deadLine)

            //when
            val result = mvc.post(URI_PREFIX) {
                content = om.writeValueAsString(questRequest)
                unitTestDefaultConfiguration()
            }

            //then
            result.andExpect {
                status { isBadRequest() }
            }
        }

        @DisplayName("데드라인 값이 현재 시간 + 5분 이후면 200이 반환된다")
        @Test
        fun `데드라인 값이 현재 시간 + 5분 이후면 200이 반환된다`() {
            //given
            val deadLine = now.plusMinutes(5).plusSeconds(1)
            val questRequest = WebQuestRequest("t", "d", deadLine = deadLine)

            //when
            val result = mvc.post(URI_PREFIX) {
                content = om.writeValueAsString(questRequest)
                unitTestDefaultConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
            }
        }

        @DisplayName("데드라인 값이 다음 오전 6시 -5분 이후면 400이 반환된다")
        @Test
        fun `데드라인 값이 다음 오전 6시 -5분 이후면 400이 반환된다`() {
            //given
            val deadLine = nextReset.minusMinutes(5).plusSeconds(1)
            val questRequest = WebQuestRequest("t", "d", deadLine = deadLine)

            //when
            val result = mvc.post(URI_PREFIX) {
                content = om.writeValueAsString(questRequest)
                unitTestDefaultConfiguration()
            }

            //then
            result.andExpect {
                status { isBadRequest() }
            }
        }

        @DisplayName("데드라인 값이 다음 오전 6시 -5분과 동일하면 400이 반환된다")
        @Test
        fun `데드라인 값이 다음 오전 6시 -5분과 동일하면 400이 반환된다`() {
            //given
            val deadLine = nextReset.minusMinutes(5)
            val questRequest = WebQuestRequest("t", "d", deadLine = deadLine)

            //when
            val result = mvc.post(URI_PREFIX) {
                content = om.writeValueAsString(questRequest)
                unitTestDefaultConfiguration()
            }

            //then
            result.andExpect {
                status { isBadRequest() }
            }
        }

        @DisplayName("데드라인 값이 다음 오전 6시 -5분 이전이면 200이 반환된다")
        @Test
        fun `데드라인 값이 다음 오전 6시 -5분 이전이면 200이 반환된다`() {
            //given
            val deadLine = nextReset.minusMinutes(5).minusSeconds(1)
            val questRequest = WebQuestRequest("t", "d", deadLine = deadLine)

            //when
            val result = mvc.post(URI_PREFIX) {
                content = om.writeValueAsString(questRequest)
                unitTestDefaultConfiguration()
            }

            //then
            result.andExpect {
                status { isOk() }
            }
        }
    }

    @DisplayName("퀘스트 수정 시")
    @Nested
    inner class QuestUpdateTest {

        @ArgumentsSource(ValidQuestRequest::class)
        @DisplayName("DTO 필수 값이 모두 있다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `필수 값이 모두 있다면 200 OK가 반환된다`(questRequest: WebQuestRequest) {
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
        fun `필수 값이 없다면 400 BAD_REQUEST가 반환된다`(questRequest: WebQuestRequest?) {
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
            val questRequest = WebQuestRequest("title", "desc")
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

    @DisplayName("세부 퀘스트 카운트 변경 시")
    @Nested
    inner class UpdateDetailCountTest {

        @ValueSource(longs = [1, 5, 100, 5000])
        @DisplayName("PathVariable 값이 올바르다면 validation error가 발생하지 않는다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `PathVariable 값이 올바르다면 200 OK가 반환된다`(validQuestId: Long) {
            //given
            doReturn(detailResponse).`when`(questService).updateDetailQuestCount(anyLong(), any())

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$validQuestId/details/$validQuestId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("3")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @ArgumentsSource(InvalidLongSources::class)
        @DisplayName("PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400이 반환한다")
        fun `PathVariable 값이 올바르지 않으면 400 BAD_REQUEST가 반환된다`(invalidQuestId: Any) {
            //given
            val validDetailQuestId = 1L

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$invalidQuestId/details/$validDetailQuestId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").exists())
                .andExpect(jsonPath("$.errorResponse.errors").exists())
                .andReturn()
        }

        @ValueSource(strings = ["0", "1", "125", "254", "255"])
        @DisplayName("RequestBody 값이 유효하다면 200 OK가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 200을 반환한다")
        fun `RequestBody 값이 유효하다면 200 OK가 반환된다`(count: String) {
            //given
            val validPathVariable = 1L
            doReturn(detailResponse).`when`(questService).updateDetailQuestCount(anyLong(), any())

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$validPathVariable/details/$validPathVariable")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(count)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @DisplayName("requestBody가 없어도 200 OK가 반환된다")
        @Test
        fun `requestBody가 없어도 200 OK가 반환된다`() {
            //given
            val questId = 1L
            doReturn(detailResponse).`when`(questService).updateDetailQuestCount(anyLong(), any())

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/details/$questId")
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @ValueSource(strings = ["-1", "256"])
        @DisplayName("RequestBody 값이 유효하지 않다면 400 BAD_REQUEST가 반환된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 400을 반환한다")
        fun `RequestBody 값이 유효하지 않다면 400 BAD_REQUEST가 반환된다`(count: String) {
            //given
            val questId = 1L
            doReturn(detailResponse).`when`(questService).updateDetailQuestCount(anyLong(), any())

            //when
            val result = mvc.perform(
                patch("$URI_PREFIX/$questId/details/$questId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(count)
                    .with(csrf())
            )

            //then
            result
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse").exists())
        }

        @DisplayName("pathVariable 정보가 담긴 DTO로 서비스에 요청을 위임한다")
        @Test
        fun `pathVariable 정보가 담긴 DTO로 서비스에 요청을 위임한다`() {
            //given
            val questId = 1L
            val detailQuestId = 1L
            val count = 3
            val requestDtoCaptor = argumentCaptor<DetailInteractRequest>()
            doReturn(detailResponse).`when`(questService).updateDetailQuestCount(anyLong(), requestDtoCaptor.capture())

            //when
            mvc.perform(
                patch("$URI_PREFIX/$questId/details/$detailQuestId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(count.toString())
                    .with(csrf())
            )

            //then
            val requestDto = requestDtoCaptor.firstValue
            assertThat(requestDto.questId).isEqualTo(questId)
            assertThat(requestDto.detailQuestId).isEqualTo(detailQuestId)
            assertThat(requestDto.count).isEqualTo(count)
        }
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
                Arguments.of(WebQuestRequest("title", "desc")),
            )
        }
    }

    class InValidQuestRequest: ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            val details = mutableListOf(WebDetailQuestRequest("", DetailQuestType.COUNT, 0))
            return Stream.of(
                Arguments.of(WebQuestRequest("", "", details)),
                Arguments.of(null),
            )
        }
    }

    companion object {
        const val URI_PREFIX = "/api/v1/quests"
    }
}