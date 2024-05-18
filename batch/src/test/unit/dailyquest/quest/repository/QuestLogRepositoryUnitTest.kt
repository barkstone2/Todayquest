package dailyquest.quest.repository

import dailyquest.config.QueryDslConfig
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestState.*
import io.mockk.junit5.MockKExtension
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Pageable
import java.time.LocalDate

@Import(QueryDslConfig::class)
@ExtendWith(MockKExtension::class)
@DisplayName("퀘스트 로그 리포지토리 유닛 테스트")
@DataJpaTest
class QuestLogRepositoryUnitTest @Autowired constructor(
    private val questLogRepository: QuestLogRepository,
    private val entityManager: EntityManager
) {

    @DisplayName("getAllUserIdsWhoAchievedPerfectDay 메서드 호출 시")
    @Nested
    inner class TestGetAllUserIdsWhoAchievedPerfectDay {
        @DisplayName("loggedDate에 등록한 퀘스트를 모두 완료한 유저 아이디만 조회된다")
        @Test
        fun `loggedDate에 등록한 퀘스트를 모두 완료한 유저 아이디만 조회된다`() {
            //given
            val loggedDate = LocalDate.of(2020, 1, 1)
            val shouldBeContainedUserId = 1L
            saveQuestLog(shouldBeContainedUserId, 1L, PROCEED, loggedDate)
            saveQuestLog(shouldBeContainedUserId, 1L, COMPLETE, loggedDate)

            saveQuestLog(2L, 2L, PROCEED, loggedDate)

            //when
            val result = questLogRepository.getAllUserIdsWhoAchievedPerfectDay(loggedDate, Pageable.unpaged())

            //then
            assertThat(result).containsExactly(shouldBeContainedUserId)
        }

        @DisplayName("동일한 퀘스트에 대해 PROCEED와 COMPLETE 날짜가 다른 경우 조회되지 않는다")
        @Test
        fun `동일한 퀘스트에 대해 PROCEED와 COMPLETE 날짜가 다른 경우 조회되지 않는다`() {
            //given
            val loggedDate = LocalDate.of(2020, 1, 1)
            val shouldNotBeContainedUserId = 1L
            saveQuestLog(shouldNotBeContainedUserId, 1L, PROCEED, loggedDate)
            saveQuestLog(shouldNotBeContainedUserId, 1L, COMPLETE, loggedDate.plusDays(1))
            saveQuestLog(shouldNotBeContainedUserId, 2L, PROCEED, loggedDate.minusDays(1))
            saveQuestLog(shouldNotBeContainedUserId, 2L, COMPLETE, loggedDate)

            //when
            val result = questLogRepository.getAllUserIdsWhoAchievedPerfectDay(loggedDate, Pageable.unpaged())

            //then
            assertThat(result).doesNotContain(shouldNotBeContainedUserId)
        }

        @DisplayName("조회일에 포기하지 않은 모든 퀘스트를 완료한 경우에도 조회되지 않는다")
        @Test
        fun `조회일에 포기하지 않은 모든 퀘스트를 완료한 경우에도 조회되지 않는다`() {
            //given
            val loggedDate = LocalDate.of(2020, 1, 1)
            val shouldNotBeContainedUserId = 1L
            saveQuestLog(shouldNotBeContainedUserId, 1L, PROCEED, loggedDate)
            saveQuestLog(shouldNotBeContainedUserId, 1L, DISCARD, loggedDate)
            saveQuestLog(shouldNotBeContainedUserId, 2L, PROCEED, loggedDate)
            saveQuestLog(shouldNotBeContainedUserId, 2L, COMPLETE, loggedDate)

            //when
            val result = questLogRepository.getAllUserIdsWhoAchievedPerfectDay(loggedDate, Pageable.unpaged())

            //then
            assertThat(result).doesNotContain(shouldNotBeContainedUserId)
        }

        @DisplayName("조회일에 삭제하지 않은 모든 퀘스트를 완료한 경우에도 조회되지 않는다")
        @Test
        fun `조회일에 삭제하지 않은 모든 퀘스트를 완료한 경우에도 조회되지 않는다`() {
            //given
            val loggedDate = LocalDate.of(2020, 1, 1)
            val shouldNotBeContainedUserId = 1L
            saveQuestLog(shouldNotBeContainedUserId, 1L, PROCEED, loggedDate)
            saveQuestLog(shouldNotBeContainedUserId, 1L, DELETE, loggedDate)
            saveQuestLog(shouldNotBeContainedUserId, 2L, PROCEED, loggedDate)
            saveQuestLog(shouldNotBeContainedUserId, 2L, COMPLETE, loggedDate)

            //when
            val result = questLogRepository.getAllUserIdsWhoAchievedPerfectDay(loggedDate, Pageable.unpaged())

            //then
            assertThat(result).doesNotContain(shouldNotBeContainedUserId)
        }

        private fun saveQuestLog(userId: Long, questId: Long, state: QuestState, loggedDate: LocalDate) {
            val query =
                entityManager.createNativeQuery("insert into quest_log (quest_id, user_id, state, logged_date, type, created_date, last_modified_date) values (?, ?, ?, ?, 'MAIN', now(), now())")
            query.setParameter(1, userId)
            query.setParameter(2, questId)
            query.setParameter(3, state.name)
            query.setParameter(4, loggedDate)
            query.executeUpdate()
        }
    }
}