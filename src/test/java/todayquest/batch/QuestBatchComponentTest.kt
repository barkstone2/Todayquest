package todayquest.batch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.scope.context.StepSynchronizationManager
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.test.MetaDataInstanceFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import todayquest.quest.entity.Quest
import todayquest.quest.entity.QuestState
import todayquest.quest.entity.QuestType
import todayquest.quest.repository.QuestRepository
import todayquest.user.entity.ProviderType
import todayquest.user.entity.UserInfo
import todayquest.user.repository.UserRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DisplayName("퀘스트 배치 구성 요소 테스트")
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestBatchComponentTest @Autowired constructor(
    private val questRepository: QuestRepository,
    private val userRepository: UserRepository,
    private val questResetReader: ItemReader<Quest>,
    private val questDeadLineReader: ItemReader<Quest>,
    private val questFailProcessor: ItemProcessor<Quest, Quest>,
    private val questWriter: ItemWriter<Quest>,
) {

    @DisplayName("questResetReader 동작 시 resetTime이 일치하는 유저의 퀘스트만 조회된다")
    @Test
    fun `questResetReader 동작 시 resetTime이 일치하는 유저의 퀘스트만 조회된다`() {
        //given
        val user = userRepository.findById(1L).get()
        val anotherUser = userRepository.findById(2L).get() // resetTime 09:00

        val mustContainList = listOf(
            questRepository.save(Quest("t1", "d1", user, 1L, type = QuestType.MAIN)),
            questRepository.save(Quest("t2", "d2", user, 1L, type = QuestType.MAIN)),
            questRepository.save(Quest("t3", "d3", user, 1L, type = QuestType.MAIN)),
        )
        val resetTime = user.resetTime
        val mustNotContainQuest = questRepository.save(Quest("t", "d", anotherUser, 1L, type = QuestType.MAIN))

        val jobParameters = JobParametersBuilder()
            .addString("resetTime", resetTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
            .toJobParameters()

        val stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters)
        StepSynchronizationManager.register(stepExecution)

        //when
        val readQuests = mutableListOf<Quest>()

        while (true) {
            val quest = questResetReader.read() ?: break
            readQuests.add(quest)
        }

        //then
        assertThat(readQuests).containsAll(mustContainList)
        assertThat(readQuests).doesNotContain(mustNotContainQuest)
    }

    @DisplayName("questDeadLineReader 동작 시 deadline이 targetDate와 일치하는 퀘스트만 조회된다")
    @Test
    fun `questDeadLineReader 동작 시 deadline이 targetDate와 일치하는 퀘스트만 조회된다`() {
        //given
        val user = userRepository.findById(1L).get()
        val currentTime = LocalDateTime.now()

        val mustContainList = listOf(
            questRepository.save(
                Quest("t", "d", user, 1L,
                    type = QuestType.MAIN,
                    deadline = currentTime.minusMinutes(1)
                )
            ),
            questRepository.save(
                Quest("t", "d", user, 1L,
                    type = QuestType.MAIN,
                    deadline = currentTime.minusMinutes(1)
                )
            ),
            questRepository.save(
                Quest("t", "d", user, 1L,
                    type = QuestType.MAIN,
                    deadline = currentTime.minusMinutes(1)
                )
            ),
        )

        val mustNotContainQuest = questRepository.save(Quest("t", "d", user, 1L, type = QuestType.MAIN))

        val jobParameters = JobParametersBuilder()
            .addString("targetDate", currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .toJobParameters()

        val stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters)
        StepSynchronizationManager.register(stepExecution)

        //when
        val readQuests = mutableListOf<Quest>()

        while (true) {
            val quest = questDeadLineReader.read() ?: break
            readQuests.add(quest)
        }

        //then
        assertThat(readQuests).containsAll(mustContainList)
        assertThat(readQuests).doesNotContain(mustNotContainQuest)
    }

    @DisplayName("failProcessor 동작 후 fail 상태로 변경된다")
    @Test
    fun `failProcessor 동작 후 fail 상태로 변경된다`() {
        //given
        val user = UserInfo("o", "n", ProviderType.GOOGLE)
        val quest = Quest("q", "q", user, 1L, QuestState.PROCEED, QuestType.MAIN)

        //when
        questFailProcessor.process(quest)

        //then
        assertThat(quest.state).isEqualTo(QuestState.FAIL)
    }

    @DisplayName("questWriter 동작 시 quest 상태가 저장된다")
    @Test
    fun `questWriter 동작 시 quest 상태가 저장된다`() {
        //given
        val user = userRepository.findById(1L).get()
        val quest = Quest("q", "q", user, 1L, QuestState.PROCEED, QuestType.MAIN)

        //when
        questWriter.write(Chunk(quest))

        //then
        assertThat(quest.id).isNotNull().isNotEqualTo(0L)
    }




}