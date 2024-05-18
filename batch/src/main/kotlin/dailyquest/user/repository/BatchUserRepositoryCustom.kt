package dailyquest.user.repository

import dailyquest.achievement.entity.Achievement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BatchUserRepositoryCustom {
    fun getAllUserIdWhoCanAchieveOf(targetAchievement: Achievement, pageable: Pageable): Page<Long>
}