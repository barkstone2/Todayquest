package dailyquest.user.repository

import dailyquest.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BatchUserRepository: UserRepository {
    fun findAllByQuestRegistrationCountGreaterThanEqual(questRegistrationCount: Long, pageable: Pageable): Page<User>
    fun findAllByQuestCompletionCountGreaterThanEqual(questCompletionCount: Long, pageable: Pageable): Page<User>
    fun findAllByMaxQuestContinuousRegistrationDaysGreaterThanEqual(maxQuestContinuousRegistrationDays: Long, pageable: Pageable): Page<User>
    fun findAllByMaxQuestContinuousCompletionDaysGreaterThanEqual(maxQuestContinuousCompletionDays: Long, pageable: Pageable): Page<User>
    fun findAllByGoldEarnAmountGreaterThanEqual(goldEarnAmount: Long, pageable: Pageable): Page<User>
    fun findAllByPerfectDayCountGreaterThanEqual(perfectDayCount: Long, pageable: Pageable): Page<User>
}