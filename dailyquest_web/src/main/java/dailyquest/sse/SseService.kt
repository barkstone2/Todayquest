package dailyquest.sse

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import java.util.concurrent.ConcurrentHashMap

@Async
@Service
class SseService {
    private val sseEmitters = ConcurrentHashMap<Long, SseEmitter>()

    fun findOrCreateEmitter(userId: Long): SseEmitter {
        val sseEmitter = createEmitter(userId)
        sseEmitters[userId] = sseEmitter
        return sseEmitter
    }

    fun sendNotificationEvent(userId: Long) {
        val sseEmitter = this.getEmitter(userId)
        sseEmitter?.send(event().name("notification").data(""))
    }

    private fun getEmitter(userId: Long): SseEmitter? {
        return sseEmitters[userId]
    }

    private fun createEmitter(userId: Long): SseEmitter {
        val sseEmitter = SseEmitter()
        sseEmitter.onCompletion {
            sseEmitters.remove(userId)
        }
        sseEmitter.onError {
            sseEmitter.complete()
        }
        sseEmitter.onTimeout {
            sseEmitter.complete()
        }
        return sseEmitter
    }
}