package todayquest.quest.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import todayquest.quest.entity.*
import todayquest.user.entity.ProviderType
import todayquest.user.entity.UserInfo
import java.time.LocalDateTime

@DisplayName("퀘스트 리스폰스 DTO 테스트")
class QuestResponseUnitTest {

    @DisplayName("생성자에 값을 넣으면 값이 올바르게 담긴다")
    @Test
    fun `생성자에 값을 넣으면 값이 올바르게 담긴다`() {
        //given
        val questId = 1L
        val title = "title init"
        val description = "description init"
        val seq = 2L
        val state = QuestState.DISCARD
        val lastModifiedDate = LocalDateTime.now()
        val canComplete = false
        val type = QuestType.MAIN
        val detailId = 1L
        val detailTitle = "title"
        val detailQuests = listOf(DetailResponse(detailId, detailTitle))

        //when
        val questResponse = QuestResponse(
            questId = questId,
            title = title,
            description = description,
            seq = seq,
            state = state,
            lastModifiedDate = lastModifiedDate,
            detailQuests = detailQuests,
            canComplete = canComplete,
            type = type
        )

        //then
        assertThat(questResponse.questId).isEqualTo(questId)
        assertThat(questResponse.title).isEqualTo(title)
        assertThat(questResponse.description).isEqualTo(description)
        assertThat(questResponse.seq).isEqualTo(seq)
        assertThat(questResponse.state).isEqualTo(state)
        assertThat(questResponse.lastModifiedDate).isEqualTo(lastModifiedDate)
        assertThat(questResponse.detailQuests).isEqualTo(detailQuests)
        assertThat(questResponse.canComplete).isEqualTo(canComplete)
        assertThat(questResponse.type).isEqualTo(type)
        assertThat(questResponse.detailQuests?.get(0)?.id).isEqualTo(detailId)
        assertThat(questResponse.detailQuests?.get(0)?.title).isEqualTo(detailTitle)
    }

    @DisplayName("createDto 메서드 호출시 QuestResponse 로 변환된다")
    @Test
    fun `createDto 메서드 호출시 QuestResponse 로 변환된다`() {
        //given
        val title = "title init"
        val description = "description init"
        val seq = 2L
        val state = QuestState.DISCARD
        val type = QuestType.MAIN

        val detailTitle = "detail title"
        val detailType = DetailQuestType.COUNT
        val detailState = DetailQuestState.PROCEED

        val quest = Quest(
            title = title,
            description = description,
            seq = seq,
            state = state,
            user = UserInfo("", "", ProviderType.GOOGLE),
            type = type
        )

        val details = mutableListOf(DetailQuest(detailTitle, 15, detailType, detailState, quest))

        val detailQuests = Quest::class.java.getDeclaredField("_detailQuests")
        detailQuests.isAccessible = true
        detailQuests.set(quest, details)

        //when
        val questResponse = QuestResponse.createDto(quest)

        //then
        assertThat(questResponse.title).isEqualTo(title)
        assertThat(questResponse.description).isEqualTo(description)
        assertThat(questResponse.seq).isEqualTo(seq)
        assertThat(questResponse.state).isEqualTo(state)
        assertThat(questResponse.type).isEqualTo(type)
        assertThat(questResponse.detailQuests?.get(0)?.title).isEqualTo(detailTitle)
        assertThat(questResponse.detailQuests?.get(0)?.type).isEqualTo(detailType)
        assertThat(questResponse.detailQuests?.get(0)?.state).isEqualTo(detailState)
    }

    @DisplayName("JSON Deserialize 가 정상적으로 수행된다")
    @Test
    fun `JSON Deserialize 가 정상적으로 수행된다`() {
        //given
        val questResponse = QuestResponse(
            questId = 1L,
            title = "title init",
            description = "description init",
            seq = 2L,
            state = QuestState.DISCARD,
            lastModifiedDate = LocalDateTime.now(),
            detailQuests = listOf(DetailResponse(1L, "title")),
            canComplete = false,
            type = QuestType.MAIN
        )
        val om = ObjectMapper()
        om.registerModule(JavaTimeModule())

        //when
        val run = {om.writeValueAsString(questResponse)}

        //then
        assertDoesNotThrow(run)
    }
}