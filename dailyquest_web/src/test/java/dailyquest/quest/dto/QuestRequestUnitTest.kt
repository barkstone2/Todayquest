package dailyquest.quest.dto

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.MockedStatic
import org.mockito.Mockito
import dailyquest.common.MessageUtil
import dailyquest.quest.entity.*
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

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


    @DisplayName("deadLine 필드의 범위가 유효하지 않으면 오류가 발생한다")
    @Test
    fun `deadLine 필드의 범위가 유효하지 않으면 오류가 발생한다`() {
        //given
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        val resetTime = LocalTime.of(0, 0)

        val beforeBoundaryTime = now.minus(1, ChronoUnit.MINUTES)
        val afterBoundaryTime = LocalDateTime.of(today.plus(1, ChronoUnit.DAYS), resetTime)

        val request1 = QuestRequest("t", "d", null, beforeBoundaryTime)
        val request2 = QuestRequest("t", "d", null, afterBoundaryTime)

        //when
        val run1 = { request1.checkRangeOfDeadLine(resetTime) }
        val run2 = { request2.checkRangeOfDeadLine(resetTime) }

        //then
        assertThatThrownBy(run1).isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy(run2).isInstanceOf(IllegalArgumentException::class.java)
    }

    @DisplayName("deadLine 필드의 범위가 유효하면 오류가 발생하지 않는다")
    @Test
    fun `deadLine 필드의 범위가 유효하면 오류가 발생하지 않는다`() {
        //given
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val resetTime = LocalTime.of(0, 0)

        val beforeBoundaryTime = now.plus(10, ChronoUnit.MINUTES)
        val afterBoundaryTime = LocalDateTime.of(today.plus(1, ChronoUnit.DAYS), resetTime).minus(10, ChronoUnit.MINUTES)

        val request1 = QuestRequest("t", "d", null, null)
        val request2 = QuestRequest("t", "d", null, beforeBoundaryTime)
        val request3 = QuestRequest("t", "d", null, afterBoundaryTime)

        //when
        val run1 = { request1.checkRangeOfDeadLine(resetTime) }
        val run2 = { request2.checkRangeOfDeadLine(resetTime) }
        val run3 = { request3.checkRangeOfDeadLine(resetTime) }

        //then
        assertThatCode(run1).doesNotThrowAnyException()
        assertThatCode(run2).doesNotThrowAnyException()
        assertThatCode(run3).doesNotThrowAnyException()
    }

}