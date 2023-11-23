package dailyquest.batch

import dailyquest.quest.entity.Quest
import dailyquest.quest.entity.QuestState
import dailyquest.quest.entity.QuestType
import dailyquest.quest.repository.QuestRepository
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.scope.context.StepSynchronizationManager
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.function.FunctionItemProcessor
import org.springframework.batch.test.MetaDataInstanceFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DisplayName("퀘스트 배치 구성 요소 테스트")
@Transactional
@SpringBootTest
class QuestBatchComponentTest @Autowired constructor(
    private val questRepository: QuestRepository,
    private val questResetReader: RepositoryItemReader<Quest>,
    private val questDeadLineReader: RepositoryItemReader<Quest>,
    private val questFailProcessor: FunctionItemProcessor<Quest, Quest>,
    private val questWriter: RepositoryItemWriter<Quest>,
    private val entityManager: EntityManager,
) {

    lateinit var testUser: UserInfo
    lateinit var anotherUser: UserInfo

    @BeforeEach
    fun init() {
        testUser = UserInfo("testUser", "testUser", ProviderType.GOOGLE)
        anotherUser = UserInfo("anotherUser", "anotherUser", ProviderType.GOOGLE)
        entityManager.persist(testUser)

        anotherUser.updateResetTime(9, LocalDateTime.now())
        entityManager.persist(anotherUser)
    }

    @DisplayName("questResetReader 동작 시 resetTime이 일치하는 유저의 퀘스트만 조회된다")
    @Test
    fun `questResetReader 동작 시 resetTime이 일치하는 유저의 퀘스트만 조회된다`() {
        //given
        val mustContainList = listOf(
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
        )
        val resetTime = testUser.resetTime
        val mustNotContainQuest = questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, null))

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

    @DisplayName("questDeadLineReader 동작 시 deadLine이 targetDate 이전인 퀘스트만 조회된다")
    @Test
    fun `questDeadLineReader 동작 시 deadLine이 targetDate 이전인 퀘스트만 조회된다`() {
        //given

        val currentTime = LocalDateTime.now().withSecond(0).withNano(0)

        val mustContainList = listOf(
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
            questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime)),
            questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.minusMinutes(1))),
        )

        val mustNotContainList = listOf(
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.plusMinutes(1))),
            questRepository.save(Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
            questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, currentTime.plusMinutes(1))),
            questRepository.save(Quest("", "", anotherUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)),
        )

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
        assertThat(readQuests).doesNotContainAnyElementsOf(mustNotContainList)
    }

    @DisplayName("failProcessor 동작 후 퀘스트가 fail 상태로 변경된다")
    @Test
    fun `failProcessor 동작 후 퀘스트가 fail 상태로 변경된다`() {
        //given
        val quest = Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)

        //when
        questFailProcessor.process(quest)

        //then
        assertThat(quest.state).isEqualTo(QuestState.FAIL)
    }

    @DisplayName("questWriter 동작 시 quest 상태가 저장된다")
    @Test
    fun `questWriter 동작 시 quest 상태가 저장된다`() {
        //given
        val quest = Quest("", "", testUser, 0L, QuestState.PROCEED, QuestType.MAIN, null)

        //when
        questWriter.write(Chunk(quest))

        //then
        assertThat(quest.id).isNotNull().isNotEqualTo(0L)
    }

}