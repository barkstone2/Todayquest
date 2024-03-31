package dailyquest.batch.listener.step

import dailyquest.achievement.entity.AchievementAchieveLog
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
@DisplayName("업적 달성 알림 스텝 리스너 유닛 테스트")
class AchievementAchieveNotificationStepListenerUnitTest {
    @RelaxedMockK
    private lateinit var stepExecution: StepExecution
    @RelaxedMockK
    private lateinit var executionContextUtil: ExecutionContextUtil
    @InjectMockKs
    private lateinit var listener: AchievementAchieveNotificationStepListener
    private val notifiedUserIdsKey = "notifiedUserIds"

    @BeforeEach
    fun init() {
        mockkObject(ExecutionContextUtil)
        every { ExecutionContextUtil.from(any()) } returns executionContextUtil
        listener = AchievementAchieveNotificationStepListener()
        listener.beforeStep(stepExecution)
    }

    @DisplayName("afterStep 호출 시")
    @Nested
    inner class TestAfterStep {
        @DisplayName("stepExecutionContext에 담긴 처리한 데이터 제거를 요청한다")
        @Test
        fun `stepExecutionContext에 담긴 처리한 데이터 제거를 요청한다`() {
            //given
            //when
            listener.afterStep(stepExecution)

            //then
            verify { executionContextUtil.removeFromStepContext(notifiedUserIdsKey) }
        }
    }

    @DisplayName("afterWrite 호출 시")
    @Nested
    inner class TestAfterWrite {

        @DisplayName("chunk에서 userId를 추출해 StepExecutionContex에 저장 요청한다")
        @Test
        fun `chunk에서 userId를 추출해 StepExecutionContext에 저장 요청한다`() {
            //given
            val achievementAchieveLog = mockk<AchievementAchieveLog>()
            val userId = 1L
            every { achievementAchieveLog.userId } returns userId
            val chunk = Chunk(listOf(achievementAchieveLog))

            //when
            listener.afterWrite(chunk)

            //then
            verify {
                executionContextUtil.putToStepContext(
                    notifiedUserIdsKey,
                    match<List<Long>> { list -> list.all { it == userId } })
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
            listener.afterChunk(chunkContext)

            //then
            verify { executionContextUtil.mergeListFromStepContextToJobContext<Long>(notifiedUserIdsKey) }
        }

        @DisplayName("ChunkContext가 완료 상태가 아니면 mergeList 메서드가 호출되지 않는다")
        @Test
        fun `ChunkContext가 완료 상태가 아니면 mergeList 메서드가 호출되지 않는다`() {
            //given
            val chunkContext = mockk<ChunkContext>()
            every { chunkContext.isComplete } returns false

            //when
            listener.afterChunk(chunkContext)

            //then
            verify(inverse = true) { executionContextUtil.mergeListFromStepContextToJobContext<Long>(notifiedUserIdsKey) }
        }

        @DisplayName("ChunkContext의 완료 여부와 관계 없이 removeFromStep이 호출된다")
        @Test
        fun `ChunkContext의 완료 여부와 관계 없이 removeFromStep이 호출된다`() {
            //given
            val chunkContext = mockk<ChunkContext>()
            every { chunkContext.isComplete } returns true andThen false

            //when
            listener.afterChunk(chunkContext)
            listener.afterChunk(chunkContext)

            //then
            verify(exactly = 2) { executionContextUtil.removeFromStepContext(notifiedUserIdsKey) }
        }
    }
}