package dailyquest.sse

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import java.util.concurrent.ConcurrentHashMap

@Service
class SseService {
    private val sseEmitters = ConcurrentHashMap<Long, SseEmitter>()

    fun createNewEmitter(userId: Long): SseEmitter {
        val sseEmitter = SseEmitter(0)
        sseEmitters[userId] = sseEmitter
        return sseEmitter
    }

    @Async
    fun sendNotificationEvent(userId: Long) {
        val sseEmitter = this.getEmitter(userId)
        sseEmitter?.send(event().name("notification").data(""))
    }

    private fun getEmitter(userId: Long): SseEmitter? {
        return sseEmitters[userId]
    }
}