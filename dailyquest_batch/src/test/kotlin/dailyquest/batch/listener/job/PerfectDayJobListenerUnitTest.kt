package dailyquest.batch.listener.job

import dailyquest.common.util.JobExecutionContextUtil
import dailyquest.common.util.WebApiUtil
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution

@ExtendWith(MockKExtension::class)
@DisplayName("완벽한 하루 작업 리스너 유닛 테스트")
class PerfectDayJobListenerUnitTest {
    @RelaxedMockK
    private lateinit var webApiUtil: WebApiUtil
    @InjectMockKs
    private lateinit var listener: PerfectDayJobListener
    private lateinit var jobExecution: JobExecution
    private lateinit var executionContextUtil: JobExecutionContextUtil
    private val notifiedUserIdsKey = "notifiedUserIds"

    @BeforeEach
    fun init() {
        jobExecution = JobExecution(1)
        executionContextUtil = spyk(JobExecutionContextUtil.from(jobExecution))
        mockkObject(JobExecutionContextUtil)
        every { JobExecutionContextUtil.from(eq(jobExecution)) } returns executionContextUtil
    }

    @DisplayName("beforeJob 요청 시 jobExecution으로 JobExecutionContextUtil을 초기화한다")
    @Test
    fun `beforeJob 요청 시 jobExecution으로 JobExecutionContextUtil을 초기화한다`() {
        //given
        //when
        listener.beforeJob(jobExecution)

        //then
        verify { JobExecutionContextUtil.from(eq(jobExecution)) }
    }

    @DisplayName("afterJob 요청 시")
    @Nested
    inner class TestAfterJob {
        private val notifiedUserIds = listOf(1L, 2L)

        @BeforeEach
        fun init() {
            listener.beforeJob(jobExecution)
            jobExecution.status = BatchStatus.COMPLETED
            every { executionContextUtil.getFromJobContext<List<Long>>(any()) } returns notifiedUserIds
        }

        @DisplayName("JobExecution의 상태가 COMPLETE가 아니면 처리 로직이 호출되지 않는다")
        @Test
        fun `JobExecution의 상태가 COMPLETE가 아니면 처리 로직이 호출되지 않는다`() {
            //given
            jobExecution.status = BatchStatus.STARTED

            //when
            listener.afterJob(jobExecution)

            //then
            verify(inverse = true) {
                executionContextUtil.getFromJobContext<List<Long>>(eq(notifiedUserIdsKey))
                webApiUtil.postSseNotify(any())
            }
        }

        @DisplayName("JobExecution의 상태가 COMPLETED면")
        @Nested
        inner class WhenJobExecutionCompleted {
            @DisplayName("JobExecutionContext에서 알림을 전송할 유저 목록을 조회한다")
            @Test
            fun `JobExecutionContext에서 알림을 전송할 유저 목록을 조회한다`() {
                //given
                //when
                listener.afterJob(jobExecution)

                //then
                verify { executionContextUtil.getFromJobContext<List<Long>>(eq(notifiedUserIdsKey)) }
            }

            @DisplayName("조회한 유저 목록이 null인 경우 서버로 요청을 보내지 않는다")
            @Test
            fun `조회한 유저 목록이 null인 경우 서버로 요청을 보내지 않는다`() {
                //given
                every { executionContextUtil.getFromJobContext<List<Long>>(any()) } returns null

                //when
                listener.afterJob(jobExecution)

                //then
                verify(inverse = true) {
                    webApiUtil.postSseNotify(any())
                }
            }

            @DisplayName("조회한 유저 목록이 비어있는 경우 서버로 요청을 보내지 않는다")
            @Test
            fun `조회한 유저 목록이 비어있는 경우 서버로 요청을 보내지 않는다`() {
                //given
                every { executionContextUtil.getFromJobContext<List<Long>>(any()) } returns emptyList()

                //when
                listener.afterJob(jobExecution)

                //then
                verify(inverse = true) {
                    webApiUtil.postSseNotify(any())
                }
            }

            @DisplayName("조회한 유저 목록이 비어있지 않으면 서버로 이벤트 전송 요청을 한다")
            @Test
            fun `조회한 유저 목록이 비어있지 않으면 서버로 이벤트 전송 요청을 한다`() {
                //given
                every { executionContextUtil.getFromJobContext<List<Long>>(any()) } returns notifiedUserIds

                //when
                listener.afterJob(jobExecution)

                //then
                verify {
                    webApiUtil.postSseNotify(eq(notifiedUserIds))
                }
            }
        }
    }
}