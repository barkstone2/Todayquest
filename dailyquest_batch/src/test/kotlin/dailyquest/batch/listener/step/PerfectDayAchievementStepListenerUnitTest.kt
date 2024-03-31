package dailyquest.batch.listener.step

import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.common.util.ExecutionContextUtil
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.item.Chunk

@ExtendWith(MockKExtension::class)
@DisplayName("완벽한 하루 업적 스텝 리스너 유닛 테스트")
class PerfectDayAchievementStepListenerUnitTest {

    @RelaxedMockK
    private lateinit var stepExecution: StepExecution
    @RelaxedMockK
    private lateinit var executionContextUtil: ExecutionContextUtil
    @RelaxedMockK
    private lateinit var achievementRepository: AchievementRepository
    @InjectMockKs
    private lateinit var perfectDayLogStepListener: PerfectDayAchievementStepListener
    private val perfectDayAchievementsKey = "perfectDayAchievements"
    private val achievedLogsKey = "achievedLogs"

    @BeforeEach
    fun init() {
        mockkObject(ExecutionContextUtil)
        every { ExecutionContextUtil.from(any()) } returns executionContextUtil
    }

    @DisplayName("beforeStep 호출 시")
    @Nested
    inner class TestBeforeStep {
        @DisplayName("StepExecutionContext에 업적 목록을 조회해서 담는다")
        @Test
        fun `StepExecutionContext에 업적 목록을 조회해서 담는다`() {
            //given
            //when
            perfectDayLogStepListener.beforeStep(stepExecution)

            //then
            verify {
                achievementRepository.getAllByType(eq(AchievementType.PERFECT_DAY))
                executionContextUtil.putToStepContext(perfectDayAchievementsKey, any())
            }
        }
    }

    @DisplayName("afterStep 호출 시")
    @Nested
    inner class TestAfterStep {
        @BeforeEach
        fun init() {
            perfectDayLogStepListener.beforeStep(stepExecution)
        }

        @DisplayName("stepExecutionContext에 담긴 달성한 업적 로그 제거를 요청한다")
        @Test
        fun `stepExecutionContext에 담긴 달성한 업적 로그 제거를 요청한다`() {
            //given
            //when
            perfectDayLogStepListener.afterStep(stepExecution)

            //then
            verify { executionContextUtil.removeFromStepContext(achievedLogsKey) }
        }

        @DisplayName("stepExecutionContext에 담긴 업적 목록 제거를 요청한다")
        @Test
        fun `stepExecutionContext에 담긴 업적 목록 제거를 요청한다`() {
            //given
            //when
            perfectDayLogStepListener.afterStep(stepExecution)

            //then
            verify { executionContextUtil.removeFromStepContext(perfectDayAchievementsKey) }
        }
    }

    @DisplayName("afterWrite 호출 시")
    @Nested
    inner class TestAfterWrite {
        @BeforeEach
        fun init() {
            perfectDayLogStepListener.beforeStep(stepExecution)
        }

        @DisplayName("처리한 청크를 StepExecutionContex에 저장 요청한다")
        @Test
        fun `처리한 청크를 StepExecutionContext에 저장 요청한다`() {
            //given
            val achievementAchieveLog = mockk<AchievementAchieveLog>()
            val chunk = Chunk(listOf(achievementAchieveLog))

            //when
            perfectDayLogStepListener.afterWrite(chunk)

            //then
            verify {
                executionContextUtil.putToStepContext(
                    achievedLogsKey,
                    match<List<AchievementAchieveLog>> { list -> list.all { it == achievementAchieveLog } })
            }
        }
    }

    @DisplayName("afterChunk 호출 시")
    @Nested
    inner class TestAfterChunk {
        @DisplayName("ChunkContext가 완료 상태면 mergeList 메서드가 호출된다")
        @Test
        fun `ChunkContext가 완료 상태면 mergeList 메서드가 호출된다`() {
            //given
            val chunkContext = mockk<ChunkContext>()
            every { chunkContext.isComplete } returns true

            //when
            perfectDayLogStepListener.afterChunk(chunkContext)

            //then
            verify { executionContextUtil.mergeListFromStepContextToJobContext<AchievementAchieveLog>(achievedLogsKey) }
        }

        @DisplayName("ChunkContext가 완료 상태가 아니면 mergeList 메서드가 호출되지 않는다")
        @Test
        fun `ChunkContext가 완료 상태가 아니면 mergeList 메서드가 호출되지 않는다`() {
            //given
            val chunkContext = mockk<ChunkContext>()
            every { chunkContext.isComplete } returns false

            //when
            perfectDayLogStepListener.afterChunk(chunkContext)

            //then
            verify(inverse = true) { executionContextUtil.mergeListFromStepContextToJobContext<AchievementAchieveLog>(achievedLogsKey) }
        }

        @DisplayName("ChunkContext의 완료 여부와 관계 없이 removeFromStep이 호출된다")
        @Test
        fun `ChunkContext의 완료 여부와 관계 없이 removeFromStep이 호출된다`() {
            //given
            val chunkContext = mockk<ChunkContext>()
            every { chunkContext.isComplete } returns true andThen false

            //when
            perfectDayLogStepListener.afterChunk(chunkContext)
            perfectDayLogStepListener.afterChunk(chunkContext)

            //then
            verify(exactly = 2) { executionContextUtil.removeFromStepContext(achievedLogsKey) }
        }
    }
}