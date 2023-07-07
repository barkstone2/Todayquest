package dailyquest.quest.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dailyquest.common.MessageUtil
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import dailyquest.common.ResponseData
import dailyquest.common.RestPage
import dailyquest.jwt.JwtTokenProvider
import dailyquest.properties.RedisKeyProperties
import dailyquest.quest.dto.*
import dailyquest.quest.entity.*
import dailyquest.quest.repository.QuestLogRepository
import dailyquest.quest.repository.QuestRepository
import dailyquest.user.dto.UserRequestDto
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import dailyquest.user.service.UserService
import java.time.LocalTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("DEPRECATION")
@DisplayName("퀘스트 API 컨트롤러 통합 테스트")
@Transactional
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
)
class QuestApiControllerTest @Autowired constructor(
    var questRepository: QuestRepository,
    var userRepository: UserRepository,
    var userService: UserService,
    var context: WebApplicationContext,
    var questLogRepository: QuestLogRepository,
    var redisTemplate: RedisTemplate<String, String>,
    var redisKeyProperties: RedisKeyProperties,
) {

    companion object {
        const val SERVER_ADDR = "http://localhost:"
        const val URI_PREFIX = "/api/v1/quests"
    }

    @LocalServerPort
    var port = 0

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Value("\${quest.page.size}")
    var pageSize: Int = 0

    lateinit var mvc: MockMvc
    lateinit var testUser: UserInfo
    lateinit var anotherUser: UserInfo
    lateinit var token: Cookie
    val om: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())

    @BeforeEach
    fun setUp() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()


        testUser = userRepository.getReferenceById(userService.getOrRegisterUser("quest-controller-user1", ProviderType.GOOGLE).id)
        anotherUser = userRepository.getReferenceById(userService.getOrRegisterUser("quest-controller-user2", ProviderType.GOOGLE).id)

        val accessToken = jwtTokenProvider.createAccessToken(testUser.id)
        token = jwtTokenProvider.createAccessTokenCookie(accessToken)
    }

    @DisplayName("퀘스트 목록 요청 시")
    @Nested
    inner class QuestListTest {

        @EnumSource(QuestState::class)
        @DisplayName("요청한 State에 맞는 퀘스트만 조회된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 {0} 상태의 퀘스트만 조회된다")
        fun `요청한 State에 맞는 퀘스트만 조회된다`(state: QuestState) {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"

            val listByState: MutableMap<QuestState, List<QuestResponse>> = mutableMapOf()

            for (stateEnum in QuestState.values()) {
                val savedQuest = questRepository.save(Quest("제목", "1", testUser, 1L, stateEnum, QuestType.MAIN))
                val questResponse = QuestResponse.createDto(savedQuest)
                listByState[stateEnum] = listOf(questResponse)
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .queryParam("state", state.toString())
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(list).containsExactlyElementsOf(listByState[state])
            assertThat(list).allMatch { quest -> quest.state == state }
        }

        @DisplayName("요청 User의 퀘스트만 조회된다")
        @Test
        fun `요청 User의 퀘스트만 조회된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"

            val listOfUser = mutableListOf<QuestResponse>()

            for (i in 1..2) {
                val savedQuest = questRepository.save(Quest("본인", "1", testUser, i.toLong(), QuestState.PROCEED, QuestType.MAIN))
                val questResponse = QuestResponse.createDto(savedQuest)
                listOfUser += questResponse
            }

            for (i in 1..3) {
                questRepository.save(Quest("다른 유저", "1", anotherUser, i.toLong(), QuestState.PROCEED, QuestType.MAIN))
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(listOfUser).containsAll(list)
        }

        @DisplayName("page 번호 파라미터가 없으면 0 페이지의 퀘스트가 조회된다")
        @Test
        fun `page 번호 파라미터가 없으면 0 페이지의 퀘스트가 조회된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"

            for (pageNo in 0..1) {
                for (j in 1..pageSize) {
                    questRepository.save(Quest("$pageNo", "1", testUser, pageNo.toLong(), QuestState.PROCEED, QuestType.MAIN))
                }
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(list).allMatch { quest -> quest.title == "0" }
        }

        @DisplayName("page 번호가 0보다 큰 int 범위의 숫자면 해당 페이지가 조회된다")
        @ValueSource(ints = [0, 1, 15, 500])
        @ParameterizedTest(name = "{0} 값이 들어오면 해당 페이지를 조회한다")
        fun `page 번호가 0보다 큰 int 범위의 숫자면 해당 페이지가 조회된다`(page: Int) {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"

            for (pageNo in 0..1) {
                for (j in 1..pageSize) {
                    questRepository.save(Quest("$pageNo", "1", testUser, pageNo.toLong(), QuestState.PROCEED, QuestType.MAIN))
                }
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .queryParam("page", page.toString())
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(data?.number).isEqualTo(page)
            assertThat(list).allMatch { quest -> quest.title == page.toString() }
        }

        @DisplayName("page 번호가 0보다 큰 int 범위의 숫자가 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidIntegerSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST를 반환한다")
        fun `page 번호가 숫자가 아니면 BAD_REQUEST가 반환된다`(page: Any) {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    get(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .queryParam("page", page.toString())
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }
    }

    @DisplayName("퀘스트 조회 시")
    @Nested
    inner class QuestGetTest {

        @DisplayName("본인의 퀘스트 요청 시 퀘스트가 조회된다")
        @Test
        fun `본인의 퀘스트 요청 시 퀘스트가 조회된다`() {
            //given
            val savedQuest = questRepository.save(Quest("제목", "1", testUser, 1L, QuestState.PROCEED, QuestType.MAIN))

            val detailRequest = DetailRequest("detail", DetailQuestType.COUNT, 3)
            savedQuest.updateDetailQuests(listOf(detailRequest))

            val questId = savedQuest.id
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/$questId"

            //when
            val request = mvc
                .perform(
                    get(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data?.title).isEqualTo(savedQuest.title)
            assertThat(data?.id).isEqualTo(savedQuest.id)
            assertThat(data?.detailQuests?.get(0)?.title).isEqualTo(detailRequest.title)
            assertThat(error).isNull()
        }


        @DisplayName("존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다")
        @Test
        fun `존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다`() {
            //given
            val savedQuest = questRepository.save(Quest("제목", "1", testUser, 1L, QuestState.PROCEED, QuestType.MAIN))
            val questId = savedQuest.id + 1
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/$questId"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            //when
            val request = mvc
                .perform(
                    get(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }


        @DisplayName("타인의 퀘스트 요청 시 FORBIDDEN 이 반환된다")
        @Test
        fun `타인의 퀘스트 요청 시 FORBIDDEN 이 반환된다`() {
            //given
            val savedQuest = questRepository.save(Quest("제목", "1", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN))
            val questId = savedQuest.id
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/$questId"
            val errorMessage = MessageUtil.getMessage("exception.access.denied")

            //when
            val request = mvc
                .perform(
                    get(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isForbidden)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }


        @DisplayName("Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/$questId"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    get(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

    }

    @DisplayName("퀘스트 등록 시")
    @Nested
    open inner class QuestSaveTest {

        @DisplayName("RequestBody 구문이 올바르지 않으면 BAD_REQUEST가 반환된다")
        @Test
        fun `RequestBody 구문이 올바르지 않으면 BAD_REQUEST가 반환된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")
            val requestBody = "invalid body"

            //when
            val request = mvc
                .perform(
                    post(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }
        
        @DisplayName("DTO Validation에 실패하면 BAD_REQUEST가 반환된다")
        @Test
        fun `DTO Validation에 실패하면 BAD_REQUEST가 반환된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")
            val questRequest = QuestRequest("", "", mutableListOf(DetailRequest("", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)
            val bindingMessages = listOf(
                MessageUtil.getMessage("NotBlank.quest.title"),
                MessageUtil.getMessage("NotBlank.details.title")
            )

            //when
            val request = mvc
                .perform(
                    post(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
            assertThat(error?.errors?.values?.map { it[0] }).containsExactlyInAnyOrderElementsOf(bindingMessages)
        }

        @DisplayName("멀티 쓰레드로 동시에 요청하면 서로 다른 seq가 등록된다")
        @Test
        fun `멀티 쓰레드로 동시에 요청하면 서로 다른 seq로 등록된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"
            val questRequest = QuestRequest("title", "des")
            val requestBody = om.writeValueAsString(questRequest)
            val numOfThreads = 3
            val seqSet = ConcurrentHashMap.newKeySet<Long>()

            //when
            runBlocking(Dispatchers.IO) {
                val user1 = UserInfo("", "", ProviderType.GOOGLE)
                testUser = userRepository.save(user1)

                val accessToken = jwtTokenProvider.createAccessToken(testUser.id)
                token = jwtTokenProvider.createAccessTokenCookie(accessToken)
                repeat(numOfThreads) {
                    launch {

                        val request = mvc
                            .perform(
                                post(url)
                                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                                    .with(csrf())
                                    .cookie(token)
                                    .content(requestBody)
                            )
                        val body = request
                            .andExpect(status().isOk)
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                            .andReturn()
                            .response
                            .contentAsString

                        val result = om.readValue(body, object : TypeReference<ResponseData<QuestResponse>>() {})
                        seqSet.add(result.data?.seq!!)
                    }
                }
            }

            //then
            assertThat(seqSet.size).isEqualTo(numOfThreads)
        }

        @DisplayName("현재 시간이 유저의 코어 타임이라면 메인 퀘스트로 등록된다")
        @Test
        fun `현재 시간이 유저의 코어 타임이라면 메인 퀘스트로 등록된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)
            testUser.changeUserSettings(UserRequestDto(0, LocalTime.now().hour))

            //when
            val request = mvc
                .perform(
                    post(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data

            assertThat(data?.type).isEqualTo(QuestType.MAIN)
        }

        @DisplayName("현재 시간이 유저의 코어 타임이 아니면 서브 퀘스트로 등록된다")
        @Test
        fun `현재 시간이 유저의 코어 타임이 아니면 서브 퀘스트로 등록된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)

            val now = LocalTime.now()
            testUser.changeUserSettings(UserRequestDto(0, now.plusHours(2).hour))

            //when
            val request = mvc
                .perform(
                    post(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data

            assertThat(data?.type).isEqualTo(QuestType.SUB)
        }

        @DisplayName("퀘스트 등록 로그가 등록된다")
        @Test
        fun `퀘스트 등록 로그가 등록된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    post(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data

            val allQuestLog = questLogRepository.findAll()
            assertThat(allQuestLog).anyMatch { log -> log.state == QuestState.PROCEED && log.questId == data?.id }
        }

        @DisplayName("퀘스트가 정상적으로 등록된다")
        @Test
        fun `퀘스트가 정상적으로 등록된다`() {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}"
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    post(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data?.title).isEqualTo(questRequest.title)
            assertThat(data?.description).isEqualTo(questRequest.description)
            assertThat(data?.detailQuests?.get(0)?.title).isEqualTo(questRequest.details[0].title)
            assertThat(error).isNull()
        }
    }

    @Nested
    @DisplayName("퀘스트 수정 시")
    inner class QuestUpdateTest {

        @DisplayName("Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            //given
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/$questId"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("DTO Validation에 실패하면 BAD_REQUEST가 반환된다")
        @Test
        fun `DTO Validation에 실패하면 BAD_REQUEST가 반환된다`() {
            //given
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            val detailRequest = DetailRequest("", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("", "", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            val bindingMessages = listOf(
                MessageUtil.getMessage("NotBlank.quest.title"),
                MessageUtil.getMessage("NotBlank.details.title")
            )

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
            assertThat(error?.errors?.values?.map { it[0] }).containsExactlyInAnyOrderElementsOf(bindingMessages)
        }

        @DisplayName("다른 유저의 퀘스트를 요청하면 FORBIDDEN이 반환된다")
        @Test
        fun `다른 유저의 퀘스트를 요청하면 FORBIDDEN이 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", anotherUser, 1L, QuestState.PROCEED, QuestType.SUB))

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}"
            val errorMessage = MessageUtil.getMessage("exception.access.denied")

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isForbidden)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다")
        @Test
        fun `존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다`() {
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/10000"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("진행 중인 퀘스트가 아니라면 BAD_REQUEST 를 반환한다")
        @Test
        fun `진행 중인 퀘스트가 아니라면 BAD_REQUEST 를 반환한다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.FAIL, QuestType.SUB))

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}"
            val errorMessage = MessageUtil.getMessage("quest.error.not-proceed")

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("퀘스트 타입이 변하지 않는다")
        @Test
        fun `퀘스트 타입이 변하지 않는다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.MAIN))

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}"

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data

            assertThat(data?.type).isEqualTo(savedQuest.type)
        }

        @DisplayName("수정된 결과가 반환된다")
        @Test
        fun `수정된 결과가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}"

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data?.title).isEqualTo(questRequest.title)
            assertThat(data?.description).isEqualTo(questRequest.description)
            assertThat(data?.type).isEqualTo(savedQuest.type)
            assertThat(data?.detailQuests?.get(0)?.title).isEqualTo(detailRequest.title)
            assertThat(error).isNull()
        }

    }

    @DisplayName("퀘스트 삭제 시")
    @Nested
    inner class QuestDeleteTest {

        @DisplayName("Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/$questId/delete"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("다른 유저의 퀘스트를 요청하면 FORBIDDEN이 반환된다")
        @Test
        fun `다른 유저의 퀘스트를 요청하면 FORBIDDEN이 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", anotherUser, 1L, QuestState.PROCEED, QuestType.SUB))

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/delete"
            val errorMessage = MessageUtil.getMessage("exception.access.denied")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isForbidden)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다")
        @Test
        fun `존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다`() {
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/10000/delete"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("요청 완료 후 퀘스트가 삭제 상태가 된다")
        @Test
        fun `요청 완료 후 퀘스트가 삭제 상태가 된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/delete"

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val error = result.errorResponse

            assertThat(savedQuest.state).isEqualTo(QuestState.DELETE)
            assertThat(error).isNull()
        }
    }


    @DisplayName("퀘스트 완료 시")
    @Nested
    inner class QuestCompleteTest {

        @DisplayName("Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/$questId/complete"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("삭제된 퀘스트라면 BAD_REQUEST가 반환된다")
        @Test
        fun `삭제된 퀘스트라면 BAD_REQUEST가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.DELETE, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/complete"
            val errorMessage = MessageUtil.getMessage("quest.error.deleted")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("진행중인 퀘스트가 아니라면 BAD_REQUEST가 반환된다")
        @Test
        fun `진행중인 퀘스트가 아니라면 BAD_REQUEST가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.FAIL, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/complete"
            val errorMessage = MessageUtil.getMessage("quest.error.not-proceed")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("완료하지 않은 세부 퀘스트가 있다면 BAD_REQUEST가 반환된다")
        @Test
        fun `완료하지 않은 세부 퀘스트가 있다면 BAD_REQUEST가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))
            val detailRequest = DetailRequest("detail", DetailQuestType.CHECK, 1)
            savedQuest.updateDetailQuests(listOf(detailRequest))

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/complete"
            val errorMessage = MessageUtil.getMessage("quest.error.complete.detail")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("요청 완료 후 퀘스트가 완료 상태가 된다")
        @Test
        fun `요청 완료 후 퀘스트가 완료 상태가 된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/complete"

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val error = result.errorResponse

            assertThat(savedQuest.state).isEqualTo(QuestState.COMPLETE)
            assertThat(error).isNull()
        }

        @DisplayName("메인 퀘스트 완료 시 경험치와 골드를 획득한다")
        @Test
        fun `메인 퀘스트 완료 시 경험치와 골드를 두배로 획득한다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.MAIN))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/complete"

            val ops = redisTemplate.boundHashOps<String, Long>(redisKeyProperties.settings)

            val questClearExp = ops[redisKeyProperties.questClearExp]!!
            val questClearGold = ops[redisKeyProperties.questClearGold]!!

            val beforeExp = testUser.exp
            val beforeGold = testUser.gold

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))

            assertThat(testUser.exp).isEqualTo(beforeExp + questClearExp*2)
            assertThat(testUser.gold).isEqualTo(beforeGold + questClearGold*2)
        }


        @DisplayName("서브 퀘스트 완료 시 1배의 경험치와 골드를 획득한다")
        @Test
        fun `서브 퀘스트 완료 시 1배의 경험치와 골드를 획득한다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/complete"

            val ops = redisTemplate.boundHashOps<String, Long>(redisKeyProperties.settings)

            val questClearExp = ops[redisKeyProperties.questClearExp]!!
            val questClearGold = ops[redisKeyProperties.questClearGold]!!

            val beforeExp = testUser.exp
            val beforeGold = testUser.gold

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))

            assertThat(testUser.exp).isEqualTo(beforeExp + questClearExp)
            assertThat(testUser.gold).isEqualTo(beforeGold + questClearGold)
        }

        @DisplayName("로그 테이블에 데이터가 등록된다")
        @Test
        fun `로그 테이블에 데이터가 등록된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/complete"

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))

            val allQuestLog = questLogRepository.findAll()
            assertThat(allQuestLog).anyMatch { log -> log.questId == savedQuest.id && log.state == QuestState.COMPLETE }
        }
    }

    @DisplayName("퀘스트 포기 시")
    @Nested
    inner class QuestDiscardTest {

        @DisplayName("Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/$questId/discard"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("삭제된 퀘스트라면 BAD_REQUEST가 반환된다")
        @Test
        fun `삭제된 퀘스트라면 BAD_REQUEST가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.DELETE, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/discard"
            val errorMessage = MessageUtil.getMessage("quest.error.deleted")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("진행중인 퀘스트가 아니라면 BAD_REQUEST가 반환된다")
        @Test
        fun `진행중인 퀘스트가 아니라면 BAD_REQUEST가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.FAIL, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/discard"
            val errorMessage = MessageUtil.getMessage("quest.error.not-proceed")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("요청 완료 후 퀘스트가 포기 상태가 된다")
        @Test
        fun `요청 완료 후 퀘스트가 포기 상태가 된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/discard"

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val error = result.errorResponse

            assertThat(savedQuest.state).isEqualTo(QuestState.DISCARD)
            assertThat(error).isNull()
        }

        @DisplayName("로그 테이블에 데이터가 등록된다")
        @Test
        fun `로그 테이블에 데이터가 등록된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/discard"

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))

            val allQuestLog = questLogRepository.findAll()
            assertThat(allQuestLog).anyMatch { log -> log.questId == savedQuest.id && log.state == QuestState.DISCARD }
        }
    }

    @DisplayName("세부 퀘스트 상호작용 시")
    @Nested
    inner class DetailInteractTest {

        @DisplayName("Path Variable의 quest id가 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable의 quest id가 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/$questId/details/1"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("Path Variable의 detail quest id가 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable의 detail quest id가 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(detailQuestId: Any) {
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/1/details/$detailQuestId"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("DTO Validation에 실패하면 BAD_REQUEST가 반환된다")
        @Test
        fun `DTO Validation에 실패하면 BAD_REQUEST가 반환된다`() {
            //given
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))

            val detailRequest = DetailRequest("detail", DetailQuestType.CHECK, 1)
            savedQuest.updateDetailQuests(listOf(detailRequest))

            val detailQuestId = savedQuest.detailQuests[0].id

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/details/$detailQuestId"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            val interactRequest = DetailInteractRequest(300)
            val requestBody = om.writeValueAsString(interactRequest)

            val bindingMessages = listOf(
                MessageUtil.getMessage("Range.details.count"),
            )

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
            assertThat(error?.errors?.values?.map { it[0] }).containsExactlyInAnyOrderElementsOf(bindingMessages)
        }

        @DisplayName("다른 유저의 퀘스트를 요청하면 FORBIDDEN이 반환된다")
        @Test
        fun `다른 유저의 퀘스트를 요청하면 FORBIDDEN이 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", anotherUser, 1L, QuestState.PROCEED, QuestType.SUB))

            val detailRequest = DetailRequest("detail", DetailQuestType.CHECK, 1)
            savedQuest.updateDetailQuests(listOf(detailRequest))

            questRepository.flush()

            val detailQuestId = savedQuest.detailQuests[0].id

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/details/$detailQuestId"
            val errorMessage = MessageUtil.getMessage("exception.access.denied")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isForbidden)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다")
        @Test
        fun `존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다`() {
            val url = "${SERVER_ADDR}$port${URI_PREFIX}/10000/details/1"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("퀘스트에 포함되지 않은 세부 퀘스트 요청 시 BAD_REQUEST가 반환된다")
        @Test
        fun `퀘스트에 포함되지 않은 세부 퀘스트 요청 시 BAD_REQUEST가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/details/1"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("진행중인 퀘스트가 아니라면 BAD_REQUEST가 반환된다")
        @Test
        fun `진행중인 퀘스트가 아니라면 BAD_REQUEST가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.FAIL, QuestType.SUB))

            val detailRequest = DetailRequest("detail", DetailQuestType.CHECK, 1)
            savedQuest.updateDetailQuests(listOf(detailRequest))

            questRepository.flush()

            val detailQuestId = savedQuest.detailQuests[0].id

            val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/details/$detailQuestId"
            val errorMessage = MessageUtil.getMessage("quest.error.not-proceed")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .with(csrf())
                        .cookie(token)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }


        @DisplayName("카운트 값이 있으면 조건에 따라 대입한다")
        @Nested
        inner class ChangeCountTest {

            @DisplayName("카운트가 목표 카운트보다 크거나 같으면 목표 카운트만큼 대입하고 완료 상태로 만든다")
            @Test
            fun `목표 카운트만큼만 대입하고 완료 상태로 만든다`() {
                val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))

                val targetCount = 3
                val detailRequest = DetailRequest("detail", DetailQuestType.COUNT, targetCount)
                savedQuest.updateDetailQuests(listOf(detailRequest))

                questRepository.flush()

                val detailQuest = savedQuest.detailQuests[0]
                val detailQuestId = detailQuest.id

                val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/details/$detailQuestId"

                val count = 5
                val interactRequest = DetailInteractRequest(count)
                val requestBody = om.writeValueAsString(interactRequest)

                //when
                val request = mvc
                    .perform(
                        patch(url)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .with(csrf())
                            .cookie(token)
                            .content(requestBody)
                    )

                //then
                val body = request
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
                    .response
                    .contentAsString

                val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

                val data = result.data
                val error = result.errorResponse

                assertThat(error).isNull()
                assertThat(data?.count).isEqualTo(targetCount)
                assertThat(data?.state).isEqualTo(DetailQuestState.COMPLETE)
                assertThat(detailQuest.count).isEqualTo(targetCount)
            }

            @DisplayName("카운트가 목표 카운트보다 작으면 진행 상태로 변경한다")
            @Test
            fun `카운트가 목표 카운트 보다 작다면 진행 상태로 변경한다`() {
                val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))

                val targetCount = 5
                val detailRequest = DetailRequest("detail", DetailQuestType.COUNT, targetCount)
                savedQuest.updateDetailQuests(listOf(detailRequest))

                questRepository.flush()

                val detailQuest = savedQuest.detailQuests[0]

                val detailState = DetailQuest::class.java.getDeclaredField("state")
                detailState.isAccessible = true
                detailState.set(detailQuest, DetailQuestState.COMPLETE)

                val beforeState = detailQuest.state

                val detailQuestId = detailQuest.id

                val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/details/$detailQuestId"

                val count = 3
                val interactRequest = DetailInteractRequest(count)
                val requestBody = om.writeValueAsString(interactRequest)

                //when
                val request = mvc
                    .perform(
                        patch(url)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .with(csrf())
                            .cookie(token)
                            .content(requestBody)
                    )

                //then
                val body = request
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
                    .response
                    .contentAsString

                val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

                val data = result.data
                val error = result.errorResponse

                assertThat(error).isNull()
                assertThat(beforeState).isEqualTo(DetailQuestState.COMPLETE)
                assertThat(data?.count).isEqualTo(count)
                assertThat(data?.state).isEqualTo(DetailQuestState.PROCEED)
                assertThat(detailQuest.count).isEqualTo(count)
            }
        }

        @DisplayName("카운트 값이 없으면")
        @Nested
        inner class AddCountTest {

            @DisplayName("세부 퀘스트가 완료 상태일 때 카운트를 0으로 초기화 하고 진행 상태로 변경한다")
            @Test
            fun `세부 퀘스트가 완료 상태일 때 카운트를 초기화 하고 진행 상태로 변경한다`() {
                val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))

                val targetCount = 5
                val detailRequest = DetailRequest("detail", DetailQuestType.COUNT, targetCount)
                savedQuest.updateDetailQuests(listOf(detailRequest))

                questRepository.flush()

                val detailQuest = savedQuest.detailQuests[0]

                val detailState = DetailQuest::class.java.getDeclaredField("state")
                detailState.isAccessible = true
                detailState.set(detailQuest, DetailQuestState.COMPLETE)

                val beforeState = detailQuest.state

                val detailQuestId = detailQuest.id

                val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/details/$detailQuestId"


                //when
                val request = mvc
                    .perform(
                        patch(url)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .with(csrf())
                            .cookie(token)
                    )

                //then
                val body = request
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
                    .response
                    .contentAsString

                val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

                val data = result.data
                val error = result.errorResponse

                assertThat(error).isNull()
                assertThat(beforeState).isEqualTo(DetailQuestState.COMPLETE)
                assertThat(data?.count).isEqualTo(0)
                assertThat(data?.state).isEqualTo(DetailQuestState.PROCEED)
                assertThat(detailQuest.count).isEqualTo(0)
            }

            @DisplayName("세부 퀘스트가 완료 상태가 아닐때 카운트를 1 증가시킨다")
            @Test
            fun `세부 퀘스트가 완료 상태가 아니면 카운트를 1 증가시킨다`() {
                val savedQuest = questRepository.save(Quest("title", "desc", testUser, 1L, QuestState.PROCEED, QuestType.SUB))

                val targetCount = 5
                val detailRequest = DetailRequest("detail", DetailQuestType.COUNT, targetCount)
                savedQuest.updateDetailQuests(listOf(detailRequest))

                questRepository.flush()

                val detailQuest = savedQuest.detailQuests[0]
                val beforeCount = detailQuest.count

                val detailQuestId = detailQuest.id
                val url = "${SERVER_ADDR}$port${URI_PREFIX}/${savedQuest.id}/details/$detailQuestId"

                //when
                val request = mvc
                    .perform(
                        patch(url)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .with(csrf())
                            .cookie(token)
                    )

                //then
                val body = request
                    .andExpect(status().isOk)
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn()
                    .response
                    .contentAsString

                val result = om.readValue(body, object: TypeReference<ResponseData<DetailResponse>>(){})

                val data = result.data
                val error = result.errorResponse

                assertThat(error).isNull()
                assertThat(data?.count).isEqualTo(beforeCount + 1)
                assertThat(data?.state).isEqualTo(DetailQuestState.PROCEED)
                assertThat(detailQuest.count).isEqualTo(beforeCount + 1)
            }
        }

    }

}