package dailyquest.common.util

import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution

@ExtendWith(MockKExtension::class)
class ExecutionContextUtilUnitTest {

    private lateinit var jobExecution: JobExecution
    private lateinit var stepExecution: StepExecution
    private lateinit var contextUtil: ExecutionContextUtil

    @BeforeEach
    fun init() {
        jobExecution = JobExecution(1)
        stepExecution = StepExecution("step", jobExecution)
        contextUtil = ExecutionContextUtil.from(stepExecution)
    }

    @DisplayName("putToStepContext 호출 시 StepExecutionContext에 데이터가 저장된다")
    @Test
    fun `putToStepContext 호출 시 StepExecutionContext에 데이터가 저장된다`() {
        //given
        val key = "key"
        val longs = listOf(1L, 2L)

        //when
        contextUtil.putToStepContext(key, longs)

        //then
        val result = stepExecution.executionContext.get(key)
        assertThat(result).isNotNull.isInstanceOf(List::class.java)
    }

    @DisplayName("StepExecutionContext에 없는 값을 조회하면 null을 반환한다")
    @Test
    fun `StepExecutionContext에 없는 값을 조회하면 null을 반환한다`() {
        //given
        //when
        val result = contextUtil.getFromStepContext<Long>("null")

        //then
        assertThat(result).isNull()
    }

    @DisplayName("StepExecutionContext에 담긴 타입과 다른 타입을 요청하면 ClassCastException 예외가 발생한다")
    @Test
    fun `StepExecutionContext에 담긴 타입과 다른 타입을 요청하면 ClassCastException 예외가 발생한다`() {
        //given
        val key = "key"
        val value = 1L
        contextUtil.putToStepContext("key", value)

        //when
        val function = { contextUtil.getFromStepContext<String>(key) }

        //then
        assertThrows<ClassCastException> { function.invoke() }
    }

    @DisplayName("jobExecutionContext 조회시 제네릭 타입 파라미터가 제대로 처리된다")
    @Test
    fun `jobExecutionContext 조회시 제네릭 타입 파라미터가 제대로 처리된다`() {
        //given
        val key = "key"
        val value = listOf(3L)
        contextUtil.putToJobContext(key, value)

        //when
        val fromJobContext = contextUtil.getFromJobContext<List<Long>>(key)

        //then
        assertThat(fromJobContext).isEqualTo(value)
    }

    @DisplayName("mergeListFromStepContextToJobContext 호출 시")
    @Nested
    inner class TestMergeListFromStepContextToJobContext {
        @DisplayName("JobContext에서 조회한 리스트가 null이 아니면 toMutableList를 호출한다")
        @Test
        fun `JobContext에서 조회한 리스트가 null이 아니면 toMutableList를 호출한다`() {
            //given
            val key = "key"
            val list = mockk<List<Long>>(relaxed = true)
            contextUtil.putToJobContext(key, list)

            //when
            contextUtil.mergeListFromStepContextToJobContext<Long>(key)

            //then
            verify { list.toMutableList() }
        }

        @DisplayName("JobContext에서 조회한 리스트가 null이면 새로운 mutableList를 생성한다")
        @Test
        fun `JobContext에서 조회한 리스트가 null이면 새로운 mutableList를 생성한다`() {
            //given
            val key = "key"

            //when
            contextUtil.mergeListFromStepContextToJobContext<Long>(key)

            //then
            val list = contextUtil.getFromJobContext<List<Long>>(key)
            assertThat(list).isNotNull.isInstanceOf(MutableList::class.java)
        }

        @DisplayName("StepContext에서 조회한 리스트가 null이 아니면, JobContext에서 조회한 리스트에 addAll을 호출한다")
        @Test
        fun `StepContext에서 조회한 리스트가 null이 아니면, JobContext에서 조회한 리스트에 addAll을 호출한다`() {
            //given
            val key = "key"
            val mutableList = mutableListOf(1L, 2L)
            val list = listOf(3L, 4L)
            contextUtil.putToJobContext(key, mutableList)
            contextUtil.putToStepContext(key, list)

            //when
            contextUtil.mergeListFromStepContextToJobContext<Long>(key)

            //then
            val result = contextUtil.getFromJobContext<List<Long>>(key)
            assertThat(result).containsAll(mutableList).containsAll(list)
        }

        @DisplayName("JobContext에 mutableList를 삽입한다")
        @Test
        fun `JobContext에 mutableList를 삽입한다`() {
            //given
            val key = "key"

            //when
            contextUtil.mergeListFromStepContextToJobContext<Long>(key)

            //then
            val result = contextUtil.getFromJobContext<List<Long>>(key)
            assertThat(result).isNotNull
        }
    }
}