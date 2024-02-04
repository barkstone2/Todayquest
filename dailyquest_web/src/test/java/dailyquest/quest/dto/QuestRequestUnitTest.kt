package dailyquest.quest.dto

import dailyquest.common.MessageUtil
import dailyquest.quest.entity.*
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.mockito.Answers
import org.mockito.ArgumentMatchers
import org.mockito.MockedStatic
import org.mockito.Mockito
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@DisplayName("퀘스트 리퀘스트 DTO 유닛 테스트")
class QuestRequestUnitTest {

    private lateinit var messageUtil: MockedStatic<MessageUtil>

    @BeforeEach
    fun init() {
        messageUtil = Mockito.mockStatic(MessageUtil::class.java)
        Mockito.`when`(MessageUtil.getMessage(ArgumentMatchers.anyString())).thenReturn("")
        Mockito.`when`(MessageUtil.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn("")
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }

    @DisplayName("생성자에 값을 넣으면 값이 올바르게 담긴다")
    @Test
    fun `생성자에 값을 넣으면 값이 올바르게 담긴다`() {
        //given
        val title = "title init"
        val description = "description init"
        val detailTitle = "detail title"
        val details = mutableListOf(DetailRequest(detailTitle, DetailQuestType.CHECK, 1))

        //when
        val questRequest = QuestRequest(title, description, details)

        //then
        assertThat(questRequest.title).isEqualTo(title)
        assertThat(questRequest.description).isEqualTo(description)
        assertThat(questRequest.details).isEqualTo(details)
        assertThat(questRequest.details[0]).isEqualTo(details[0])
        assertThat(questRequest.details[0].title).isEqualTo(detailTitle)
    }

    @DisplayName("toMainQuest 호출 시 MAIN 타입으로 변경된다")
    @Test
    fun `toMainQuest 호출 시 MAIN 타입으로 변경된다`() {
        //given
        val questRequest = QuestRequest("title", "description")
        val typeField = QuestRequest::class.java.getDeclaredField("type")
        typeField.isAccessible = true
        val beforeType = typeField.get(questRequest) as QuestType

        //when
        questRequest.toMainQuest()

        //then
        val afterType = typeField.get(questRequest) as QuestType
        assertThat(beforeType).isEqualTo(QuestType.SUB)
        assertThat(afterType).isEqualTo(QuestType.MAIN)
    }

    @DisplayName("mapToEntity 호출 시 Quest 엔티티로 변환된다")
    @Test
    fun `mapToEntity 호출 시 Quest 엔티티로 변환된다`() {
        //given
        val questRequest = QuestRequest("title init", "description init")

        //when
        val entity = questRequest.mapToEntity(1L, UserInfo("", "", ProviderType.GOOGLE))

        //then
        assertThat(entity.title).isEqualTo(questRequest.title)
        assertThat(entity.description).isEqualTo(questRequest.description)
        assertThat(entity.type).isEqualTo(QuestType.SUB)
        assertThat(entity.state).isEqualTo(QuestState.PROCEED)
    }

    @DisplayName("데드라인 범위 확인 시")
    @Nested
    inner class TestDeadLineRange {

        @DisplayName("현재 시점이 오늘 오전 6시 이전이면 deadLine 필드의 범위를 오늘 오전 6시를 기준으로 판단한다")
        @Test
        fun `현재 시점이 오늘 오전 6시 이전이면 deadLine 필드의 범위를 오늘 오전 6시를 기준으로 판단한다`() {
            //given
            val nowDate = LocalDate.now()
            val mockNow = LocalDateTime.of(nowDate, LocalTime.of(5, 59))

            val failDeadLine1 = LocalDateTime.of(nowDate, LocalTime.of(5, 59))
            val failDeadLine2 = LocalDateTime.of(nowDate, LocalTime.of(6, 0))
            val failDeadLine3 = LocalDateTime.of(nowDate, LocalTime.of(6, 1))

            Mockito.mockStatic(LocalDateTime::class.java, Answers.CALLS_REAL_METHODS).use {
                Mockito.`when`(LocalDateTime.now()).thenReturn(mockNow)

                val request1 = QuestRequest("t", "d", deadLine = failDeadLine1)
                val request2 = QuestRequest("t", "d", deadLine = failDeadLine2)
                val request3 = QuestRequest("t", "d", deadLine = failDeadLine3)

                //when
                val run1 = { request1.checkRangeOfDeadLine() }
                val run2 = { request2.checkRangeOfDeadLine() }
                val run3 = { request3.checkRangeOfDeadLine() }

                //then
                assertThatThrownBy(run1).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run2).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run3).isInstanceOf(IllegalArgumentException::class.java)
            }
        }

        @DisplayName("현재 시점이 오늘 오전 6시 보다 같거나 나중이면, deadLine 필드의 범위를 다음날 오전 6시를 기준으로 판단한다")
        @Test
        fun `현재 시점이 오늘 오전 6시 보다 같거나 나중이면, deadLine 필드의 범위를 다음날 오전 6시를 기준으로 판단한다`() {
            //given
            val nowDate = LocalDate.now()
            val mockNow = LocalDateTime.of(nowDate, LocalTime.of(6, 0))

            val passDeadLine1 = LocalDateTime.of(nowDate, LocalTime.of(7, 0))
            val passDeadLine2 = LocalDateTime.of(nowDate.plusDays(1L), LocalTime.of(5, 0))
            val failDeadLine1 = LocalDateTime.of(nowDate.plusDays(1L), LocalTime.of(5, 59))
            val failDeadLine2 = LocalDateTime.of(nowDate.plusDays(1L), LocalTime.of(6, 0))
            val failDeadLine3 = LocalDateTime.of(nowDate.plusDays(1L), LocalTime.of(6, 1))

            Mockito.mockStatic(LocalDateTime::class.java, Answers.CALLS_REAL_METHODS).use {
                Mockito.`when`(LocalDateTime.now()).thenReturn(mockNow)

                val request1 = QuestRequest("t", "d", deadLine = passDeadLine1)
                val request2 = QuestRequest("t", "d", deadLine = passDeadLine2)
                val request3 = QuestRequest("t", "d", deadLine = failDeadLine1)
                val request4 = QuestRequest("t", "d", deadLine = failDeadLine2)
                val request5 = QuestRequest("t", "d", deadLine = failDeadLine3)

                //when
                val run1 = { request1.checkRangeOfDeadLine() }
                val run2 = { request2.checkRangeOfDeadLine() }
                val run3 = { request3.checkRangeOfDeadLine() }
                val run4 = { request4.checkRangeOfDeadLine() }
                val run5 = { request5.checkRangeOfDeadLine() }

                //then
                assertDoesNotThrow(run1)
                assertDoesNotThrow(run2)
                assertThatThrownBy(run3).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run4).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run5).isInstanceOf(IllegalArgumentException::class.java)
            }
        }

        @DisplayName("deadLine 필드 값이 현재 시점 +5분 보다 이르거나 같으면 오류가 발생한다")
        @Test
        fun `deadLine 필드 값이 현재 시점 +5분 보다 이르거나 같으면 오류가 발생한다`() {
            //given
            val nowDate = LocalDate.now()
            val mockNow = LocalDateTime.of(nowDate, LocalTime.of(5, 0))

            val failDeadLine1 = LocalDateTime.of(nowDate, LocalTime.of(4, 59))
            val failDeadLine2 = LocalDateTime.of(nowDate, LocalTime.of(5, 0))
            val failDeadLine3 = LocalDateTime.of(nowDate, LocalTime.of(5, 4))
            val failDeadLine4 = LocalDateTime.of(nowDate, LocalTime.of(5, 5))
            val passDeadLine1 = LocalDateTime.of(nowDate, LocalTime.of(5, 6))

            Mockito.mockStatic(LocalDateTime::class.java, Answers.CALLS_REAL_METHODS).use {
                Mockito.`when`(LocalDateTime.now()).thenReturn(mockNow)

                val request1 = QuestRequest("t", "d", deadLine = failDeadLine1)
                val request2 = QuestRequest("t", "d", deadLine = failDeadLine2)
                val request3 = QuestRequest("t", "d", deadLine = failDeadLine3)
                val request4 = QuestRequest("t", "d", deadLine = failDeadLine4)
                val request5 = QuestRequest("t", "d", deadLine = passDeadLine1)

                //when
                val run1 = { request1.checkRangeOfDeadLine() }
                val run2 = { request2.checkRangeOfDeadLine() }
                val run3 = { request3.checkRangeOfDeadLine() }
                val run4 = { request4.checkRangeOfDeadLine() }
                val run5 = { request5.checkRangeOfDeadLine() }

                //then
                assertThatThrownBy(run1).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run2).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run3).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run4).isInstanceOf(IllegalArgumentException::class.java)
                assertDoesNotThrow(run5)
            }

        }

        @DisplayName("deadLine 필드 값이 다음 초기화 시간 -5분보다 같거나 나중이면 오류가 발생한다")
        @Test
        fun `deadLine 필드 값이 다음 초기화 시간 -5분보다 같거나 나중이면 오류가 발생한다`() {
            //given
            val nowDate = LocalDate.now()
            val mockNow = LocalDateTime.of(nowDate, LocalTime.of(5, 0))

            val passDeadLine1 = LocalDateTime.of(nowDate, LocalTime.of(5, 54))
            val failDeadLine1 = LocalDateTime.of(nowDate, LocalTime.of(5, 55))
            val failDeadLine2 = LocalDateTime.of(nowDate, LocalTime.of(5, 59))
            val failDeadLine3 = LocalDateTime.of(nowDate, LocalTime.of(6, 0))
            val failDeadLine4 = LocalDateTime.of(nowDate, LocalTime.of(6, 1))

            Mockito.mockStatic(LocalDateTime::class.java, Answers.CALLS_REAL_METHODS).use {
                Mockito.`when`(LocalDateTime.now()).thenReturn(mockNow)

                val request1 = QuestRequest("t", "d", deadLine = passDeadLine1)
                val request2 = QuestRequest("t", "d", deadLine = failDeadLine1)
                val request3 = QuestRequest("t", "d", deadLine = failDeadLine2)
                val request4 = QuestRequest("t", "d", deadLine = failDeadLine3)
                val request5 = QuestRequest("t", "d", deadLine = failDeadLine4)

                //when
                val run1 = { request1.checkRangeOfDeadLine() }
                val run2 = { request2.checkRangeOfDeadLine() }
                val run3 = { request3.checkRangeOfDeadLine() }
                val run4 = { request4.checkRangeOfDeadLine() }
                val run5 = { request5.checkRangeOfDeadLine() }

                //then
                assertDoesNotThrow(run1)
                assertThatThrownBy(run2).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run3).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run4).isInstanceOf(IllegalArgumentException::class.java)
                assertThatThrownBy(run5).isInstanceOf(IllegalArgumentException::class.java)
            }
        }

    }

}