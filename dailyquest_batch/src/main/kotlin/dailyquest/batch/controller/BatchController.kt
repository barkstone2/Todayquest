package dailyquest.batch.controller

import dailyquest.batch.service.JobService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/ap1/v1")
@RestController
class BatchController(
    private val jobService: JobService,
) {

    @PostMapping("/check-and-achieve")
    fun checkAndAchieve(
        @RequestBody achievementId: Long,
    ) {
        jobService.runCheckAndAchieveJob(achievementId)
    }
}