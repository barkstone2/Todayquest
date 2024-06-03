package dailyquest.batch.listener.step

import dailyquest.common.util.ExecutionContextUtil
import dailyquest.user.dto.UserPerfectDayCount
import dailyquest.user.entity.User
import dailyquest.user.record.entity.UserRecord
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
@DisplayName("완벽한 하루 횟수 증가 스텝 리스너 유닛 테스트")
class IncreasePerfectDayCountStepListenerUnitTest {
    @RelaxedMockK
    private lateinit var stepExecution: StepExecution
    @RelaxedMockK
    private lateinit var executionContextUtil: ExecutionContextUtil
    @InjectMockKs
    private lateinit var listener: IncreasePerfectDayCountStepListener
    private val userPerfectDayCountsKey = "userPerfectDayCounts"

    @BeforeEach
    fun init() {
        mockkObject(ExecutionContextUtil)
        every { ExecutionContextUtil.from(any()) } returns executionContextUtil
        listener = IncreasePerfectDayCountStepListener()
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
            verify { executionContextUtil.removeFromStepContext(userPerfectDayCountsKey) }
        }
    }

    @DisplayName("afterWrite 호출 시")
    @Nested
    inner class TestAfterWrite {
        @DisplayName("chunk에 담긴 유저 정보를 UserPerfectDayCount로 변환해 StepExecutionContex에 저장 요청한다")
        @Test
        fun `chunk에 담긴 유저 정보를 UserPerfectDayCount로 변환해 StepExecutionContext에 저장 요청한다`() {
            //given
            val user = mockk<UserRecord>(relaxed = true)
            val userId = 1L
            every { user.id } returns userId
            val chunk = Chunk(listOf(user))

            //when
            listener.afterWrite(chunk)

            //then
            verify {
                executionContextUtil.putToStepContext(
                    userPerfectDayCountsKey,
                    match<List<UserPerfectDayCount>> { list -> list.all { it.userId == userId } })
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
            verify { executionContextUtil.mergeListFromStepContextToJobContext<UserPerfectDayCount>(userPerfectDayCountsKey) }
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
            verify(inverse = true) { executionContextUtil.mergeListFromStepContextToJobContext<UserPerfectDayCount>(userPerfectDayCountsKey) }
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
            verify(exactly = 2) { executionContextUtil.removeFromStepContext(userPerfectDayCountsKey) }
        }
    }
}