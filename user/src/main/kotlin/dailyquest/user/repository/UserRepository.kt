package dailyquest.user.repository

import dailyquest.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {
    fun findByOauth2Id(userId: String): User?
    fun existsByNickname(nickname: String): Boolean
    fun findAllByIdIn(userIds: List<Long>, pageable: Pageable): Page<User>
}