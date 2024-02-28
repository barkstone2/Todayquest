package dailyquest.context

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.repository.AchievementRepository
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@ComponentScan(basePackages = ["dailyquest.achievement"])
@EnableJpaRepositories(basePackageClasses = [AchievementRepository::class])
@EntityScan(basePackageClasses = [Achievement::class])
@TestConfiguration
class AchievementTestContextConfig {
}