package dailyquest.sse

import dailyquest.user.dto.UserPrincipal
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RequestMapping("/api/v1/sse")
@RestController
class SseController(
    private val sseService: SseService
) {

    @GetMapping(value = [""], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getSseEmitter(
        @AuthenticationPrincipal principal: UserPrincipal
    ): SseEmitter {
        val emitter = sseService.findOrCreateEmitter(principal.id)
        emitter.send(SseEmitter.event().name("connect").data(""))
        return emitter
    }
}