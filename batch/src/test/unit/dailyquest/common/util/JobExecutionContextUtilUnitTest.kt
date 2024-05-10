package dailyquest.common.util

import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.JobExecution

@ExtendWith(MockKExtension::class)
@DisplayName("JobExecutionContextUtil 유닛 테스트")
class JobExecutionContextUtilUnitTest {
    lateinit var jobExecution: JobExecution
    private lateinit var contextUtil: JobExecutionContextUtil

    @BeforeEach
    fun init() {
        jobExecution = JobExecution(1)
        contextUtil = JobExecutionContextUtil.from(jobExecution)
    }

    @DisplayName("putToJobContext 호출 시 JobExecutionContext에 데이터가 저장된다")
    @Test
    fun `putToJobContext 호출 시 JobExecutionContext에 데이터가 저장된다`() {
        //given
        val key = "key"
        val longs = listOf(1L, 2L)

        //when
        contextUtil.putToJobContext(key, longs)

        //then
        val result = jobExecution.executionContext.get(key)
        assertThat(result).isNotNull.isInstanceOf(List::class.java)
    }

    @DisplayName("JobExecutionContext에 없는 값을 조회하면 null을 반환한다")
    @Test
    fun `JobExecutionContext에 없는 값을 조회하면 null을 반환한다`() {
        //given
        //when
        val result = contextUtil.getFromJobContext<Long>("null")

        //then
        assertThat(result).isNull()
    }

    @DisplayName("JobExecutionContext에 담긴 타입과 다른 타입을 요청하면 ClassCastException 예외가 발생한다")
    @Test
    fun `JobExecutionContext에 담긴 타입과 다른 타입을 요청하면 ClassCastException 예외가 발생한다`() {
        //given
        val key = "key"
        val value = 1L
        contextUtil.putToJobContext("key", value)

       //when
        val function = { contextUtil.getFromJobContext<String>(key) }

        //then
        assertThrows<ClassCastException> { function.invoke() }
    }
}