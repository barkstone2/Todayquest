package dailyquest.aop

import dailyquest.notification.service.NotificationService
import dailyquest.sse.SseService
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory

@ExtendWith(MockKExtension::class)
@DisplayName("알림 Aspect 유닛 테스트")
class NotificationAspectUnitTest {
    @RelaxedMockK
    private lateinit var sseService: SseService
    @RelaxedMockK
    private lateinit var notificationService: NotificationService
    private lateinit var notificationServiceProxy: NotificationService

    @BeforeEach
    fun init() {
        val factory = AspectJProxyFactory(notificationService)
        factory.addAspect(NotificationAspect(sseService))
        notificationServiceProxy = factory.getProxy()
    }

    @DisplayName("알림 저장 시 sse 전송 요청을 보낸다")
    @Test
    fun `알림 저장 시 sse 전송 요청을 보낸다`() {
        //given
        val userId = 1L

        //when
        notificationServiceProxy.saveNotification(mockk(), userId)

        //then
        verify { sseService.sendNotificationEvent(eq(userId)) }
    }
}