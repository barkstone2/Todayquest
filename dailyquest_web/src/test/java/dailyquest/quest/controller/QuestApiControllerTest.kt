package dailyquest.quest.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementAchieveLogRepository
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.common.MessageUtil
import dailyquest.common.ResponseData
import dailyquest.common.RestPage
import dailyquest.context.IntegrationTestContextWithRedisAndElasticsearch
import dailyquest.jwt.JwtTokenProvider
import dailyquest.properties.RedisKeyProperties
import dailyquest.quest.dto.*
import dailyquest.quest.entity.*
import dailyquest.quest.repository.QuestLogRepository
import dailyquest.quest.repository.QuestRepository
import dailyquest.search.repository.QuestIndexRepository
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.User
import dailyquest.user.repository.UserRepository
import jakarta.persistence.EntityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
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
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap

@DisplayName("퀘스트 API 컨트롤러 통합 테스트")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class QuestApiControllerTest @Autowired constructor(
    context: WebApplicationContext,
    userRepository: UserRepository,
    jwtTokenProvider: JwtTokenProvider,
    var questRepository: QuestRepository,
    var questLogRepository: QuestLogRepository,
    var redisTemplate: RedisTemplate<String, String>,
    var redisKeyProperties: RedisKeyProperties,
    var entityManager: EntityManager,
    var questIndexRepository: QuestIndexRepository,
    private val achievementRepository: AchievementRepository,
    private val achievementAchieveLogRepository: AchievementAchieveLogRepository,
): IntegrationTestContextWithRedisAndElasticsearch(context, userRepository, jwtTokenProvider) {

    private val uriPrefix = "/api/v1/quests"

    @Value("\${quest.page.size}")
    private var pageSize: Int = 0

    @DisplayName("현재 퀘스트 목록 요청 시")
    @Nested
    inner class CurrentQuestListTest {
        private val url = "$SERVER_ADDR$port$uriPrefix"

        @EnumSource(QuestState::class)
        @DisplayName("요청한 State에 맞는 퀘스트만 조회된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 {0} 상태의 퀘스트만 조회된다")
        fun `요청한 State에 맞는 퀘스트만 조회된다`(state: QuestState) {
            //given
            for (stateEnum in QuestState.values()) {
                questRepository.save(Quest("제목", "1", user.id, 1L, stateEnum, QuestType.MAIN))
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("state", state.toString())
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
            val result: ResponseData<List<QuestResponse>> = om.readValue(body)
            val resultQuests = result.data
            assertThat(resultQuests).allMatch { quest -> quest.state == state }
        }

        @DisplayName("요청 User의 퀘스트만 조회된다")
        @Test
        fun `요청 User의 퀘스트만 조회된다`() {
            //given
            val questIdsOfAnotherUser = mutableListOf<Long>()
            for (i in 1..3) {
                val savedQuest = questRepository.save(Quest("다른 유저", "1", anotherUser.id, i.toLong(), QuestState.PROCEED, QuestType.MAIN))
                questIdsOfAnotherUser.add(savedQuest.id)
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
            val result: ResponseData<List<QuestResponse>> = om.readValue(body)
            val resultQuests = result.data
            assertThat(resultQuests).noneMatch { questIdsOfAnotherUser.contains(it.id) }
        }
    }


    @DisplayName("퀘스트 검색 시")
    @Nested
    inner class SearchQuestTest {
        private val url = "${SERVER_ADDR}$port${uriPrefix}/search"

        @EnumSource(QuestState::class)
        @DisplayName("요청한 State에 맞는 퀘스트만 조회된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 {0} 상태의 퀘스트만 조회된다")
        fun `요청한 State에 맞는 퀘스트만 조회된다`(state: QuestState) {
            //given
            for (stateEnum in QuestState.values()) {
                questRepository.save(Quest("제목", "1", user.id, 1L, stateEnum, QuestType.MAIN))
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("state", state.toString())
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
            val result: ResponseData<RestPage<QuestResponse>> = om.readValue(body)
            val data = result.data
            val resultQuests = data?.content
            assertThat(resultQuests).allMatch { quest -> quest.state == state }
        }

        @DisplayName("요청 User의 퀘스트만 조회된다")
        @Test
        fun `요청 User의 퀘스트만 조회된다`() {
            //given
            val questIdsOfAnotherUser = mutableListOf<Long>()
            for (i in 1..3) {
                val savedQuest = questRepository.save(Quest("다른 유저", "1", anotherUser.id, i.toLong(), QuestState.PROCEED, QuestType.MAIN))
                questIdsOfAnotherUser.add(savedQuest.id)
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
            val result: ResponseData<RestPage<QuestResponse>> = om.readValue(body)
            val data = result.data
            val resultQuests = data?.content
            assertThat(resultQuests).noneMatch { questIdsOfAnotherUser.contains(it.id) }
        }

        @DisplayName("page 번호 파라미터가 없으면 0 페이지의 퀘스트가 조회된다")
        @Test
        fun `page 번호 파라미터가 없으면 0 페이지의 퀘스트가 조회된다`() {
            //given
            for (pageNo in 1 downTo 0) {
                for (j in 1..pageSize) {
                    questRepository.save(Quest("$pageNo", "1", user.id, pageNo.toLong(), QuestState.PROCEED, QuestType.MAIN))
                }
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
            val result: ResponseData<RestPage<QuestResponse>> = om.readValue(body)
            val data = result.data
            val quests = data?.content
            assertThat(quests).allMatch { quest -> quest.title == "0" }
        }

        @DisplayName("page 번호가 0보다 큰 int 범위의 숫자면 해당 페이지가 조회된다")
        @ValueSource(ints = [0, 1, 15, 500])
        @ParameterizedTest(name = "{0} 값이 들어오면 해당 페이지를 조회한다")
        fun `page 번호가 0보다 큰 int 범위의 숫자면 해당 페이지가 조회된다`(page: Int) {
            //given
            for (pageNo in 1 downTo 0) {
                for (j in 1..pageSize) {
                    questRepository.save(Quest("$pageNo", "1", user.id, pageNo.toLong(), QuestState.PROCEED, QuestType.MAIN))
                }
            }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("page", page.toString())
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString
            val result: ResponseData<RestPage<QuestResponse>> = om.readValue(body)
            val data = result.data
            val list = data?.content

            assertThat(list).allMatch { quest -> quest.title == page.toString() }
        }

        @DisplayName("page 번호가 0보다 큰 int 범위의 숫자가 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidIntegerSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST를 반환한다")
        fun `page 번호가 숫자가 아니면 BAD_REQUEST가 반환된다`(page: Any) {
            //given
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("page", page.toString())
                )

            //then
            request
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").value(errorMessage))
        }

        @DisplayName("시작 날짜 검색 조건만 존재하는 경우, 요청한 시작 날짜의 오전 6시 또는 이후에 등록된 퀘스트만 조회된다")
        @Test
        fun `시작 날짜 검색 조건만 존재하는 경우, 요청한 시작 날짜의 오전 6시 또는 이후에 등록된 퀘스트만 조회된다`() {
            //given
            val startDate = LocalDate.of(2022, 12, 1)
            val startDateTime = LocalDateTime.of(startDate, LocalTime.of(6, 0))

            val beforeConditionTime = LocalDateTime.of(startDate, LocalTime.of(5, 59))
            insertQuestWithCreatedTime(beforeConditionTime)
            val sameConditionTime = LocalDateTime.of(startDate, LocalTime.of(6, 0))
            insertQuestWithCreatedTime(sameConditionTime)
            val afterConditionTime = LocalDateTime.of(startDate, LocalTime.of(6, 1))
            insertQuestWithCreatedTime(afterConditionTime)

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("startDate", startDate.toString())
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})
            val data = result.data
            val list = data?.content
            assertThat(list).noneMatch { quest -> quest.createdDate.isBefore(startDateTime) }
        }

        @DisplayName("끝 날짜 검색 조건만 존재하는 경우, 요청한 끝 날짜의 다음날 오전 6시 이전에 등록된 퀘스트만 조회된다")
        @Test
        fun `끝 날짜 검색 조건만 존재하는 경우, 요청한 끝 날짜의 다음날 오전 6시 이전에 등록된 퀘스트만 조회된다`() {
            //given
            val endDate = LocalDate.of(2022, 12, 1)
            val endDateTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(6, 0))

            val beforeConditionTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(5, 59))
            insertQuestWithCreatedTime(beforeConditionTime)
            val sameConditionTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(6, 0))
            insertQuestWithCreatedTime(sameConditionTime)
            val afterConditionTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(6, 1))
            insertQuestWithCreatedTime(afterConditionTime)

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("endDate", endDate.toString())
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(list).noneMatch { quest -> quest.createdDate.isAfter(endDateTime) }
        }

        @DisplayName("날짜 검색 조건이 둘 다 존재하는 경우, 시작 날짜 오전 6시 또는 이후부터 종료 날짜 다음날 오전 6시 이전에 등록된 퀘스트만 조회된다")
        @Test
        fun `날짜 검색 조건이 둘 다 존재하는 경우, 시작 날짜 오전 6시 또는 이후부터 종료 날짜 다음날 오전 6시 이전에 등록된 퀘스트만 조회된다`() {
            //given
            val startDate = LocalDate.of(2022, 12, 1)
            val startDateTime = LocalDateTime.of(startDate, LocalTime.of(6, 0))

            val endDate = LocalDate.of(2022, 12, 10)
            val endDateTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(6, 0))

            val beforeStartConditionTime = LocalDateTime.of(startDate, LocalTime.of(5, 59))
            insertQuestWithCreatedTime(beforeStartConditionTime)
            val sameStartConditionTime = LocalDateTime.of(startDate, LocalTime.of(6, 0))
            insertQuestWithCreatedTime(sameStartConditionTime)
            val beforeEndConditionTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(5, 59))
            insertQuestWithCreatedTime(beforeEndConditionTime)
            val sameEndConditionTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(6, 0))
            insertQuestWithCreatedTime(sameEndConditionTime)
            val afterEndConditionTime = LocalDateTime.of(endDate.plusDays(1), LocalTime.of(6, 1))
            insertQuestWithCreatedTime(afterEndConditionTime)

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(list).noneMatch {
                quest -> quest.createdDate.isBefore(startDateTime) || quest.createdDate.isAfter(endDateTime)
            }
        }

        private fun insertQuestWithCreatedTime(createdTime: LocalDateTime) {
            val insertQuery = entityManager
                .createNativeQuery("insert into quest (quest_id, created_date, last_modified_date, description, user_quest_seq, state, title, type, user_id) values (default, ?, ?, '', 1, 'PROCEED', '', 'MAIN', ?)")
                .setParameter(3, user.id)
            insertQuery.setParameter(1, createdTime).setParameter(2, createdTime).executeUpdate()
        }

        @DisplayName("키워드 타입과 키워드 조건이 존재하는 경우, 해당 키워드가 포함된 퀘스트만 조회된다")
        @Test
        fun `키워드 타입과 키워드 조건이 존재하는 경우, 해당 키워드가 포함된 퀘스트만 조회된다`() {
            //given
            val keyword = "키워드"
            val keywordType = QuestSearchKeywordType.ALL.name

            val mustContainIds = mutableListOf<Long>()
            questRepository.save(Quest("$keyword 제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustContainIds.add(it.id) }
            questRepository.save(Quest("${keyword}제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustContainIds.add(it.id) }
            questRepository.save(Quest("제목", "$keyword 설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustContainIds.add(it.id) }

            val questHasDetail = Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)
            DetailQuest.of("$keyword 세부 제목", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, questHasDetail)
            questRepository.save(questHasDetail).let { mustContainIds.add(it.id) }
            val mustNotContainId = questRepository.save(Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).id

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("keywordType", keywordType)
                        .queryParam("keyword", keyword)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(list)
                .noneMatch { it.id == mustNotContainId }
                .allMatch { mustContainIds.contains(it.id) }
        }

        @DisplayName("키워드 타입이 제목인 경우, 제목에 키워드가 포함된 퀘스트만 조회된다")
        @Test
        fun `키워드 타입이 제목인 경우, 제목에 키워드가 포함된 퀘스트만 조회된다`() {
            //given
            val keyword = "키워드"
            val keywordType = QuestSearchKeywordType.TITLE.name

            val mustContainIds = mutableListOf<Long>()
            questRepository.save(Quest("$keyword 제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustContainIds.add(it.id) }
            questRepository.save(Quest("${keyword}제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustContainIds.add(it.id) }

            val mustNotContainIds = mutableListOf<Long>()
            questRepository.save(Quest("제목", "$keyword 설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }
            val questHasDetail = Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)
            DetailQuest.of("$keyword 세부 제목", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, questHasDetail)
            questRepository.save(questHasDetail).let { mustNotContainIds.add(it.id) }
            questRepository.save(Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("keywordType", keywordType)
                        .queryParam("keyword", keyword)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(list)
                .noneMatch { mustNotContainIds.contains(it.id) }
                .allMatch { mustContainIds.contains(it.id) }
        }

        @DisplayName("키워드 타입이 제목 + 설명인 경우, 제목이나 설명에 키워드가 포함된 퀘스트만 조회된다")
        @Test
        fun `키워드 타입이 제목 + 설명인 경우, 제목이나 설명에 키워드가 포함된 퀘스트만 조회된다`() {
            //given
            val keyword = "키워드"
            val keywordType = QuestSearchKeywordType.TITLE.name

            val mustContainIds = mutableListOf<Long>()
            val mustNotContainIds = mutableListOf<Long>()

            questRepository.save(Quest("$keyword 제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustContainIds.add(it.id) }
            questRepository.save(Quest("${keyword}제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustContainIds.add(it.id) }
            questRepository.save(Quest("제목", "$keyword 설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustContainIds.add(it.id) }

            val q = Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)
            DetailQuest.of("$keyword 세부 제목", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, q)
            questRepository.save(q).let { mustNotContainIds.add(it.id) }

            questRepository.save(Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("keywordType", keywordType)
                        .queryParam("keyword", keyword)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(list).noneMatch { mustNotContainIds.contains(it.id) }
            assertThat(list).allMatch { mustContainIds.contains(it.id) }
        }

        @DisplayName("키워드 타입이 설명인 경우, 설명에 키워드가 포함된 퀘스트만 조회된다")
        @Test
        fun `키워드 타입이 설명인 경우, 설명에 키워드가 포함된 퀘스트만 조회된다`() {
            //given
            val keyword = "키워드"
            val keywordType = QuestSearchKeywordType.TITLE.name

            val mustContainIds = mutableListOf<Long>()
            val mustNotContainIds = mutableListOf<Long>()

            questRepository.save(Quest("$keyword 제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }
            questRepository.save(Quest("${keyword}제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }

            questRepository.save(Quest("제목", "$keyword 설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustContainIds.add(it.id) }

            val q = Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)
            DetailQuest.of("$keyword 세부 제목", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, q)

            questRepository.save(q).let { mustNotContainIds.add(it.id) }
            questRepository.save(Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("keywordType", keywordType)
                        .queryParam("keyword", keyword)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(list).noneMatch { mustNotContainIds.contains(it.id) }
            assertThat(list).allMatch { mustContainIds.contains(it.id) }
        }

        @DisplayName("키워드 타입이 세부 퀘스트 제목인 경우, 세부 퀘스트 제목에 키워드가 포함된 퀘스트만 조회된다")
        @Test
        fun `키워드 타입이 세부 퀘스트 제목인 경우, 세부 퀘스트 제목에 키워드가 포함된 퀘스트만 조회된다`() {
            //given
            val keyword = "키워드"
            val keywordType = QuestSearchKeywordType.TITLE.name

            val mustContainIds = mutableListOf<Long>()
            val mustNotContainIds = mutableListOf<Long>()

            questRepository.save(Quest("$keyword 제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }
            questRepository.save(Quest("${keyword}제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }
            questRepository.save(Quest("제목", "$keyword 설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }

            val q = Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)
            DetailQuest.of("$keyword 세부 제목", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED, q)

            questRepository.save(q).let { mustContainIds.add(it.id) }
            questRepository.save(Quest("제목", "설명", user.id, 1, QuestState.PROCEED, QuestType.MAIN)).let { mustNotContainIds.add(it.id) }

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                        .queryParam("keywordType", keywordType)
                        .queryParam("keyword", keyword)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<RestPage<QuestResponse>>>(){})

            val data = result.data
            val list = data?.content

            assertThat(list).noneMatch { mustNotContainIds.contains(it.id) }
            assertThat(list).allMatch { mustContainIds.contains(it.id) }
        }

    }

    @DisplayName("퀘스트 조회 시")
    @Nested
    inner class QuestGetTest {

        private val urlPrefix = "${SERVER_ADDR}$port${uriPrefix}"

        @DisplayName("본인의 퀘스트 요청 시 퀘스트가 조회된다")
        @Test
        fun `본인의 퀘스트 요청 시 퀘스트가 조회된다`() {
            //given
            val savedQuest = questRepository.save(Quest("제목", "1", user.id, 1L, QuestState.PROCEED, QuestType.MAIN))
            val detailRequest = DetailQuest.of("detail", 3, DetailQuestType.COUNT, DetailQuestState.PROCEED, savedQuest)
            savedQuest.replaceDetailQuests(listOf(detailRequest))

            val questId = savedQuest.id
            val url = "$urlPrefix/$questId"

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("제목", "1", user.id, 1L, QuestState.PROCEED, QuestType.MAIN))
            val questId = savedQuest.id + 1
            val url = "$urlPrefix/$questId"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }


        @DisplayName("타인의 퀘스트 요청 시 NOT_FOUND가 반환된다")
        @Test
        fun `타인의 퀘스트 요청 시 NOT_FOUND가 반환된다`() {
            //given
            val savedQuest = questRepository.save(Quest("제목", "1", anotherUser.id, 1L, QuestState.PROCEED, QuestType.MAIN))
            val questId = savedQuest.id
            val url = "$urlPrefix/$questId"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val url = "$urlPrefix/$questId"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    get(url)
                        .useUserConfiguration()
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
    inner class QuestSaveTest {
        private val url = "${SERVER_ADDR}$port${uriPrefix}"

        @DisplayName("RequestBody 구문이 올바르지 않으면 BAD_REQUEST가 반환된다")
        @Test
        fun `RequestBody 구문이 올바르지 않으면 BAD_REQUEST가 반환된다`() {
            //given
            val errorMessage = MessageUtil.getMessage("exception.badRequest")
            val requestBody = "invalid body"

            //when
            val request = mvc
                .perform(
                    post(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val questRequest = QuestRequest("title", "des")
            val requestBody = om.writeValueAsString(questRequest)
            val numOfThreads = 3
            val seqSet = ConcurrentHashMap.newKeySet<Long>()

            //when
            runBlocking(Dispatchers.IO) {
                val user1 = User("", "", ProviderType.GOOGLE)
                user = userRepository.save(user1)

                val accessToken = jwtTokenProvider.createAccessToken(user.id)
                userToken = jwtTokenProvider.createAccessTokenCookie(accessToken)
                repeat(numOfThreads) {
                    launch {

                        val request = mvc
                            .perform(
                                post(url)
                                    .useUserConfiguration()
                                    .content(requestBody)
                            )
                        val body = request
                            .andExpect(status().isOk)
                            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)
            user.updateCoreTime(LocalTime.now().hour)

            //when
            val request = mvc
                .perform(
                    post(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)

            val now = LocalTime.now()
            user.updateCoreTime(now.plusHours(2).hour)

            //when
            val request = mvc
                .perform(
                    post(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    post(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    post(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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

        @DisplayName("현재 등록 횟수가 0이고, 목표 횟수가 1인 등록 업적이 있을 때, 해당 업적이 달성된다")
        @Test
        fun `현재 등록 횟수가 0이고, 목표 횟수가 1인 등록 업적이 있을 때, 해당 업적이 달성된다`() {
            //given
            val targetAchievement = Achievement("t", "d", AchievementType.QUEST_REGISTRATION, 1)
            achievementRepository.save(targetAchievement)
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)

            //when
            mvc.post(url) {
                useUserConfiguration()
                content = requestBody
            }.andExpect { status { isOk() } }

            //then
            val achieveLogs = achievementAchieveLogRepository.findAll()
            assertThat(achieveLogs).anyMatch { it.achievement == targetAchievement }
        }

        @DisplayName("현재 연속 등록일이 0이고, 목표 값이 1인 연속 등록 업적이 있으면, 해당 업적이 달성된다")
        @Test
        fun `현재 연속 등록일이 0이고, 목표 값이 1인 연속 등록 업적이 있으면, 해당 업적이 달성된다`() {
            //given
            val targetAchievement = Achievement("t", "d", AchievementType.QUEST_CONTINUOUS_REGISTRATION, 1)
            achievementRepository.save(targetAchievement)
            val questRequest = QuestRequest("t", "d", mutableListOf(DetailRequest("dt", DetailQuestType.COUNT, 1)))
            val requestBody = om.writeValueAsString(questRequest)

            //when
            mvc.post(url) {
                useUserConfiguration()
                content = requestBody
            }.andExpect { status { isOk() } }

            //then
            val achieveLogs = achievementAchieveLogRepository.findAll()
            assertThat(achieveLogs).anyMatch { it.achievement == targetAchievement }
        }
    }

    @Nested
    @DisplayName("퀘스트 수정 시")
    inner class QuestUpdateTest {

        private val urlPrefix = "${SERVER_ADDR}$port${uriPrefix}"

        @DisplayName("Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            //given
            val url = "$urlPrefix/$questId"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "$urlPrefix/${savedQuest.id}"

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
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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

        @DisplayName("다른 유저의 퀘스트를 요청하면 NOT_FOUND가 반환된다")
        @Test
        fun `다른 유저의 퀘스트를 요청하면 NOT_FOUND가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", anotherUser.id, 1L, QuestState.PROCEED, QuestType.SUB))

            val url = "$urlPrefix/${savedQuest.id}"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val url = "$urlPrefix/10000"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.FAIL, QuestType.SUB))

            val url = "$urlPrefix/${savedQuest.id}"
            val errorMessage = MessageUtil.getMessage("quest.error.not-proceed")

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.MAIN))

            val url = "$urlPrefix/${savedQuest.id}"

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))

            val url = "$urlPrefix/${savedQuest.id}"

            val detailRequest = DetailRequest("update", DetailQuestType.COUNT, 1)
            val questRequest = QuestRequest("update", "update", mutableListOf(detailRequest))

            val requestBody = om.writeValueAsString(questRequest)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val url = "${SERVER_ADDR}$port${uriPrefix}/$questId/delete"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val data = result.data
            val error = result.errorResponse

            assertThat(data).isNull()
            assertThat(error?.message).isEqualTo(errorMessage)
        }

        @DisplayName("다른 유저의 퀘스트를 요청하면 NOT_FOUND가 반환된다")
        @Test
        fun `다른 유저의 퀘스트를 요청하면 NOT_FOUND가 반환된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", anotherUser.id, 1L, QuestState.PROCEED, QuestType.SUB))

            val url = "${SERVER_ADDR}$port${uriPrefix}/${savedQuest.id}/delete"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val url = "${SERVER_ADDR}$port${uriPrefix}/10000/delete"
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isNotFound)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${uriPrefix}/${savedQuest.id}/delete"

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val result = om.readValue(body, object: TypeReference<ResponseData<QuestResponse>>(){})

            val error = result.errorResponse

            assertThat(savedQuest.state).isEqualTo(QuestState.DELETE)
            assertThat(error).isNull()
        }

        @DisplayName("엘라스틱서치 문서가 삭제된다")
        @Test
        fun `엘라스틱서치 문서가 삭제된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${uriPrefix}/${savedQuest.id}/delete"

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

            val questId = savedQuest.id
            val findDocument = questIndexRepository.findById(questId)

            assertThat(findDocument).isEmpty()
        }
    }


    @DisplayName("퀘스트 완료 시")
    @Nested
    inner class QuestCompleteTest {
        private val urlFormat = "${SERVER_ADDR}$port${uriPrefix}/%s/complete"

        @DisplayName("Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            val url = urlFormat.format(questId)
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.DELETE, QuestType.SUB))
            val url = urlFormat.format(savedQuest.id)
            val errorMessage = MessageUtil.getMessage("quest.error.deleted")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.FAIL, QuestType.SUB))
            val url = urlFormat.format(savedQuest.id)
            val errorMessage = MessageUtil.getMessage("quest.error.not-proceed")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val detailRequest = DetailQuest.of("detail", 1, DetailQuestType.CHECK, DetailQuestState.PROCEED , savedQuest)
            savedQuest.replaceDetailQuests(listOf(detailRequest))

            val url = urlFormat.format(savedQuest.id)
            val errorMessage = MessageUtil.getMessage("quest.error.complete.detail")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = urlFormat.format(savedQuest.id)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.MAIN))
            val url = urlFormat.format(savedQuest.id)

            val ops = redisTemplate.boundHashOps<String, Long>(redisKeyProperties.settings)

            val questClearExp = ops[redisKeyProperties.questClearExp]!!
            val questClearGold = ops[redisKeyProperties.questClearGold]!!

            val beforeExp = user.exp
            val beforeGold = user.gold

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

            assertThat(user.exp).isEqualTo(beforeExp + questClearExp*2)
            assertThat(user.gold).isEqualTo(beforeGold + questClearGold*2)
        }

        @DisplayName("서브 퀘스트 완료 시 1배의 경험치와 골드를 획득한다")
        @Test
        fun `서브 퀘스트 완료 시 1배의 경험치와 골드를 획득한다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = urlFormat.format(savedQuest.id)

            val ops = redisTemplate.boundHashOps<String, Long>(redisKeyProperties.settings)

            val questClearExp = ops[redisKeyProperties.questClearExp]!!
            val questClearGold = ops[redisKeyProperties.questClearGold]!!

            val beforeExp = user.exp
            val beforeGold = user.gold

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

            assertThat(user.exp).isEqualTo(beforeExp + questClearExp)
            assertThat(user.gold).isEqualTo(beforeGold + questClearGold)
        }

        @DisplayName("로그 테이블에 데이터가 등록된다")
        @Test
        fun `로그 테이블에 데이터가 등록된다`() {
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = urlFormat.format(savedQuest.id)

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

            val allQuestLog = questLogRepository.findAll()
            assertThat(allQuestLog).anyMatch { log -> log.questId == savedQuest.id && log.state == QuestState.COMPLETE }
        }

        @DisplayName("현재 총 골드 획득이 0이고, 목표값이 1인 골드 획득 업적이 있을 때, 해당 업적이 달성된다")
        @Test
        fun `현재 총 골드 획득이 0이고, 목표값이 1인 골드 획득 업적이 있을 때, 해당 업적이 달성된다`() {
            //given
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = urlFormat.format(savedQuest.id)
            val targetAchievement = Achievement("t", "d", AchievementType.GOLD_EARN, 1)
            achievementRepository.save(targetAchievement)

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val achieveLogs = achievementAchieveLogRepository.findAll()
            assertThat(achieveLogs).anyMatch { it.achievement == targetAchievement }
        }

        @DisplayName("현재 완료 횟수가 0이고, 목표값이 1인 완료 업적이 있을 때, 해당 업적이 달성된다")
        @Test
        fun `현재 완료 횟수가 0이고, 목표값이 1인 완료 업적이 있을 때, 해당 업적이 달성된다`() {
            //given
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = urlFormat.format(savedQuest.id)
            val targetAchievement = Achievement("t", "d", AchievementType.QUEST_COMPLETION, 1)
            achievementRepository.save(targetAchievement)

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val achieveLogs = achievementAchieveLogRepository.findAll()
            assertThat(achieveLogs).anyMatch { it.achievement == targetAchievement }
        }

        @DisplayName("현재 연속 완료일이 0이고, 목표 값이 1인 연속 완료 업적이 있으면, 해당 업적이 달성된다")
        @Test
        fun `현재 연속 완료일이 0이고, 목표 값이 1인 연속 완료 업적이 있으면, 해당 업적이 달성된다`() {
            //given
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = urlFormat.format(savedQuest.id)
            val targetAchievement = Achievement("t", "d", AchievementType.QUEST_CONTINUOUS_COMPLETION, 1)
            achievementRepository.save(targetAchievement)

            //when
            mvc.patch(url) {
                useUserConfiguration()
            }.andExpect { status { isOk() } }

            //then
            val achieveLogs = achievementAchieveLogRepository.findAll()
            assertThat(achieveLogs).anyMatch { it.achievement == targetAchievement }
        }
    }

    @DisplayName("퀘스트 포기 시")
    @Nested
    inner class QuestDiscardTest {

        @DisplayName("Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable이 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            val url = "${SERVER_ADDR}$port${uriPrefix}/$questId/discard"
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.DELETE, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${uriPrefix}/${savedQuest.id}/discard"
            val errorMessage = MessageUtil.getMessage("quest.error.deleted")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.FAIL, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${uriPrefix}/${savedQuest.id}/discard"
            val errorMessage = MessageUtil.getMessage("quest.error.not-proceed")

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isBadRequest)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${uriPrefix}/${savedQuest.id}/discard"

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            val body = request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
            val savedQuest = questRepository.save(Quest("title", "desc", user.id, 1L, QuestState.PROCEED, QuestType.SUB))
            val url = "${SERVER_ADDR}$port${uriPrefix}/${savedQuest.id}/discard"

            //when
            val request = mvc
                .perform(
                    patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .cookie(userToken)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

            val allQuestLog = questLogRepository.findAll()
            assertThat(allQuestLog).anyMatch { log -> log.questId == savedQuest.id && log.state == QuestState.DISCARD }
        }
    }

    // TODO 테스트 코드 리팩터링
    @DisplayName("세부 퀘스트 카운트 변경 시")
    @Nested
    inner class TestUpdateDetailQuestCount {
        private val url = "$SERVER_ADDR$port$uriPrefix/%s/details/%s"

        private lateinit var quest: Quest
        private lateinit var proceedDetailQuest: DetailQuest
        private lateinit var completedDetailQuest: DetailQuest
        private lateinit var questOfOtherUser: Quest
        private lateinit var detailQuestOfOtherUser: DetailQuest

        @BeforeEach
        fun beforeEach() {
            val seq = questRepository.getNextSeqByUserId(user.id)
            quest = Quest("t", "d", user.id, seq, type = QuestType.MAIN)
            proceedDetailQuest = DetailQuest.of("t", 10, DetailQuestType.COUNT, DetailQuestState.PROCEED, quest)
            completedDetailQuest = DetailQuest.of("t", 10, 10, DetailQuestType.COUNT, DetailQuestState.COMPLETE, quest)
            quest.replaceDetailQuests(listOf(proceedDetailQuest, completedDetailQuest))
            questRepository.save(quest)

            val anotherSeq = questRepository.getNextSeqByUserId(anotherUser.id)
            questOfOtherUser = Quest("t", "d", anotherUser.id, anotherSeq, type = QuestType.MAIN)
            detailQuestOfOtherUser = DetailQuest.of("t", 10, DetailQuestType.COUNT, DetailQuestState.PROCEED, questOfOtherUser)
            questOfOtherUser.replaceDetailQuests(listOf(detailQuestOfOtherUser))
            questRepository.save(questOfOtherUser)
        }

        @DisplayName("Path Variable의 quest id가 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable의 quest id가 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(questId: Any) {
            //given
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url.format(questId, 1))
                        .useUserConfiguration()
                )

            //then
            request
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").value(errorMessage))
        }

        @DisplayName("Path Variable의 detail quest id가 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다")
        @ArgumentsSource(QuestApiControllerUnitTest.InvalidLongSources::class)
        @ParameterizedTest(name = "{0} 값이 들어오면 BAD_REQUEST가 반환한다")
        fun `Path Variable의 detail quest id가 유효한 Long 타입이 아니면 BAD_REQUEST가 반환된다`(detailQuestId: Any) {
            //given
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url.format(1, detailQuestId))
                        .useUserConfiguration()
                )

            //then
            request
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").value(errorMessage))
        }

        @DisplayName("요청 DTO 카운트가 256이면 BAD_REQUEST 응답 코드와 함께 Validation 에러가 반환된다")
        @Test
        fun `요청 DTO 카운트가 256이면 BAD_REQUEST 응답 코드와 함께 Validation 에러가 반환된다`() {
            //given
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            val interactRequest = DetailInteractRequest(256)
            val requestBody = om.writeValueAsString(interactRequest)

            val bindingMessages = listOf(
                MessageUtil.getMessage("Range.details.count"),
            )

            //when
            val request = mvc
                .perform(
                    patch(url.format(1, 1))
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            request
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").value(errorMessage))
                .andExpect(jsonPath("$.errorResponse.errors").value(Matchers.hasValue(bindingMessages)))
        }


        @DisplayName("요청 DTO 카운트가 255면 OK가 반환된다")
        @Test
        fun `요청 DTO 카운트가 255면 OK가 반환된다`() {
            //given
            val interactRequest = DetailInteractRequest(255)
            val requestBody = om.writeValueAsString(interactRequest)

            //when
            val request = mvc
                .perform(
                    patch(url.format(quest.id, proceedDetailQuest.id))
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @DisplayName("요청 DTO 카운트가 0이면 OK가 반환된다")
        @Test
        fun `요청 DTO 카운트가 0이면 OK가 반환된다`() {
            //given
            val interactRequest = DetailInteractRequest(0)
            val requestBody = om.writeValueAsString(interactRequest)

            //when
            val request = mvc
                .perform(
                    patch(url.format(quest.id, proceedDetailQuest.id))
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            request
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.errorResponse").doesNotExist())
        }

        @DisplayName("요청 DTO 카운트가 -1이면 BAD_REQUEST가 반환된다")
        @Test
        fun `요청 DTO 카운트가 -1이면 BAD_REQUEST가 반환된다`() {
            //given
            val interactRequest = DetailInteractRequest(-1)
            val requestBody = om.writeValueAsString(interactRequest)
            val errorMessage = MessageUtil.getMessage("exception.badRequest")
            val bindingMessages = listOf(MessageUtil.getMessage("Range.details.count"))

            //when
            val request = mvc
                .perform(
                    patch(url.format(quest.id, proceedDetailQuest.id))
                        .useUserConfiguration()
                        .content(requestBody)
                )

            //then
            request
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").value(errorMessage))
                .andExpect(jsonPath("$.errorResponse.errors").value(Matchers.hasValue(bindingMessages)))
        }

        @DisplayName("다른 유저의 퀘스트를 요청하면 NOT_FOUND가 반환된다")
        @Test
        fun `다른 유저의 퀘스트를 요청하면 NOT_FOUND가 반환된다`() {
            //given
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            //when
            val request = mvc
                .perform(
                    patch(url.format(questOfOtherUser.id, detailQuestOfOtherUser.id))
                        .useUserConfiguration()
                )

            //then
            request
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").value(errorMessage))
        }

        @DisplayName("존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다")
        @Test
        fun `존재하지 않는 퀘스트 요청 시 NOT_FOUND가 반환된다`() {
            //given
            val errorMessage = MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest"))

            //when
            val request = mvc
                .perform(
                    patch(url.format(10000, proceedDetailQuest.id))
                        .useUserConfiguration()
                )

            //then
            request
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").value(errorMessage))
        }

        @DisplayName("퀘스트에 포함되지 않은 세부 퀘스트 요청 시 BAD_REQUEST가 반환된다")
        @Test
        fun `퀘스트에 포함되지 않은 세부 퀘스트 요청 시 BAD_REQUEST가 반환된다`() {
            //given
            val errorMessage = MessageUtil.getMessage("exception.badRequest")

            //when
            val request = mvc
                .perform(
                    patch(url.format(quest.id, detailQuestOfOtherUser.id))
                        .useUserConfiguration()
                )

            //then
            request
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").value(errorMessage))
        }

        @DisplayName("진행중인 퀘스트가 아니라면 BAD_REQUEST가 반환된다")
        @Test
        fun `진행중인 퀘스트가 아니라면 BAD_REQUEST가 반환된다`() {
            //given
            quest.failQuest()
            questRepository.flush()
            val errorMessage = MessageUtil.getMessage("quest.error.not-proceed")

            //when
            val request = mvc
                .perform(
                    patch(url.format(quest.id, proceedDetailQuest.id))
                        .useUserConfiguration()
                )

            //then
            request
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errorResponse.message").value(errorMessage))
        }

        @DisplayName("요청 카운트 값이 존재하고")
        @Nested
        inner class ChangeCountTest {

            @DisplayName("값이 목표 카운트보다 크거나 같으면 목표 카운트로 변경하고 완료 상태가 된다")
            @Test
            fun `값이 목표 카운트보다 크거나 같으면 목표 카운트로 변경하고 완료 상태가 된다`() {
                //given
                val biggerCount = proceedDetailQuest.targetCount + 1
                val interactRequest = DetailInteractRequest(biggerCount)
                val requestBody = om.writeValueAsString(interactRequest)

                //when
                val request = mvc
                    .perform(
                        patch(url.format(quest.id, proceedDetailQuest.id))
                            .useUserConfiguration()
                            .content(requestBody)
                    )

                //then
                request
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.data.count").value(proceedDetailQuest.targetCount))
                    .andExpect(jsonPath("$.data.state").value(QuestState.COMPLETE.name))
            }

            @DisplayName("카운트가 목표 카운트보다 작으면 진행 상태로 변경한다")
            @Test
            fun `카운트가 목표 카운트 보다 작다면 진행 상태로 변경한다`() {
                //given
                val smallerCount = completedDetailQuest.targetCount - 1
                val interactRequest = DetailInteractRequest(smallerCount)
                val requestBody = om.writeValueAsString(interactRequest)

                //when
                val request = mvc
                    .perform(
                        patch(url.format(quest.id, completedDetailQuest.id))
                            .useUserConfiguration()
                            .content(requestBody)
                    )

                //then
                request
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.data.count").value(completedDetailQuest.targetCount-1))
                    .andExpect(jsonPath("$.data.state").value(QuestState.PROCEED.name))
            }
        }

        @DisplayName("요청 카운트 값이 없고")
        @Nested
        inner class AddCountTest {

            @DisplayName("세부 퀘스트가 완료 상태라면 카운트를 0으로 초기화 하고 진행 상태로 변경한다")
            @Test
            fun `세부 퀘스트가 완료 상태라면 카운트를 0으로 초기화 하고 진행 상태로 변경한다`() {
                //when
                val request = mvc
                    .perform(
                        patch(url.format(quest.id, completedDetailQuest.id))
                            .useUserConfiguration()
                    )

                //then
                request
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.data.count").value(0))
                    .andExpect(jsonPath("$.data.state").value(QuestState.PROCEED.name))
            }

            @DisplayName("세부 퀘스트가 완료 상태가 아닐때 카운트를 1 증가시킨다")
            @Test
            fun `세부 퀘스트가 완료 상태가 아니면 카운트를 1 증가시킨다`() {
                //given
                val beforeCount = proceedDetailQuest.count

                //when
                val request = mvc
                    .perform(
                        patch(url.format(quest.id, proceedDetailQuest.id))
                            .useUserConfiguration()
                    )

                //then
                request
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.data.count").value(beforeCount + 1))
                    .andExpect(jsonPath("$.data.state").value(QuestState.PROCEED.name))
            }

            @DisplayName("1 증가한 카운트가 목표 카운트라면 완료 상태로 변경한다")
            @Test
            fun `1 증가한 카운트가 목표 카운트라면 완료 상태로 변경한다`() {
                //given
                proceedDetailQuest.updateCountAndState(proceedDetailQuest.targetCount-1)
                questRepository.flush()

                //when
                val request = mvc
                    .perform(
                        patch(url.format(quest.id, proceedDetailQuest.id))
                            .useUserConfiguration()
                    )

                //then
                request
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.data.state").value(QuestState.COMPLETE.name))
            }
        }
    }
}