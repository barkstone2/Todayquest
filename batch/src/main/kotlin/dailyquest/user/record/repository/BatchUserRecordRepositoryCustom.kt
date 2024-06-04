package dailyquest.user.record.repository

import dailyquest.achievement.entity.Achievement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BatchUserRecordRepositoryCustom {
    fun getAllUserIdWhoCanAchieveOf(targetAchievement: Achievement, pageable: Pageable): Page<Long>
}