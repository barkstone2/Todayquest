package todayquest.quest.repository

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.NestedTestConfiguration.*
import todayquest.config.JpaAuditingConfiguration
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestState
import todayquest.quest.entity.QuestType
import todayquest.user.entity.UserInfo
import todayquest.user.repository.UserRepository
import java.time.LocalDateTime
import java.util.*

@DisplayName("퀘스트 리포지토리 유닛 테스트")
@DataJpaTest
@Import(JpaAuditingConfiguration::class)
class QuestRepositoryUnitTest {

    @Autowired
    lateinit var questRepository: QuestRepository

    @Autowired
    lateinit var userRepository: UserRepository

    lateinit var userInfo: UserInfo
    lateinit var anotherUser: UserInfo
    lateinit var quest: Quest

    @BeforeEach
    fun init() {

        userInfo = userRepository.getReferenceById(1L)
        anotherUser = userRepository.getReferenceById(2L)

        quest = Quest(
            "title",
            "desc",
            userInfo,
            1L,
            QuestState.PROCEED,
            QuestType.MAIN
        )

        quest = questRepository.save(quest)
    }

    @DisplayName("getReferenceById 호출 시")
    @Nested
    inner class GetReferenceByIdTest {

        @DisplayName("ID가 null이면 InvalidDataAccessApiUsageException 예외가 던져진다")
        @Test
        fun `실패 테스트`() {
            //given
            val questId = null

            //when
            //then
            assertThrows<InvalidDataAccessApiUsageException> { questRepository.getReferenceById(null) }
        }

        @DisplayName("유효한 ID가 들어오면 퀘스트가 조회된다")
        @Test
        fun `성공 테스트`() {

            //given
            val questId = quest.id

            //when
            val findQuest = questRepository.getReferenceById(questId)

            //then
            assertThat(findQuest.title).isEqualTo(quest.title)
            assertThat(findQuest.description).isEqualTo(quest.description)
        }
    }


    @DisplayName("findById 호출 시")
    @Nested
    inner class FindByIdTest {

        @DisplayName("ID가 null이면 InvalidDataAccessApiUsageException 예외가 던져진다")
        @Test
        fun `실패 테스트`() {
            //given
            val questId = null

            //when
            //then
            assertThrows<InvalidDataAccessApiUsageException> { questRepository.findById(questId) }
        }

        @DisplayName("유효한 ID가 들어오면 퀘스트가 조회된다")
        @Test
        fun `성공 테스트`() {
            //given
            val questId = quest.id

            //when
            val findQuest = questRepository.findById(questId).get()

            //then
            assertThat(findQuest.title).isEqualTo(quest.title)
            assertThat(findQuest.description).isEqualTo(quest.description)
        }
    }

    @DisplayName("save 호출 시")
    @Nested
    inner class SaveTest {

        @DisplayName("엔티티가 null이면 InvalidDataAccessApiUsageException 예외가 던져진다")
        @Test
        fun `실패 테스트`() {
            //given
            val questId = null

            //when
            //then
            assertThrows<InvalidDataAccessApiUsageException> { questRepository.save(questId) }
        }

        @DisplayName("유효한 엔티티가 들어오면 퀘스트가 등록된다")
        @Test
        fun `성공 테스트`() {
            //given
            val newQuest = Quest(
                title = "newTitle",
                description = "",
                user = userInfo,
                seq = 1L,
                type = QuestType.MAIN
            )

            //when
            val savedQuest = questRepository.save(newQuest)

            //then
            assertThat(savedQuest.title).isEqualTo(newQuest.title)
            assertThat(savedQuest.description).isEqualTo(newQuest.description)
        }
    }

    @DisplayName("getQuestsList 호출 시")
    @Nested
    inner class ListTest {

        @EnumSource(QuestState::class)
        @DisplayName("조회한 상태의 퀘스트만 조회된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 {0} 상태의 퀘스트만 조회된다")
        fun `퀘스트 타입별 조회`(state: QuestState) {
            //given
            questRepository.save(Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN))
            questRepository.save(Quest("", "", userInfo, 1L, QuestState.FAIL, QuestType.MAIN))
            questRepository.save(Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.MAIN))
            questRepository.save(Quest("", "", userInfo, 1L, QuestState.DELETE, QuestType.MAIN))
            questRepository.save(Quest("", "", userInfo, 1L, QuestState.COMPLETE, QuestType.MAIN))

            //when
            val questsList = questRepository.getQuestsList(userInfo.id, state, Pageable.unpaged())

            //then
            assertThat(questsList).allMatch { quest -> quest.state == state }
        }


        @ValueSource(longs = [1, 2])
        @DisplayName("조회한 유저의 퀘스트만 조회된다")
        @ParameterizedTest(name = "userId {0} 값이 들어오면 {0}번 유저의 퀘스트만 조회된다")
        fun `퀘스트 유저별 조회`(userId: Long) {
            //given
            questRepository.save(Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN))
            questRepository.save(Quest("", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN))
            questRepository.save(Quest("", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN))
            questRepository.save(Quest("", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN))
            questRepository.save(Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN))

            //when
            val questsList = questRepository.getQuestsList(userId, QuestState.PROCEED, Pageable.unpaged())

            //then
            assertThat(questsList).allMatch { quest -> quest.user.id == userId }
        }

        @ValueSource(ints = [0, 1, 2])
        @DisplayName("페이지 번호에 맞는 퀘스트가 조회된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 {0} 페이지의 퀘스트가 조회된다")
        fun `페이징 적용 조회`(pageNo: Int) {
            //given
            val savedList = listOf(
                questRepository.save(Quest("1", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("2", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("3", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN))
            )

            //when
            val questsList = questRepository.getQuestsList(anotherUser.id, QuestState.PROCEED, PageRequest.of(pageNo, 1))

            //then
            assertThat(questsList.number).isEqualTo(pageNo)
            assertThat(questsList.content[0]).isEqualTo(savedList[pageNo])
        }
    }

    @DisplayName("getQuestsForBatch 호출 시")
    @Nested
    inner class BatchListTest {

        @EnumSource(QuestState::class)
        @DisplayName("조회한 상태의 퀘스트만 조회된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 {0} 상태의 퀘스트만 조회된다")
        fun `퀘스트 상태별 조회`(state: QuestState) {

            //given
            val resetTime = userInfo.resetTime
            val questOfState = mapOf(
                Pair(QuestState.PROCEED, questRepository.save(Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN))),
                Pair(QuestState.FAIL, questRepository.save(Quest("", "", userInfo, 1L, QuestState.FAIL, QuestType.MAIN))),
                Pair(QuestState.DISCARD, questRepository.save(Quest("", "", userInfo, 1L, QuestState.DISCARD, QuestType.MAIN))),
                Pair(QuestState.DELETE, questRepository.save(Quest("", "", userInfo, 1L, QuestState.DELETE, QuestType.MAIN))),
                Pair(QuestState.COMPLETE, questRepository.save(Quest("", "", userInfo, 1L, QuestState.COMPLETE, QuestType.MAIN))),
            )

            //when
            val questsList = questRepository.getQuestsForBatch(state, LocalDateTime.now(), resetTime, Pageable.unpaged())

            //then
            assertThat(questsList.content).contains(questOfState[state])
            assertThat(questsList).allMatch { quest -> quest.state == state }
        }


        @DisplayName("조회한 시간 이전에 생성된 퀘스트만 조회된다")
        @Test
        fun `퀘스트 생성시간별 조회`() {

            //given
            val resetTime = userInfo.resetTime
            val beforeList = listOf(
                questRepository.save(Quest("1", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("2", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN))
            )
            questRepository.flush()
            val targetDate = LocalDateTime.now()

            val afterList = listOf(
                questRepository.save(Quest("3", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("4", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("5", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN))
            )

            //when
            val questsList = questRepository.getQuestsForBatch(QuestState.PROCEED, targetDate, resetTime, Pageable.unpaged())

            //then
            assertThat(questsList).allMatch { quest -> quest.createdDate?.isBefore(targetDate)!! }
            assertThat(questsList.content).containsAll(beforeList)
            assertThat(questsList.content).doesNotContainAnyElementsOf(afterList)
        }

        @DisplayName("등록한 유저의 resetTime이 파라미터 값과 일치하는 퀘스트만 조회된다")
        @Test
        fun `등록유저의 resetTime이 일치하는 경우만 조회`() {

            //given
            val resetTime = anotherUser.resetTime // userInfo-00:00, anotherUser-09:00
            val mustNotContain = listOf(
                questRepository.save(Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("", "", userInfo, 1L, QuestState.PROCEED, QuestType.MAIN))
            )
            val mustContain = listOf(
                questRepository.save(Quest("", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN)),
            )

            //when
            val questsList = questRepository.getQuestsForBatch(QuestState.PROCEED, LocalDateTime.now(), resetTime, Pageable.unpaged())

            //then
            assertThat(questsList.content).containsAll(mustContain)
            assertThat(questsList.content).doesNotContainAnyElementsOf(mustNotContain)
        }


        @ValueSource(ints = [0, 1, 2])
        @DisplayName("페이지 번호에 맞는 퀘스트가 조회된다")
        @ParameterizedTest(name = "{0} 값이 들어오면 {0} 페이지의 퀘스트가 조회된다")
        fun `페이징 테스트`(pageNo: Int) {
            //given
            val savedList = listOf(
                questRepository.save(Quest("1", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("2", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN)),
                questRepository.save(Quest("3", "", anotherUser, 1L, QuestState.PROCEED, QuestType.MAIN))
            )

            //when
            val questsList = questRepository.getQuestsForBatch(QuestState.PROCEED, LocalDateTime.now(), anotherUser.resetTime, PageRequest.of(pageNo, 1))

            //then
            assertThat(questsList.number).isEqualTo(pageNo)
            assertThat(questsList.content[0]).isEqualTo(savedList[pageNo])
        }
    }


    @DisplayName("getNextSeqByUserId 호출 시")
    @Nested
    inner class GetNextSeqByUserIdTest {

        @Test
        @DisplayName("새로운 퀘스트가 등록되지 않으면 항상 같은 값을 반환한다")
        fun `새로운 퀘스트가 등록되지 않으면 항상 같은 값을 반환한다`() {

            //given
            val userId = userInfo.id
            val initSeq = questRepository.getNextSeqByUserId(userId)

            //when
            val nextSeq = questRepository.getNextSeqByUserId(userId)

            //then
            assertThat(initSeq).isEqualTo(nextSeq)
        }

        @Test
        @DisplayName("현재 퀘스트의 MAX_SEQ+1 값을 반환한다")
        fun `현재 퀘스트의 MAX_SEQ+1 값을 반환한다`() {

            //given
            val userId = userInfo.id
            val maxSeq = 5351L
            val saveQuest = Quest("", "", userInfo, maxSeq, QuestState.PROCEED, QuestType.MAIN)
            questRepository.save(saveQuest)

            //when
            val nextSeq = questRepository.getNextSeqByUserId(userId)

            //then
            assertThat(nextSeq).isEqualTo(maxSeq+1)
        }

        @Test
        @DisplayName("현재 등록된 퀘스트가 없으면 1을 반환한다")
        fun `현재 등록된 퀘스트가 없으면 1을 반환한다`() {

            //given
            val userId = userInfo.id
            questRepository.deleteAll()
            questRepository.flush()

            //when
            val nextSeq = questRepository.getNextSeqByUserId(userId)

            //then
            assertThat(nextSeq).isEqualTo(1)
        }


        @Test
        @DisplayName("유저 별로 SEQ 채번이 나눠진다")
        fun `유저 별로 SEQ 채번이 나눠진다`() {

            //given
            val userId1 = userInfo.id
            val userId2 = anotherUser.id
            val user1Seq = 533L
            questRepository.deleteAll()
            questRepository.flush()

            val saveQuest = Quest("", "", userInfo, user1Seq, QuestState.PROCEED, QuestType.MAIN)
            questRepository.save(saveQuest)

            //when
            val user1NextSeq = questRepository.getNextSeqByUserId(userId1)
            val user2NextSeq = questRepository.getNextSeqByUserId(userId2)

            //then
            assertThat(user1NextSeq).isEqualTo(user1Seq+1)
            assertThat(user2NextSeq).isEqualTo(1)
        }
    }

}