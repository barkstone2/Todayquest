package dailyquest.aop

import dailyquest.sse.SseService
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

@Aspect
@Component
class NotificationAspect(
    private val sseService: SseService
) {
    @Pointcut("execution(* dailyquest.notification.service.NotificationService.saveNotification(..))")
    fun targetMethod(){}

    @AfterReturning(
        pointcut = "targetMethod()",
        returning = "result"
    )
    fun afterSaveNotification(joinPoint: JoinPoint, result: Any?) {
        val args = joinPoint.args
        val userId = args[1] as Long
        sseService.sendNotificationEvent(userId)
    }
}