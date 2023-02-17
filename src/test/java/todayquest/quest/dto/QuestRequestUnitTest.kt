package todayquest.quest.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import todayquest.quest.entity.*
import todayquest.user.entity.ProviderType
import todayquest.user.entity.UserInfo

@DisplayName("퀘스트 리퀘스트 DTO 유닛 테스트")
class QuestRequestUnitTest {

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
}